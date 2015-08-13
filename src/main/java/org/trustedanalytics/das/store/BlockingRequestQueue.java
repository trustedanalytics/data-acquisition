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
package org.trustedanalytics.das.store;

import org.trustedanalytics.das.parser.Request;

/**
 * Approximation of publish/subscribe interface
 * 
 */
public interface BlockingRequestQueue {

    /**
     * Adds element to queue
     * @param item Item to add
     */
    void offer(Request item);

    /**
     * Retrieves element with removing it, or block if queue is empty
     * @return Request element
     * @throws InterruptedException if interrupted while waiting
     */
    Request take() throws InterruptedException;

    default void processItem(QueueItemConsumer<Request> consumer) throws Exception {
        Request request = take();
        consumer.consumeItem(request);
    }

    // Just as an example - probably shouldn't be here
    default void processItemDontRemoveOnError(QueueItemConsumer<Request> consumer) throws Exception {
        Request request = take();
        try {
            consumer.consumeItem(request);
        } catch (Exception ex) {
            offer(request);
            throw ex;
        }
    }
}
