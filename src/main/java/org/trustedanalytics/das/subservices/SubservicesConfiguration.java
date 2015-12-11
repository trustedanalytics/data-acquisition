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
package org.trustedanalytics.das.subservices;

import org.trustedanalytics.cloud.auth.AuthTokenRetriever;
import org.trustedanalytics.cloud.auth.OAuth2TokenRetriever;
import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.parser.RequestParsingService;
import org.trustedanalytics.das.store.BlockingRequestIdQueue;
import org.trustedanalytics.das.store.RequestStore;
import org.trustedanalytics.das.subservices.downloader.DownloaderClient;
import org.trustedanalytics.das.subservices.downloader.RestDownloaderClient;
import org.trustedanalytics.das.subservices.metadata.MetadataParser;
import org.trustedanalytics.das.subservices.metadata.RestMetadataParserClient;

import com.google.common.util.concurrent.ServiceManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Resource;

@Configuration
public class SubservicesConfiguration {

    @Value("${services.downloader}")
    private String downloaderUrl;

    @Value("${services.metadataparser}")
    private String metadataParserUrl;

    @Value("${callback.url}")
    private String callbackUrl;

    @Autowired
    private RequestStore requestStore;

    @Autowired
    private RequestParsingService requestParsingService;

    @Resource(name = "toRequestsParser")
    private BlockingRequestIdQueue toRequestsParser;

    @Resource(name = "toDownloader")
    private BlockingRequestIdQueue toDownloader;

    @Resource(name = "toMetadataParser")
    private BlockingRequestIdQueue toMetadataParser;

    @Bean
    public DownloaderClient downloaderClient() {
        return new RestDownloaderClient(new RestTemplate(), downloaderUrl, callbackUrl);
    }

    @Bean
    public RestOperations metadataParserTemplate() {
        return new RestTemplate();
    }

    @Bean
    public MetadataParser metadataParser() {
        return new RestMetadataParserClient(metadataParserTemplate(), metadataParserUrl, callbackUrl);
    }

    @Bean
    public AuthTokenRetriever authTokenRetriever() {
        return new OAuth2TokenRetriever();
    }

    private List<PoolingThreadedService> requestParsingServices() {
        return createServices(
                toRequestsParser, requestParsingService::parseRequest, "parsing", 2);
    }

    private List<PoolingThreadedService> downloadingServices() {
        return createServices(
                toDownloader, downloaderClient()::download, "downloading", 2);
    }

    private List<PoolingThreadedService> metaparsingServices() {
        return createServices(
                toMetadataParser, metadataParser()::processRequest, "metaparsing", 4);
    }

    // TODO: should be wrapped so that "awaitStopped" is called right after stopAsync
    @Bean(initMethod = "startAsync", destroyMethod = "stopAsync")
    public ServiceManager serviceManager() {
        List<PoolingThreadedService> services = new ArrayList<>();
        services.addAll(requestParsingServices());
        services.addAll(downloadingServices());
        services.addAll(metaparsingServices());
        return new ServiceManager(services);
    }

    private List<PoolingThreadedService> createServices(
            BlockingRequestIdQueue queue, Consumer<Request> handler, String name, int threadsCount) {
        List<PoolingThreadedService> services = new ArrayList<>(threadsCount);
        for (int i = 0; i < threadsCount; i++) {
            services.add(new PoolingThreadedService(queue, handler, name, requestStore));
        }
        return services;
    }
}
