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
import java.net.URISyntaxException;

import org.apache.commons.lang3.RandomStringUtils;
import org.trustedanalytics.das.helper.RandomUUIDRequestIdGenerator;
import org.trustedanalytics.das.helper.RequestIdGenerator;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class KafkaQueueTest {

    private static String TOPIC = "test";

    private RequestIdGenerator generator;

    private Producer<String, String> producer;

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
        generator = new RandomUUIDRequestIdGenerator();
    }

    @Test
    public void testConsumer() throws InterruptedException, URISyntaxException {
        String expectedId = generator.getId(generateRandomUri());
        KafkaRequestIdQueue queue =
                KafkaRequestIdQueue.newJsonQueue(TOPIC, kafka.getDefaultProducerConfig(),
                        kafka.getDefaultConsumerConfig());

        queue.offer(expectedId);
        assertThat(queue.take(), equalTo(expectedId));
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

    private String generateRandomUri() {
        return "http://" + RandomStringUtils.randomAlphanumeric(10) + ".com";
    }
}
