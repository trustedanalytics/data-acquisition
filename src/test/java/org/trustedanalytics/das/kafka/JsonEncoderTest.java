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

import org.trustedanalytics.das.parser.Request;
import kafka.utils.VerifiableProperties;
import org.junit.Test;

import java.net.URI;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class JsonEncoderTest {

    @Test
    public void testToBytes() throws Exception {
        JsonEncoder jsonEncoder = new JsonEncoder(new VerifiableProperties());
        byte[] bytes = jsonEncoder.toBytes(Request.newInstance(1, new URI("http://junit.org")));
        String expected =
                "{'userId':1,'source':'http://junit.org','state':'NEW','timestamps':{},'publicRequest':false}".replaceAll("'", "\"");
        assertThat(new String(bytes), equalTo(expected));

    }
}
