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

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.store.BlockingRequestQueue;

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
        BlockingRequestQueue queue = new BlockingMemoryMultiTopicRequestQueue();

        queue.offer(Request.newInstance(1, "request#1", "file:///foo/bar"));
        Request request = queue.take();
        assertThat(request.getId(), equalTo("request#1"));
    }

    @Test
    public void testSeparation() throws URISyntaxException, InterruptedException {
        BlockingRequestQueue topic1 = new BlockingMemoryMultiTopicRequestQueue();
        BlockingRequestQueue topic2 = new BlockingMemoryMultiTopicRequestQueue();

        topic1.offer(Request.newInstance(1, "request#1", "file:///foo/bar"));
        topic2.offer(Request.newInstance(1, "request#2", "file:///foo/bar"));

        assertThat(topic1.take().getId(), equalTo("request#1"));
        assertThat(topic2.take().getId(), equalTo("request#2"));
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
        BlockingRequestQueue topic1 = new BlockingMemoryMultiTopicRequestQueue();
        topic1.take();
    }

}
