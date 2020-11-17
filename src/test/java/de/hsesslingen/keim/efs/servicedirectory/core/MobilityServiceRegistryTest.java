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

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.service.MobilityService.API;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import de.hsesslingen.keim.efs.servicedirectory.ServiceDirectoryPluginTestApplication;
import java.util.EnumSet;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ServiceDirectoryPluginTestApplication.class})
@ActiveProfiles("test")
public class MobilityServiceRegistryTest {

    @Autowired
    private MobilityServiceRegistry registry;

    private static MobilityService[] services = new MobilityService[]{
        new MobilityService(
        "legendary-service-1",
        "Legendary Services GmbH",
        "Legendary Service 1",
        "http://legendary-service-1/",
        "Entire Spacetime-continuum",
        Set.of(Mode.CAR),
        EnumSet.allOf(API.class)
        ),
        new MobilityService(
        "legendary-service-2",
        "Legendary Services GmbH",
        "Legendary Service 2",
        "http://legendary-service-2/",
        "Entire Spacetime-continuum",
        Set.of(Mode.BICYCLE),
        EnumSet.allOf(API.class)
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
    public void getAllServicesTest() {
        var result = registry.getAll();
        assertEquals(services.length, result.size());

        for (var service : services) {
            var rs = result.stream().filter(s -> s.equals(service)).findAny().get();
            assertEquals(service, rs);
        }
    }

    @Test
    public void getServiceByIdTest() {
        MobilityService actualService = registry.getById("legendary-service-1");
        assertNotNull(actualService);
        assertEquals(services[0], actualService);
    }
}
