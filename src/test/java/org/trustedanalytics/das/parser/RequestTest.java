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
package org.trustedanalytics.das.parser;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.junit.Test;

public class RequestTest {

    private Request createTestRequest(String source) throws URISyntaxException {
        return new Request.RequestBuilder(12, source)
            .withCategory("test category")
            .withId(UUID.randomUUID().toString())
            .withOrgUUID(UUID.randomUUID().toString())
            .withState(State.DOWNLOADED)
            .withTitle("test title")
            .build();
    }

    @Test
    public void setTimestamp_getTimestamps() throws URISyntaxException {
        Request request =  new Request.RequestBuilder(0, "http://example.com").build();
        Request withNewTimestamp = request.setCurrentTimestamp(State.NEW);
        
        assertThat(withNewTimestamp.getTimestamps(), hasKey(State.NEW));
    }

    @Test
    public void newInstance_testCopy() throws URISyntaxException {
        Request request = createTestRequest("http://example.com");
        Request testRequest =  new Request.RequestBuilder(request).build();
        assertEquals(request, testRequest);
    }

    @Test
    public void newInstance_testCopyEmptySource() throws URISyntaxException {
        Request request = createTestRequest(null);

        Request testRequest = new Request.RequestBuilder(request).build();
        assertEquals(request, testRequest);
    }
}
