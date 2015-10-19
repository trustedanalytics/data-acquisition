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
import static org.trustedanalytics.das.parser.Request.State.NEW;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.junit.Test;

public class RequestTest {

    private Request createTestRequest() throws URISyntaxException {
        Request request = new Request();
        request.setCategory("test category");
        request.setId(UUID.randomUUID().toString());
        request.setOrgUUID(UUID.randomUUID().toString());
        request.setSource(new URI("http://example.com"));
        request.setState(Request.State.DOWNLOADED);
        request.setTimestamp(Request.State.DOWNLOADED);
        request.setTitle("test title");
        request.setUserId(12);
        return request;
    }


    @Test
    public void setTimestamp_getTimestamps() throws URISyntaxException {
        Request request = Request.newInstance(0, new URI("http://example.com"));
        request.setTimestamp(NEW);
        
        assertThat(request.getTimestamps(), hasKey(Request.State.NEW));
    }

    @Test
    public void newInstance_testCopy() throws URISyntaxException {
        Request request = createTestRequest();
        Request testRequest = Request.newInstance(request);
        assertEquals(request, testRequest);
    }

    @Test
    public void newInstance_testCopyEmptySource() throws URISyntaxException {
        Request request = createTestRequest();
        request.setSource(null);
        Request testRequest = Request.newInstance(request);
        assertEquals(request, testRequest);
    }
}
