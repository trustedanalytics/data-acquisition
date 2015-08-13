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
import org.trustedanalytics.das.store.BlockingRequestQueue;

import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import static org.trustedanalytics.das.parser.Request.State.ERROR;

/**
 * Executes given procedure for items pulled from queue. It uses single thread.
 *
 * It could (and was before) generic on type data collected from queue
 *
 */
public class PoolingThreadedService extends AbstractExecutionThreadService {

    private BlockingRequestQueue queue;
    private Consumer<Request> handler;
    private String name;
    private static final Logger LOGGER = LoggerFactory.getLogger(PoolingThreadedService.class);

    public PoolingThreadedService(BlockingRequestQueue queue, Consumer<Request> handler,
                                  String name) {
        this.queue = queue;
        this.handler = handler;
        this.name = name;
    }

    @Override protected void run() throws Exception {
        while (isRunning()) {
            Optional
                    .ofNullable(queue.take())
                    .ifPresent(item -> {
                        try {
                            handler.accept(item);
                        } catch (Exception e) {
                            LOGGER.warn("Error processing request: " + item, e);
                            item.changeState(ERROR);
                        }
                    });
        }
    }

    @Override protected String serviceName() {
        return "QueuePoolingService(" + name + ")";
    }
}
