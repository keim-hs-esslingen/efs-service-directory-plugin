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

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.service.MobilityType;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import de.hsesslingen.keim.efs.servicedirectory.ServiceDirectoryPluginTestApplication;
import static org.junit.Assert.assertTrue;
import org.junit.Before;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ServiceDirectoryPluginTestApplication.class})
@ActiveProfiles("test")
public class MobilityServiceFinderTest {

    @Autowired
    MobilityServiceFinder finder;

    @Autowired
    MobilityServiceRegistry registry;

    private static MobilityService[] services = new MobilityService[]{
        new MobilityService(
        "legendary-service-1",
        "Legendary Services GmbH",
        "Legendary Service 1",
        "http://legendary-service-1/",
        "Entire Spacetime-continuum",
        Set.of(MobilityType.FREE_RIDE),
        Set.of(Mode.CAR)
        ),
        new MobilityService(
        "legendary-service-2",
        "Legendary Services GmbH",
        "Legendary Service 2",
        "http://legendary-service-2/",
        "Entire Spacetime-continuum",
        Set.of(MobilityType.RIDE_HAILING),
        Set.of(Mode.BICYCLE)
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
    public void searchTest() {
        var modes = Set.of(Mode.CAR);
        assertEquals(1, finder.search(null, modes, null, false).size());

        modes = Set.of(Mode.CAR, Mode.BUS);
        assertEquals(1, finder.search(null, modes, null, false).size());

        assertEquals(1, finder.search(null, modes, null, true).size());

        registry.setActive("legendary-service-1", false);
        assertEquals(0, finder.search(null, modes, null, true).size());
    }

    @Test
    public void searchByModesTest() {
        var modes = Set.of(Mode.BICYCLE);
        List<MobilityService> result;

        assertEquals(1, finder.searchByModes(modes).size());

        assertEquals(2, (result = finder.searchByModes(null)).size());
        assertTrue(result.containsAll(registry.getAll()));

        modes = Set.of(Mode.BICYCLE, Mode.CAR);
        assertEquals(2, (result = finder.searchByModes(modes)).size());
        assertTrue(result.containsAll(registry.getAll()));

        assertEquals(2, finder.searchByModes(null).size());

        assertEquals(0, finder.searchByModes(Set.of(Mode.CABLE_CAR)).size());
    }

    @Test
    public void searchByMobilityTypeTest() {
        var mTypes = Set.of(MobilityType.FREE_RIDE);
        List<MobilityService> result;

        assertEquals(1, finder.searchByMobilityType(mTypes).size());

        assertEquals(2, (result = finder.searchByMobilityType(null)).size());
        assertTrue(result.containsAll(registry.getAll()));

        mTypes = Set.of(MobilityType.FREE_RIDE, MobilityType.RIDE_HAILING);
        assertEquals(2, (result = finder.searchByMobilityType(mTypes)).size());
        assertTrue(result.containsAll(registry.getAll()));

        assertEquals(0, finder.searchByMobilityType(Set.of(MobilityType.FLIGHT)).size());
    }
}
