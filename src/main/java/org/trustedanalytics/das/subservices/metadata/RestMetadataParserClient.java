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
package org.trustedanalytics.das.subservices.metadata;

import org.trustedanalytics.das.helper.OAuthAuthenticator;
import org.trustedanalytics.das.helper.RestTokenAuthenticator;
import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.subservices.callbacks.CallbackUrlListener;
import org.trustedanalytics.metadata.parser.api.MetadataParseRequest;

import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriTemplate;

import java.util.UUID;

public class RestMetadataParserClient implements MetadataParser, CallbackUrlListener {

    private final RestOperations restTemplate;
    private final String metadataParserUrl;
    private final RestTokenAuthenticator authenticator = new OAuthAuthenticator();
    private String callbacksUrl;
 
    public RestMetadataParserClient(RestOperations restTemplate, String metadataParserUrl, String callbacksUrl) {
        this.restTemplate = restTemplate;
        this.metadataParserUrl = metadataParserUrl;
        this.callbacksUrl = callbacksUrl;
    }

    @Override
    public void processRequest(Request request) {
        authenticator.authenticate(restTemplate, request.getToken());
        restTemplate.postForObject(metadataParserUrl + "/rest/metadata", createRequest(request),
                                   String.class);
    }

    private MetadataParseRequest createRequest(Request request) {
        MetadataParseRequest req = new MetadataParseRequest();
        req.setId(request.getId());
        req.setIdInObjectStore(request.getIdInObjectStore());
        req.setSource(request.getSource());
        req.setTitle(request.getTitle());
        req.setCategory(request.getCategory());
        req.setOrgUUID(UUID.fromString(request.getOrgUUID()));
        req.setPublicRequest(request.isPublicRequest());
        req.setCallbackUrl(new UriTemplate(callbacksUrl).expand("metadata", request.getId()));
        return req;
    }

    @Override
    public void setCallbacksUrl(String callbacksUrl) {
        this.callbacksUrl = callbacksUrl;
    }

}
