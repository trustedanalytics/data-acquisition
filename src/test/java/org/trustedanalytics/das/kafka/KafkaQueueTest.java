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
package org.trustedanalytics.das.kafka;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.trustedanalytics.das.parser.Request;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class KafkaQueueTest {

    private static String TOPIC = "test";

    private Producer<String, Request> producer;
    
    private static KafkaEmbeded kafka = new KafkaEmbeded();

    @BeforeClass
    public static void setup() throws IOException {
    	kafka.start();
    	kafka.createTopic(TOPIC);
    }

    @Before
    public void setupProducer() {
        ProducerConfig pConfig = new ProducerConfig(kafka.getDefaultProducerConfig());
        producer = new Producer<>(pConfig);
    }

    @Test
    public void testConsumer() throws InterruptedException {
        Request expected = Request.newInstance("some_org", 1, "marian", null);
        KafkaRequestQueue queue =
                KafkaRequestQueue.newJsonQueue(TOPIC, kafka.getDefaultProducerConfig(),
                        kafka.getDefaultConsumerConfig());

        queue.offer(expected);
        assertThat(queue.take(), equalTo(expected));
        queue.close();
    }

    @After
    public void shudownProducer() {
        producer.close();
    }

    @AfterClass
    public static void tearDown() throws IOException {
        kafka.shutdown();
    }
}
