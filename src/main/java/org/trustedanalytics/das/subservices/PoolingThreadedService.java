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

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.store.BlockingRequestIdQueue;

import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.trustedanalytics.das.store.RequestStore;

import static org.trustedanalytics.das.parser.State.ERROR;

/**
 * Executes given procedure for items pulled from queue. It uses single thread.
 * <p>
 * It could (and was before) generic on type data collected from queue
 */
public class PoolingThreadedService extends AbstractExecutionThreadService {

    private BlockingRequestIdQueue queue;
    private Consumer<Request> handler;
    private String name;
    private static final Logger LOGGER = LoggerFactory.getLogger(PoolingThreadedService.class);

    private RequestStore requestStore;

    public PoolingThreadedService(BlockingRequestIdQueue queue, Consumer<Request> handler,
                                  String name, RequestStore requestStore) {
        this.queue = queue;
        this.handler = handler;
        this.name = name;
        this.requestStore = requestStore;
    }

    @Override
    protected void run() throws Exception {
        while (isRunning()) {
            LOGGER.info("Heartbeat from {}", serviceName());
            Optional
                    .ofNullable(queue.take())
                    .flatMap(requestId -> {
                        LOGGER.info("Request id: {}", requestId);
                        final Optional<Request> request = requestStore.get(requestId);
                        LOGGER.info("Redis id: {}", request);
                        if (! request.isPresent()) {
                            LOGGER.warn("Request not found in redis database");
                        }
                        return request;
                    })
                    .ifPresent(request -> {
                            try {
                                LOGGER.info("Processing request: " + request);
                                handler.accept(request);
                            } catch (Exception e) {
                                LOGGER.warn("Error processing request: " + request, e);
                                requestStore.put(request.changeState(ERROR));
                            }
                        });
        }
    }

    @Override
    protected String serviceName() {
        return "QueuePoolingService(" + name + ")";
    }
}
