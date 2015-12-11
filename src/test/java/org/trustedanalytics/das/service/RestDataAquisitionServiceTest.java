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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.trustedanalytics.cloud.auth.AuthTokenRetriever;
import org.trustedanalytics.das.dataflow.FlowManager;
import org.trustedanalytics.das.helper.RequestIdGenerator;
import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.parser.State;
import org.trustedanalytics.das.security.permissions.PermissionVerifier;
import org.trustedanalytics.das.store.RequestStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

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
    private Function<String, FlowHandler> flowDispatcher;

    @Mock
    PermissionVerifier permissionVerifier;

    @InjectMocks
    private RestDataAcquisitionService service;

    @Before
    public void setUp() {
        when(flowDispatcher.apply(anyString())).thenReturn(new RequestFlowForExistingFile());
        when(flowDispatcher.apply("http")).thenReturn(new RequestFlowForNewFile());
        when(flowDispatcher.apply("https")).thenReturn(new RequestFlowForNewFile());
    }

    @Test
    public void testAdd_whenSourceIsHttpResource_DownloadFile() throws Exception {
        testAdd(getTestHttpRequest());

    }

    @Test
    public void testAdd_whenSourceIsHdfs_SkipDownloadAndNotify() throws Exception {
        testAdd(getTestRequestWithHdfsFile(), State.VALIDATED, r ->
                verify(flowManager).requestDownloaded(r));

    }

    @Test
    public void testAdd_whenSourceIsLocalFile_SkipDownloadAndNotify() throws Exception {
        testAdd(getTestRequestWithLocalFile1(), State.VALIDATED, r ->
                verify(flowManager).requestDownloaded(r));
    }

    @Test
    public void testAdd_whenSourceIsLocalFile2_SkipDownloadAndNotify() throws Exception {
        testAdd(getTestRequestWithLocalFile2(), State.VALIDATED, r ->
                verify(flowManager).requestDownloaded(r));
    }

    @Test
    public void testAdd_whenSourceIsLocalFile3_SkipDownloadAndNotify() throws Exception {
        testAdd(getTestRequestWithLocalFile3(), State.VALIDATED, r ->
                verify(flowManager).requestDownloaded(r));

    }

    private void testAdd(Request.RequestBuilder requestBuilder) throws AccessDeniedException {
        testAdd(requestBuilder, State.NEW, r ->
                verify(flowManager).newRequest(r));
    }

    private void testAdd(Request.RequestBuilder requestBuilder, State expectedState, Consumer<Request> verify) throws AccessDeniedException {
        String testOrgUUID = UUID.randomUUID().toString();
        Request request = requestBuilder
                .withOrgUUID(testOrgUUID)
                .withToken("2asdas13")
                .build();

        Request expected = requestBuilder
                .withState(expectedState)
                .withId("2")
                .withOrgUUID(testOrgUUID)
                .build();

        when(tokenRetriever.getAuthToken(any(Authentication.class))).thenReturn("1231aessa");
        when(idGenerator.getId(request.getSource())).thenReturn("2");

        service.addRequest(request.toDto(), context);
        verify.accept(expected);
    }


    private Request.RequestBuilder getTestHttpRequest() {
        return new Request.RequestBuilder(1, "http://foo/bar.txt")
            .withOrgUUID("org")
            .withId("1");
    }

    private Request.RequestBuilder getTestRequestWithHdfsFile() {
        return new Request.RequestBuilder(1, "hdfs://nameservice1/org/intel/hdfsbroker/userspace/c5378e1f-a35b-4b8b-b800/b519d8c7-2fc0-4842-920b/000000_1")
                .withOrgUUID("org")
                .withId("1");
    }

    private Request.RequestBuilder getTestRequestWithLocalFile1() {
        return new Request.RequestBuilder(1, "file.csv")
                .withOrgUUID("org")
                .withId("1");
    }

    private Request.RequestBuilder getTestRequestWithLocalFile2() {
        return new Request.RequestBuilder(1, "file (1).csv")
                .withOrgUUID("org")
                .withId("1");
    }

    private Request.RequestBuilder getTestRequestWithLocalFile3() {
        return new Request.RequestBuilder(1, "file:///file%20(1).csv")
                .withOrgUUID("org")
                .withId("1");
    }

    @Test
    public void testGetStatus() throws Exception {
        Request request = getTestHttpRequest().build();
        when(requestStore.get("1")).thenReturn(Optional.of(request));
        Request current = new Request.RequestBuilder(service.getRequest("1", context)).build();
        Request expected = getTestHttpRequest().build();
        assertThat(current, equalTo(expected));
    }
}
