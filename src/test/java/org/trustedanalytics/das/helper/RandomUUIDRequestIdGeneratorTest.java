/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.das.helper;

import static org.junit.Assert.assertFalse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;


public class RandomUUIDRequestIdGeneratorTest {

    private static final int REPEAT_COUNT = 100;

    private RequestIdGenerator generator;

    @Before
    public void init() {
        generator = new RandomUUIDRequestIdGenerator();
    }

    @Test
    public void testUniquenessGetId() throws Exception {
        Set<String> ids = new HashSet<>(REPEAT_COUNT);
        for (int i = 0; i < REPEAT_COUNT; i++) {
            String id = generator.getId(generateRandomUri());
            assertFalse("Collision in IDs generated for random URIs", ids.contains(id));
            ids.add(id);
        }
    }

    @Test
    public void testUniquenessForTheSameInput() throws Exception {
        Set<String> ids = new HashSet<>(REPEAT_COUNT);
        String someUri = generateRandomUri();
        for (int i = 0; i < REPEAT_COUNT; i++) {
            String id = generator.getId(someUri);
            assertFalse("Collision in IDs generated for the same URIs", ids.contains(id));
            ids.add(id);
        }
    }

    private String generateRandomUri() {
        return "http://" + RandomStringUtils.randomAlphanumeric(10) + ".com";
    }
}
