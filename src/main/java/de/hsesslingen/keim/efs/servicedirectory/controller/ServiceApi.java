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
package de.hsesslingen.keim.efs.servicedirectory.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.hsesslingen.keim.efs.mobility.config.EfsSwaggerApiResponseSupport;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.servicedirectory.core.MobilityServiceRegistry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Collection;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Rest API with CRUD functionalities for Services
 *
 * @author k.sivarasah 11 Sep 2019
 */
@RestController
@RequestMapping(value = "/api/services", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "Service Api")
@EfsSwaggerApiResponseSupport
public class ServiceApi {

    @Autowired
    private MobilityServiceRegistry registry;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get all Services", notes = "Returns all registered services, regardless of their current status (up/down)")
    public Collection<MobilityService> getAll() {
        return registry.getAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get Service by id", notes = "Returns the service with the specific id, regardless of its current status (up/down)")
    public MobilityService getServiceById(@PathVariable String id) {
        return registry.getById(id);
    }

    @ApiIgnore
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MobilityService registerService(@Valid @RequestBody MobilityService service) {
        return registry.register(service);
    }

    @ApiIgnore
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MobilityService updateService(@PathVariable String id, @Valid @RequestBody MobilityService service) {
        return registry.update(id, service);
    }

    @ApiIgnore
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteService(@PathVariable String id) {
        registry.delete(id);
    }
}
