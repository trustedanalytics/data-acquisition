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

import static org.trustedanalytics.das.parser.Request.State.DOWNLOADED;
import static org.trustedanalytics.das.parser.Request.State.ERROR;
import static org.trustedanalytics.das.parser.Request.State.FINISHED;
import static org.trustedanalytics.das.parser.Request.State.NEW;
import static org.trustedanalytics.das.parser.Request.State.VALIDATED;

import org.trustedanalytics.das.store.BlockingRequestQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import org.trustedanalytics.das.parser.Request;

/**
 * It manages how the data flow in DAS.
 */
public class FlowManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowManager.class);

    private BlockingRequestQueue toRequestParser;
    private BlockingRequestQueue toDownloader;
    private BlockingRequestQueue toMetadataParser;

    public FlowManager(
            BlockingRequestQueue toRequestParser,
            BlockingRequestQueue toDownloader,
            BlockingRequestQueue toMetadataParser) {
        this.toRequestParser = toRequestParser;
        this.toDownloader = toDownloader;
        this.toMetadataParser = toMetadataParser;
    }

    public void newRequest(Request request) {
        request.changeState(NEW);
        LOGGER.debug("newRequest({})", request);
        enqueue(toRequestParser, request);
    }

    public void requestParsed(Request request) {
        request.changeState(VALIDATED);
        LOGGER.debug("requestParsed({})", request);
        enqueue(toDownloader, request);
    }

    public void requestDownloaded(Request request) {
        request.changeState(DOWNLOADED);
        LOGGER.debug("requestDownloaded({})", request);
        enqueue(toMetadataParser, request);
    }

    public void metadataParsed(Request request) {
        request.changeState(FINISHED);
        LOGGER.debug("metadataParsed({})", request);
    }

    public void requestFailed(Request request) {
        request.changeState(ERROR);
        LOGGER.error("requestFailed({})", request);
    }

    private void enqueue(BlockingRequestQueue queue, Request item) {
        try {
            queue.offer(item);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }
}
