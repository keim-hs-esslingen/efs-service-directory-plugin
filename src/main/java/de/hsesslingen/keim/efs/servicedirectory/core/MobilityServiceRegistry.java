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

import static de.hsesslingen.keim.efs.mobility.exception.HttpException.*;

import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import static org.apache.commons.lang3.StringUtils.isBlank;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Utility class with CRUD functionalities for {@link MobilityService} Currently
 * a list of registered services are load by configuration properties. Therefore
 * the services cannot not be deleted or updated.
 *
 * @author k.sivarasah 12 Sep 2019
 */
@Service
public class MobilityServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(MobilityServiceRegistry.class);

    private final Map<String, MobilityService> services = new HashMap<>();
    private final Map<String, ActivityState> activityStateMap = new HashMap();

    /**
     * Returns a collection of services that are registered in the service
     * directory
     *
     * @return Collection of {@link MobilityService}s
     */
    public Collection<MobilityService> getAll() {
        return services.values();
    }

    /**
     * Returns a collection of services that are registered in the service
     * directory
     *
     * @return Collection of {@link MobilityService}s
     */
    public Stream<MobilityService> streamAll() {
        return services.values().stream();
    }

    /**
     * Returns a stream of services that are registered in the service directory
     *
     * @param excludeInactive Whether inactive services should be excluded right
     * away.
     * @return Stream of {@link MobilityService}s
     */
    public Stream<MobilityService> streamAll(boolean excludeInactive) {
        if (excludeInactive) {
            return streamAll().filter(s -> isActive(s.getId()));
        } else {
            return streamAll();
        }
    }

    /**
     * Creates a stream of pairs of services and their current activity states.
     *
     * @return
     */
    public Stream<Pair<MobilityService, ActivityState>> streamServiceStates() {
        return streamAll().map(s -> Pair.of(s, getServiceState(s.getId())));
    }

    /**
     * Gets the current activity state of the given service. If the service is
     * not registered, this method returns null.
     *
     * @param serviceId
     * @return
     */
    public ActivityState getServiceState(String serviceId) {
        return activityStateMap.get(serviceId);
    }

    /**
     * Returns the {@link MobilityService} with the specified id
     *
     * @param id Unique identifier of a service
     * @return The MobilityService
     */
    public MobilityService getById(String id) {
        var service = services.get(id);

        if (service == null) {
            throw notFound("Service with id [%s] not found", id);
        }

        return service;
    }

    /**
     * Tests whether the service with the given id is marked as active.
     *
     * @param serviceId
     * @return
     */
    public boolean isActive(String serviceId) {
        var state = activityStateMap.get(serviceId);
        return state == null ? false : state.isActive();
    }

    /**
     * Sets the "active" value for the given service to true, if such a service
     * is registered.
     *
     * @param serviceId
     * @param value
     */
    public void setActive(String serviceId, boolean value) {
        if (activityStateMap.containsKey(serviceId)) {
            activityStateMap.get(serviceId).setActive(value);
        }
    }

    /**
     * Marks the service with the given id as active, if such a service is
     * registered.
     *
     * @param serviceId
     */
    public void markActive(String serviceId) {
        if (services.containsKey(serviceId)) {
            activityStateMap.get(serviceId).markActive();
        }
    }

    /**
     * Marks the service with the given id as inactive, if such a service is
     * registered.
     *
     * @param serviceId
     */
    public void markInactive(String serviceId) {
        if (services.containsKey(serviceId)) {
            activityStateMap.get(serviceId).markInactive();
        }
    }

    /**
     * Registeres the provided service in the registry.
     *
     * @param service
     * @return
     */
    public MobilityService register(MobilityService service) {
        logger.info("Registering new mobility service...");

        var id = service.getId();

        if (isBlank(id)) {
            throw badRequest("The provided service does not have an id.");
        }

        services.put(id, service);
        activityStateMap.put(id, ActivityState.active());

        return service;
    }

    /**
     * Updates the provided service in the registry.
     *
     * @param id
     * @param service
     * @return
     */
    public MobilityService update(String id, MobilityService service) {
        logger.info("Updating registered service with id " + id);

        if (!services.containsKey(id)) {
            throw notFound("Service with id [%s] not found", id);
        }

        service.setId(id);
        services.put(id, service);
        activityStateMap.get(id).markActive();

        return service;
    }

    /**
     * Deletes the services with the given id.
     *
     * @param id
     */
    public void delete(String id) {
        logger.info("Deleting service with id " + id);
        services.remove(id);
        activityStateMap.remove(id);
    }

    /**
     * Deletes all registered services. Useful for testing.
     */
    public void deleteAll() {
        logger.info("Deleting all registered services...");
        services.clear();
        activityStateMap.clear();
    }

    public static class ActivityState {

        private boolean active;
        private Instant lastUpdate;

        public ActivityState(boolean active) {
            this.active = active;
            this.lastUpdate = Instant.now();
        }

        public ActivityState setActive(boolean value) {
            this.active = value;
            this.lastUpdate = Instant.now();
            return this;
        }

        public ActivityState markInactive() {
            setActive(false);
            return this;
        }

        public ActivityState markActive() {
            setActive(true);
            return this;
        }

        public boolean isActive() {
            return active;
        }

        public Instant getLastUpdate() {
            return lastUpdate;
        }

        public static ActivityState active() {
            return new ActivityState(true);
        }

        public static ActivityState inactive() {
            return new ActivityState(false);
        }
    }

}
