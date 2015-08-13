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
package org.trustedanalytics.das.kafka;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.net.URI;

import kafka.utils.VerifiableProperties;

import org.junit.Test;

import org.trustedanalytics.das.parser.Request;


public class JsonDecoderTest {

    @Test
    public void testFromBytes() throws Exception {
        JsonDecoder jsonDecoder = new JsonDecoder(new VerifiableProperties());
        Request request =
                jsonDecoder.fromBytes("{'userId':1,'source':'http://junit.org','state':'NEW'}"
                        .replaceAll("'", "\"").getBytes());
        assertThat(request, equalTo(Request.newInstance(1, new URI("http://junit.org"))));
    }

}
