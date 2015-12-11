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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.store.BlockingRequestIdQueue;

/**
 * 
 * 
 *
 */
public class BlockingMemoryMultiTopicQueueTest {

    @Before
    public void init() {}

    @Test
    public void testOffer() throws Exception {
        BlockingRequestIdQueue queue = new BlockingMemoryMultiTopicRequestIdQueue();

        queue.offer("request#1");
        String requestId = queue.take();
        assertThat(requestId, equalTo("request#1"));
    }

    @Test
    public void testSeparation() throws URISyntaxException, InterruptedException {
        BlockingRequestIdQueue topic1 = new BlockingMemoryMultiTopicRequestIdQueue();
        BlockingRequestIdQueue topic2 = new BlockingMemoryMultiTopicRequestIdQueue();

        topic1.offer("request#1");
        topic2.offer("request#2");

        assertThat(topic1.take(), equalTo("request#1"));
        assertThat(topic2.take(), equalTo("request#2"));
    }

    /**
     * What I wanted to test here is if take() method will block. It is a little bit risky, because
     * if implementation of take() will change to non blocking, InterruptedException will be thrown
     * from the first test that will call a blocking method.
     * 
     * TODO: check JCiP for proper way and refactor
     * 
     * @throws URISyntaxException
     * @throws InterruptedException
     */
    @Test(expected = InterruptedException.class)
    public void testBlocking() throws URISyntaxException, InterruptedException {
        Thread.currentThread().interrupt();
        BlockingRequestIdQueue topic1 = new BlockingMemoryMultiTopicRequestIdQueue();
        topic1.take();
    }

}
