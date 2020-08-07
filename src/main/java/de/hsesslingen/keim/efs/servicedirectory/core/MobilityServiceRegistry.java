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
import org.springframework.stereotype.Component;

import de.hsesslingen.keim.efs.mobility.exception.ResourceNotFoundException;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class with CRUD functionalities for {@link MobilityService} Currently
 * a list of registered services are load by configuration properties. Therefore
 * the services cannot not be deleted or updated.
 *
 * @author k.sivarasah 12 Sep 2019
 */
@Component
public class MobilityServiceRegistry {

    private static final Log log = LogFactory.getLog(MobilityServiceRegistry.class);

    private Map<String, MobilityService> services = new HashMap<>();

    /**
     * Returns a collection of services that are registered to the service
     * directory
     *
     * @return Collection of {@link MobilityService}s
     */
    public Collection<MobilityService> getAll() {
        return services.values();
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
            throw new ResourceNotFoundException(String.format("Service with id [%s] not found", id));
        }

        return service;
    }

    /**
     * Registeres the provided service in the registry.
     *
     * @param service
     * @return
     */
    public MobilityService register(MobilityService service) {
        log.info("Registering new mobility service...");

        var id = service.getId();

        if (StringUtils.isBlank(id)) {
            throw badRequest("The provided service does not have an id.");
        }

        services.put(id, service);

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
        log.info("Updating registered service with id " + id);

        if (!services.containsKey(id)) {
            throw new ResourceNotFoundException(String.format("Service with id [%s] not found", id));
        }

        service.setId(id);
        services.put(id, service);

        return service;
    }

    /**
     * Deletes the services with the given id.
     *
     * @param id
     */
    public void delete(String id) {
        log.info("Deleting service with id " + id);
        services.remove(id);
    }

    /**
     * Deletes all registered services. Useful for testing.
     */
    public void deleteAll() {
        log.info("Deleting all registered services...");
        services.clear();
    }

}
