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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.message.MessageAndMetadata;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.serializer.Decoder;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;

import org.trustedanalytics.das.store.BlockingRequestIdQueue;
import org.trustedanalytics.das.store.QueueItemConsumer;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * TODO: check method shutdown in scala code, what does it do, is autocloseable really needed
 *
 *
 */
public class KafkaRequestIdQueue implements BlockingRequestIdQueue, AutoCloseable {

    private static final int FIRST_ELEMENT_INDEX = 0;

    private static final int TOPIC_COUNT = 1;

    private static final VerifiableProperties emptyProps = new VerifiableProperties();

    private final ConsumerConnector consumer;

    private final String topic;

    private final ConsumerIterator<String, String> streamIterator;

    private Producer<String, String> producer;

    private Decoder<String> msgDecoder;

    private Decoder<String> keyDecoder;

    public KafkaRequestIdQueue(String topic, Properties producerProperties,
                               Properties consumerProperties,
                               Decoder<String> keyDecoder, Decoder<String> msgDecoder) {
        ConsumerConfig consumerConfig = new ConsumerConfig(consumerProperties);
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);
        ProducerConfig producerConfig = new ProducerConfig(producerProperties);
        producer = new Producer<>(producerConfig);
        this.topic = topic;
        this.keyDecoder = keyDecoder;
        this.msgDecoder = msgDecoder;
        streamIterator = getStreamIterator();
    }

    /**
     * Modified example from kafka site with some defensive checks added.
     */
    private ConsumerIterator<String, String> getStreamIterator() {
        Map<String, Integer> topicCountMap = ImmutableMap.of(topic, TOPIC_COUNT);
        Map<String, List<KafkaStream<String, String>>> consumerMap =
                consumer.createMessageStreams(topicCountMap, keyDecoder, msgDecoder);
        List<KafkaStream<String, String>> streams = consumerMap.get(topic);
        Preconditions.checkNotNull(streams, "There is no topic named : " + topic);
        //copy in case of live list returned. Needed for index check below.
        ImmutableList<KafkaStream<String, String>> streamsCopy = ImmutableList.copyOf(streams);

        Preconditions.checkElementIndex(FIRST_ELEMENT_INDEX, streamsCopy.size(),
                "Failed to find any KafkaStreams related to topic : " + topic);
        KafkaStream<String, String> stream = streamsCopy.get(FIRST_ELEMENT_INDEX);

        Preconditions.checkNotNull(stream, "Returned kafka stream is null");

        ConsumerIterator<String, String> iterator = stream.iterator();
        Preconditions.checkNotNull(iterator, "Returned kafka iterator is null");
        return iterator;
    }

    @Override
    public void offer(String requestId) {
        producer.send(new KeyedMessage<>(topic, requestId));
    }


    @Override
    public String take() throws InterruptedException {
        if (streamIterator.hasNext()) {
            MessageAndMetadata<String, String> next = streamIterator.next();
            System.out.print(next);
            return next.message();
        }
        // TODO: check if kafka next.message() can return null, if so we need to change return type
        // to Optional<Request>
        return null;
    }

    @Override
    public void processItem(QueueItemConsumer<String> consumer) throws Exception {
        BlockingRequestIdQueue.super.processItem(consumer);
        // TODO : implement ACK functionality when needed
    }

    /**
     * TODO:
     */
    @Override
    public void close() {
        if (consumer != null) consumer.shutdown();
    }

    public static KafkaRequestIdQueue newJsonQueue(String topic, Properties producerProperties, Properties consumerProperties) {
        return new KafkaRequestIdQueue(topic, producerProperties,
                consumerProperties, new StringDecoder(emptyProps), new StringDecoder(emptyProps));
    }
}

