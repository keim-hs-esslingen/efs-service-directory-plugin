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
    private MobilityServiceFinder finder;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Search for Services", notes = "Searches for available service using the provided search-criteria.")
    public List<MobilityService> search(
            @ApiParam("Returns only this services, that support at least one of the given modes.")
            @RequestParam(required = false, defaultValue = "") Set<Mode> modes,
            //
            @ApiParam("Returns only those services, that support ALL of the given APIs.")
            @RequestParam(required = false, defaultValue = "") Set<API> apis,
            //
            @ApiParam(value = "Defines if inactive services should be excluded from the result list.")
            @RequestParam(required = false, defaultValue = "true") boolean excludeInactive,
            //
            @ApiParam("Returns only services, whose ID is given in this list.")
            @RequestParam(required = false, defaultValue = "") Set<String> serviceIds
    ) {
        return finder.search(modes, apis, excludeInactive, serviceIds);
    }
}
