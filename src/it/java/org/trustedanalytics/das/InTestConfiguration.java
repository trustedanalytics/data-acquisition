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
package org.trustedanalytics.das;

import org.trustedanalytics.cloud.auth.AuthTokenRetriever;
import org.trustedanalytics.das.dataflow.FlowManager;
import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.security.authorization.Authorization;
import org.trustedanalytics.das.subservices.callbacks.CallbackUrlListener;
import org.trustedanalytics.das.subservices.downloader.DownloadStatus;
import org.trustedanalytics.das.subservices.downloader.DownloaderClient;
import org.trustedanalytics.das.subservices.metadata.MetadataParser;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("test")
public class InTestConfiguration {

    @Bean
    public BeanPostProcessor authenticationDisabler() {
        return new AuthenticationDisabler();
    }

    @Bean
    public Authorization authorization() throws IOException, ServletException {
        return mock(Authorization.class);
    }

    @Bean
    public DownloaderClient downloaderClient(FlowManager flowManager) {
        return new DummyDownloaderClient(flowManager);
    }

    @Bean
    public MetadataParser metadataParser(FlowManager flowManager) {
        return new DummyMetadataParser(flowManager);
    }

    @Bean
    public AuthTokenRetriever authTokenRetriever() {
        return mock(AuthTokenRetriever.class);
    }

    @Bean
    public String TOKEN() {
        return "fakeToken";
    }

    //FIXME: Instead of mocking DownloaderClient we should rather mock RestTemplate and CallbacksService 
    // to make the test closer to the service boundaries.
    public static class DummyDownloaderClient implements DownloaderClient, CallbackUrlListener {

        private FlowManager flowManager;

        public DummyDownloaderClient(FlowManager flowManager) {
            this.flowManager = flowManager;
        }

        @Override public DownloadStatus download(Request request) {
            request.setIdInObjectStore("test_id_in_store");
            flowManager.requestDownloaded(request);
            return new DownloadStatus();
        }

        @Override public DownloadStatus getStatus(Request request) {
            return new DownloadStatus();
        }

        @Override
        public void setCallbacksUrl(String callbacksUrl) {
        }
    }


    public static class DummyMetadataParser implements MetadataParser, CallbackUrlListener {
        private final FlowManager flowManager;

        public DummyMetadataParser(FlowManager flowManager) {
            this.flowManager = flowManager;
        }

        @Override
        public void processRequest(Request request) {
            flowManager.metadataParsed(request);
        }

        @Override
        public void setCallbacksUrl(String callbacksUrl) {
        }

    }
}
