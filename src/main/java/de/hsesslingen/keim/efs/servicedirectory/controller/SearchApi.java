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

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.hsesslingen.keim.efs.mobility.config.EfsSwaggerApiResponseSupport;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.service.MobilityService.API;
import de.hsesslingen.keim.efs.mobility.service.MobilityType;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import de.hsesslingen.keim.efs.servicedirectory.core.MobilityServiceFinder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Rest API to search for Services
 *
 * @author k.sivarasah 11 Sep 2019
 */
@RestController
@RequestMapping("/api/search")
@Api(tags = "Search Api")
@EfsSwaggerApiResponseSupport
public class SearchApi {

    @Autowired
    MobilityServiceFinder finder;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Search for Services", notes = "Searches for available service using the provided search-criteria "
            + "(only active service, services providing certain modes and/or mobility types)")
    public List<MobilityService> search(
            @RequestParam(required = false, defaultValue = "") Set<MobilityType> mobilityTypes,
            @RequestParam(required = false, defaultValue = "") Set<Mode> modes,
            @RequestParam(required = false, defaultValue = "") Set<String> serviceIds,
            @RequestParam(required = false, defaultValue = "") Set<API> apis,
            @RequestParam(required = false, defaultValue = "true")
            @ApiParam(value = "true - searches only for active services. false - searches for services regardless of its active status") boolean active
    ) {

        return finder.search(mobilityTypes, modes, serviceIds, apis, active);
    }
}
