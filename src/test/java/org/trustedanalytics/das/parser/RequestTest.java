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

import static org.trustedanalytics.das.parser.Request.State.NEW;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public class RequestTest {

    @Test public void setTimestamp_getTimestamps() throws URISyntaxException {
        Request request = Request.newInstance(0, new URI("http://example.com"));
        request.setTimestamp(NEW);
        
        assertThat(request.getTimestamps(), hasKey(Request.State.NEW));
    }
}
