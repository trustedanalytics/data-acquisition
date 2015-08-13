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
package org.trustedanalytics.das.store.memory;

import java.util.concurrent.ArrayBlockingQueue;

import org.springframework.stereotype.Repository;

import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.store.BlockingRequestQueue;

/**
 * Default simple implementation. For testing only. Ack doesn't check anything, always removes given
 * element from queue.
 *
 */
@Repository
public class BlockingMemoryMultiTopicRequestQueue implements BlockingRequestQueue {

    private final ArrayBlockingQueue<Request> queue = new ArrayBlockingQueue<>(10);

    BlockingMemoryMultiTopicRequestQueue() {
    }

    @Override
    public void offer(Request t) {
        queue.offer(t);
    }

    @Override
    public Request take() throws InterruptedException {
        return queue.take();
    }

}
