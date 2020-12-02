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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.StringUtils;

import de.hsesslingen.keim.efs.mobility.exception.EfsError;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.service.MobilityService.API;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import de.hsesslingen.keim.efs.servicedirectory.ServiceDirectoryPluginTestApplication;
import java.util.EnumSet;
import java.util.Set;
import org.junit.Before;

/**
 * @author k.sivarasah 5 Oct 2019
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ServiceDirectoryPluginTestApplication.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ServiceApiTest extends BaseClassApiTest {

    @Autowired
    MockMvc mockMvc;

    private static final String serviceUri = "/api/services";
    private static final String serviceWithIdUri = "/api/services/{id}";
    private static final String serviceId1 = "legendary-service-1";

    private static MobilityService[] services = new MobilityService[]{
        new MobilityService(
        "legendary-service-1",
        "Legendary Services GmbH",
        "Legendary Service 1",
        "http://legendary-service-1/",
        "Entire Spacetime-continuum",
        Set.of(Mode.CAR),
        EnumSet.allOf(API.class),
        null
        ),
        new MobilityService(
        "legendary-service-2",
        "Legendary Services GmbH",
        "Legendary Service 2",
        "http://legendary-service-2/",
        "Entire Spacetime-continuum",
        Set.of(Mode.BICYCLE),
        EnumSet.allOf(API.class),
        null
        )
    };

    @Before
    public void prepare() {
        registry.deleteAll();

        for (var service : services) {
            registry.register(service);
        }
    }

    @Test
    public void getAllServicesTest() throws Exception {
        MvcResult result = mockMvc.perform(get(serviceUri))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andReturn();

        List<MobilityService> resultServices = getServiceList(result);
        assertNotNull(resultServices);
        assertEquals(2, resultServices.size());
        assertTrue(resultServices.containsAll(registry.getAll()));
    }

    @Test
    public void getServiceByIdTest() throws Exception {
        MvcResult result = mockMvc.perform(get(serviceWithIdUri, serviceId1))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andReturn();

        MobilityService service = mapper.readValue(result.getResponse().getContentAsByteArray(), MobilityService.class);
        assertNotNull(service);
        assertEquals(getServiceFromRegistry(serviceId1), service);
    }

    @Test
    public void getServiceByIdTest_404() throws Exception {
        MvcResult result = mockMvc.perform(get(serviceWithIdUri, "unknown_service_id"))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();

        EfsError error = mapper.readValue(result.getResponse().getContentAsByteArray(), EfsError.class);
        assertFalse(StringUtils.isEmpty(error.getMessage()));
        assertEquals(404, error.getCode());
    }

    @Test
    public void registerNewServiceTest() throws Exception {
        MvcResult result = mockMvc.perform(post(serviceUri)
                .content(toJsonString(getServiceFromRegistry(serviceId1)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andReturn();

        MobilityService service = mapper.readValue(
                result.getResponse().getContentAsByteArray(), MobilityService.class
        );
        assertNotNull(service);
        assertEquals(getServiceFromRegistry(serviceId1), service);
    }

    @Test
    public void registerNewServiceTest_400() throws Exception {
        MobilityService service = copy(getServiceFromRegistry(serviceId1)).setServiceUrl(null);
        mockMvc.perform(post(serviceUri)
                .content(toJsonString(service))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("serviceUrl")))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void updateServiceTest() throws Exception {
        MvcResult result = mockMvc.perform(put(serviceWithIdUri, serviceId1)
                .content(toJsonString(getServiceFromRegistry(serviceId1)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andReturn();

        MobilityService service = mapper.readValue(
                result.getResponse().getContentAsByteArray(), MobilityService.class
        );
        assertNotNull(service);
        assertEquals(getServiceFromRegistry(serviceId1), service);
    }
}
