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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.type.TypeFactory;

import de.hsesslingen.keim.efs.mobility.exception.MiddlewareError;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.service.MobilityService.API;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import de.hsesslingen.keim.efs.servicedirectory.ServiceDirectoryPluginTestApplication;
import java.util.EnumSet;
import java.util.Set;
import org.junit.Before;

/**
 * @author k.sivarasah 4 Oct 2019
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ServiceDirectoryPluginTestApplication.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SearchApiTest extends BaseClassApiTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String SEARCH_URI = "/api/search";

    private static final MobilityService[] SERVICES = new MobilityService[]{
        new MobilityService(
        "legendary-service-1",
        "Legendary Services GmbH",
        null,
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
        null,
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

        for (var service : SERVICES) {
            registry.register(service);
        }
    }

    @Test
    public void searchTest() throws Exception {
        assertNotNull(mockMvc);

        MvcResult result = mockMvc.perform(get(SEARCH_URI).param("active", "false"))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andReturn();

        List<MobilityService> resultServices = getServiceList(result);

        assertNotNull(resultServices);
        assertEquals(2, resultServices.size());
        assertTrue(resultServices.containsAll(registry.getAll()));
    }

    @Test
    public void searchActiveServicesOnlyTest() throws Exception {
        assertNotNull(mockMvc);

        registry.setActive("legendary-service-2", false);

        MvcResult result = mockMvc.perform(get(SEARCH_URI).param("active", "true"))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andReturn();

        List<MobilityService> resultServices = getServiceList(result);

        assertNotNull(resultServices);
        assertEquals(1, resultServices.size());
        assertTrue(resultServices.contains(getServiceFromRegistry("legendary-service-1")));
    }

    @Test
    public void searchActiveServicesByModeTest() throws Exception {
        assertNotNull(mockMvc);

        MvcResult result = mockMvc
                .perform(
                        get(SEARCH_URI).param("active", "true").param("modes", "BICYCLE")
                )
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andReturn();

        TypeFactory typeFactory = mapper.getTypeFactory();
        List<MobilityService> resultServices = mapper.readValue(
                result.getResponse().getContentAsByteArray(),
                typeFactory.constructCollectionLikeType(List.class, MobilityService.class)
        );

        assertNotNull(resultServices);
        assertEquals(1, resultServices.size());
        assertTrue(resultServices.contains(getServiceFromRegistry("legendary-service-2")));
    }

    @Test
    public void searchActiveServicesByModeTest_NoResult() throws Exception {
        assertNotNull(mockMvc);

        MvcResult result = mockMvc.perform(get(SEARCH_URI).param("active", "true").param("modes", "TRAM"))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andReturn();

        List<MobilityService> resultServices = getServiceList(result);

        assertNotNull(resultServices);
        assertTrue(resultServices.isEmpty());
    }

    @Test
    public void searchActiveServicesByModeTest_BadRequest() throws Exception {
        assertNotNull(mockMvc);

        MvcResult result = mockMvc.perform(get(SEARCH_URI).param("active", "true").param("modes", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        MiddlewareError error = mapper.readValue(result.getResponse().getContentAsByteArray(), MiddlewareError.class);
        assertFalse(StringUtils.isEmpty(error.getMessage()));
        assertEquals("400", error.getCode());
    }

    @Test
    public void searchServicesAndFilterByServiceIdTest_EmptyResult() throws Exception {
        assertNotNull(mockMvc);

        registry.setActive("legendary-service-2", false);

        MvcResult result = mockMvc.perform(get(SEARCH_URI).param("serviceIds", "UNKNOWN"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string("[]"))
                .andDo(print())
                .andReturn();

        List<MobilityService> resultServices = getServiceList(result);
        assertTrue(resultServices.isEmpty());
    }

    @Test
    public void searchServicesAndFilterByServiceIdTest_SingleResult() throws Exception {
        assertNotNull(mockMvc);

        registry.setActive("legendary-service-2", false);

        MvcResult result = mockMvc.perform(get(SEARCH_URI).param("serviceIds", "legendary-service-1"))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andReturn();

        List<MobilityService> resultServices = getServiceList(result);
        assertNotNull(resultServices);
        assertEquals(1, resultServices.size());
        assertTrue(resultServices.contains(getServiceFromRegistry("legendary-service-1")));
    }

}
