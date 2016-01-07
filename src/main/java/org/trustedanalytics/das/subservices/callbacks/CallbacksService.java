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
package org.trustedanalytics.das.subservices.callbacks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import org.trustedanalytics.cloud.auth.AuthTokenRetriever;
import org.trustedanalytics.das.dataflow.FlowManager;
import org.trustedanalytics.das.dataflow.NoSuchRequestInStore;
import org.trustedanalytics.das.helper.RequestIdGenerator;
import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.service.RequestDTO;
import org.trustedanalytics.das.store.RequestStore;
import org.trustedanalytics.das.subservices.downloader.DownloadStatus;
import org.trustedanalytics.das.subservices.metadata.MetadataParseStatus;

@RestController
@RequestMapping("/rest/das/callbacks")
public class CallbacksService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallbacksService.class);

    private static final String RESPONSE_OK = "OK";

    @Autowired
    private FlowManager flowManager;

    @Autowired
    private RequestStore requestStore;

    @Autowired
    private AuthTokenRetriever tokenRetriever;

    @Autowired
    private RequestIdGenerator requestIdGenerator;

    @RequestMapping(value = "/downloader/{id}", method = RequestMethod.POST)
    @ResponseBody
    public String downloaderStatusUpdate(@RequestBody DownloadStatus status,
                                         @PathVariable String id) {
        LOGGER.debug("Update in downloading for {}, status: {}", id, status);
        try {
            switch (status.getState()) {
                case "DONE":
                    Request request = requestStore.get(id)
                            .orElseThrow(() ->new NoSuchRequestInStore("No job with id: "+id));
                    flowManager.requestDownloaded(request.setIdInObjectStore(status.getSavedObjectId()));
                    break;
                case "FAILED":
                    flowManager.requestFailed(id);
                    break;
                default:
                    LOGGER.warn("No action on downloader status update: " + status.getState());
            }
        }
        catch(NoSuchRequestInStore e) {
            throw new HttpMessageNotWritableException(e.getMessage(), e);
        }
        return RESPONSE_OK;
    }

    @RequestMapping(value = "/metadata/{id}", method = RequestMethod.POST)
    public String metadataStatusUpdate(@RequestBody MetadataParseStatus status,
                                       @PathVariable String id) {
        try{
            switch (status.getState()) {
                case DONE:
                    flowManager.metadataParsed(id);
                    break;
                case FAILED:
                    flowManager.requestFailed(id);
                    break;
                default:
                    LOGGER.warn("No action on metadata status update: " + status.getState());
            }
        }
        catch(NoSuchRequestInStore e) {
            throw new HttpMessageNotWritableException(e.getMessage(), e);
        }
        return RESPONSE_OK;
    }

    @RequestMapping(value = "/uploader", method = RequestMethod.POST)
    @ResponseBody
    public String uploaderStatusUpdate(@RequestBody RequestDTO requestDto) {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final String token = tokenRetriever.getAuthToken(auth);

        Request request = new Request.RequestBuilder(requestDto)
                .withToken(token)
                .withId(requestIdGenerator.getId(requestDto.getSource()))
                .build();

        flowManager.requestUploaded(request);

        return RESPONSE_OK;
    }
}
