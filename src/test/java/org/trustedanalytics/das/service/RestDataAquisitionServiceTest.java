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
package org.trustedanalytics.das.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.trustedanalytics.cloud.auth.AuthTokenRetriever;
import org.trustedanalytics.das.dataflow.FlowManager;
import org.trustedanalytics.das.helper.RequestIdGenerator;
import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.security.permissions.PermissionVerifier;
import org.trustedanalytics.das.store.RequestStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class RestDataAquisitionServiceTest {

    @Mock
    private RequestStore requestStore;

    @Mock
    private RequestIdGenerator idGenerator;

    @Mock
    private FlowManager flowManager;

    @Mock
    private AuthTokenRetriever tokenRetriever;

    @Mock
    private HttpServletRequest context;

    @Mock
    PermissionVerifier permissionVerifier;

    @InjectMocks
    private RestDataAcquisitionService service;

    @Test
    public void testAdd() throws Exception {
        String testOrgUUID = UUID.randomUUID().toString();
        Request request = getTestRequest();
        request.setToken("2asdas13");
        request.setOrgUUID(testOrgUUID);
        when(tokenRetriever.getAuthToken(any(Authentication.class))).thenReturn("1231aessa");
        when(idGenerator.getId(request.getSource())).thenReturn("2");

        service.addRequest(request, context);

        Request expected = getTestRequest();
        expected.setId("2");
        expected.setOrgUUID(testOrgUUID);
        verify(requestStore).put(expected);
    }

    private Request getTestRequest() {
        try {
            return Request.newInstance("org", 1, "1", new URI("file:///foo/bar.txt"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetStatus() throws Exception {
        Request request = getTestRequest();
        when(requestStore.get("1")).thenReturn(Optional.of(request));
        Request current = service.getRequest("1", context);
        Request expected = getTestRequest();
        assertThat(current, equalTo(expected));
    }
}
