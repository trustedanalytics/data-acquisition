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
package org.trustedanalytics.das.dataflow;

import static org.trustedanalytics.das.parser.State.DOWNLOADED;
import static org.trustedanalytics.das.parser.State.ERROR;
import static org.trustedanalytics.das.parser.State.FINISHED;
import static org.trustedanalytics.das.parser.State.NEW;
import static org.trustedanalytics.das.parser.State.VALIDATED;

import org.trustedanalytics.das.parser.State;
import org.trustedanalytics.das.store.BlockingRequestIdQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.store.RequestStore;

/**
 * It manages how the data flow in DAS.
 */
public class FlowManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowManager.class);

    private BlockingRequestIdQueue toRequestParser;
    private BlockingRequestIdQueue toDownloader;
    private BlockingRequestIdQueue toMetadataParser;
    private final RequestStore requestStore;

    public FlowManager(
            BlockingRequestIdQueue toRequestParser,
            BlockingRequestIdQueue toDownloader,
            BlockingRequestIdQueue toMetadataParser,
            RequestStore requestStore) {
        this.toRequestParser = toRequestParser;
        this.toDownloader = toDownloader;
        this.toMetadataParser = toMetadataParser;
        this.requestStore = requestStore;
    }

    public Request newRequest(Request request) {
        return advanceState(request, NEW, toRequestParser, "newRequest({})");
    }

    public Request requestParsed(Request request) {
        return advanceState(request, VALIDATED, toDownloader, "requestParsed({})");
    }

    public Request requestDownloaded(Request request) {
        return advanceState(request, DOWNLOADED, toMetadataParser, "requestDownloaded({})");
    }

    public Request requestUploaded(Request request) {
        Request newRequest = request.changeState(State.NEW);
        Request validatedRequest = newRequest.changeState(State.VALIDATED);
        requestStore.put(validatedRequest);
        return requestDownloaded(validatedRequest);
    }

    public Request metadataParsed(String id) {
        Request metadataParsedRequest = requestStore.get(id)
                .orElseThrow(() -> new NoSuchRequestInStore("No job with id: " + id));
        Request finishedRequest = metadataParsedRequest.changeState(FINISHED);
        requestStore.put(finishedRequest);
        LOGGER.debug("metadataParsed({})", finishedRequest);
        return finishedRequest;
    }

    public Request requestFailed(String id) {
        Request existingRequest = requestStore.get(id)
                .orElseThrow(() -> new NoSuchRequestInStore("No job with id: " + id));
        Request failedRequest = existingRequest.changeState(ERROR);
        requestStore.put(failedRequest);
        LOGGER.error("requestFailed({})", failedRequest);
        return failedRequest;
    }

    private Request advanceState(Request request, State newState, BlockingRequestIdQueue destinationQueue, String debugMsg) {
        Request requestInNewState = request.changeState(newState);
        LOGGER.debug(debugMsg);
        enqueue(destinationQueue, requestInNewState);
        return requestInNewState;
    }

    private void enqueue(BlockingRequestIdQueue queue, Request item) {
        requestStore.put(item);
        LOGGER.info("Adding item to request store {}", item);
        try {
            queue.offer(item.getId());
            LOGGER.info("Added item to queue");
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }
}
