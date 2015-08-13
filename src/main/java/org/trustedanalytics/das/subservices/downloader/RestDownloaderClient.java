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
package org.trustedanalytics.das.subservices.downloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import org.trustedanalytics.das.helper.OAuthAuthenticator;
import org.trustedanalytics.das.helper.RestTokenAuthenticator;
import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.subservices.callbacks.CallbackUrlListener;

public class RestDownloaderClient implements DownloaderClient, CallbackUrlListener {

    private final RestTemplate restTemplate;
    private final String putRequestUrl;
    private final String getRequestStatusUrl;
    private final RestTokenAuthenticator authenticator = new OAuthAuthenticator();
    private String callbacksUrl;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestDownloaderClient.class);

    public RestDownloaderClient(
        RestTemplate restTemplate,
        String downloaderServiceUrl,
        String callbacksUrl) {
        this.restTemplate = restTemplate;
        putRequestUrl = ensureTrailingSlash(downloaderServiceUrl) + "rest/downloader/requests";
        getRequestStatusUrl = putRequestUrl + "/{id}";
        this.callbacksUrl = callbacksUrl;
        LOGGER.debug("downloader uri: {}", putRequestUrl);
    }

    private String ensureTrailingSlash(String url) {
        if (url.endsWith("/")) {
            return url;
        }
        return url + "/";
    }

    @Override
    public DownloadStatus download(Request request) {
        authenticator.authenticate(restTemplate, request.getToken());

        DownloadRequest downloadRequest = new DownloadRequest();
        downloadRequest.setSource(request.getSource().toString());
        downloadRequest.setCallback(new UriTemplate(callbacksUrl).
                expand("downloader", request.getId()).toString());

        return restTemplate.postForObject(putRequestUrl, downloadRequest, DownloadStatus.class);
    }

    @Override
    public DownloadStatus getStatus(Request request) {
        authenticator.authenticate(restTemplate, request.getToken());

        return restTemplate.getForObject(getRequestStatusUrl, DownloadStatus.class,
            request.getId());
    }

    @Override
    public void setCallbacksUrl(String callbacksUrl) {
        this.callbacksUrl = callbacksUrl;
    }
}
