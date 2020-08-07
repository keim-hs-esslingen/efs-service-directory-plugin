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

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.servicedirectory.core.MobilityServiceRegistry;

/**
 * @author k.sivarasah 5 Oct 2019
 */
public class BaseClassApiTest {

    @Autowired
    MobilityServiceRegistry registry;
    
    @Autowired
    ObjectMapper mapper;
    
    protected List<MobilityService> getServiceList(MvcResult result) throws JsonParseException, JsonMappingException, IOException {
        TypeFactory typeFactory = mapper.getTypeFactory();
        return mapper.readValue(result.getResponse().getContentAsByteArray(), typeFactory.constructCollectionLikeType(List.class, MobilityService.class));
    }

    protected MobilityService getServiceFromRegistry(String id) {
        for (MobilityService service : registry.getAll()) {
            if (id.equals(service.getId())) {
                return service;
            }
        }
        return null;
    }

    protected String toJsonString(final Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    protected MobilityService copy(final MobilityService obj) {
        try {
            return mapper.readValue(mapper.writeValueAsString(obj), MobilityService.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

}
