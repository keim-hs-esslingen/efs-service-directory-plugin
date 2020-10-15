/*
 * MIT License
 * 
 * Copyright (c) 2020 Hochschule Esslingen
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
package de.hsesslingen.keim.efs.servicedirectory.core;

import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
import de.hsesslingen.keim.efs.servicedirectory.core.MobilityServiceRegistry.ActivityState;
import java.time.Duration;
import java.time.Instant;
import static java.util.stream.Collectors.toList;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * This class checks the registered services periodically for availability.
 *
 * @author keim
 */
@Service
public class AvailabilityChecker {

    private static final Logger logger = LoggerFactory.getLogger(AvailabilityChecker.class);

    private static final String SERVICE_INFO_PATH = "/service-info";

    @Autowired
    private MobilityServiceRegistry registry;

    @Value("${service-directory.availability-checker.state-valid-duration:PT1M}")
    private Duration stateValidDuration;

    @Value("${service-directory.availability-checker.checking-rate:10000}")
    private int checkingRate;

    @PostConstruct
    public void init() {
        if (stateValidDuration == null) {
            throw new IllegalArgumentException("The value in configuration property \"service-directory.availability-checker.state-valid-duration\" could not be parsed to a Duration.");
        }

        var stateValidMillis = stateValidDuration.toMillis();

        if (checkingRate > stateValidMillis) {
            logger.warn("The configured duration of milliseconds for \"service-directory.availability-checker.checking-rate\" is bigger that the configured duration for \"service-directory.availability-checker.state-valid-duration\". It is better to use a smaller duration for the first one than for the last one because this makes sure that invalid service states will be rechecked at latest after their doubled valid-duration.");
        }
    }

    @Scheduled(fixedRateString = "${service-directory.availability-checker.checking-rate:10000}")
    public void checkAlmostDueMobilityServices() {
        var requests = registry.streamServiceStates()
                // Filter out those services that are NOT due for checking...
                .filter(pair -> isDueForChecking(pair.getRight()))
                // Add a preconfigured request to the pair (which makes it a Triple).
                // This needs to be done, because the outgoing request adapters of each request must 
                // be called from the main thread, because they might rely on ThreadLocal storage.
                .map(pair -> Triple.of(pair.getLeft(), pair.getRight(), createServiceInfoRequest(pair.getLeft())))
                .collect(toList());

        // Send off the actual availability checking requests...
        requests.parallelStream()
                .forEach(triple -> {
                    var request = triple.getRight();
                    var state = triple.getMiddle();
                    checkServiceAvailability(request, state);
                });
    }

    private boolean isDueForChecking(ActivityState state) {
        return state.getLastUpdate().plus(stateValidDuration).isBefore(Instant.now());

    }

    private EfsRequest<MobilityService> createServiceInfoRequest(MobilityService service) {
        var baseUrl = service.getServiceUrl();
        return EfsRequest.get(baseUrl + SERVICE_INFO_PATH)
                .callOutgoingRequestAdapters()
                .expect(MobilityService.class);
    }

    private void checkServiceAvailability(EfsRequest<MobilityService> serviceInfoRequest, ActivityState state) {
        ResponseEntity<MobilityService> response;

        try {
            response = serviceInfoRequest.go();
        } catch (Exception ex) {
            state.markInactive();
            return;
        }

        if (response == null
                || response.getBody() == null
                || response.getStatusCode().is4xxClientError()
                || response.getStatusCode().is5xxServerError()) {
            state.markInactive();
        } else {
            state.markActive();
        }
    }

}
