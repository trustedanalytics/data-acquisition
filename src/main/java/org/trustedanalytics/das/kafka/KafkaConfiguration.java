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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Lists;

import kafka.admin.AdminUtils;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import com.google.common.base.Preconditions;
import org.trustedanalytics.das.store.BlockingRequestIdQueue;
import static org.trustedanalytics.das.kafka.KafkaConstants.KAFKA_EMBEDED_TYPE;

@Configuration
@ConfigurationProperties(prefix = "kafka")
@Profile("cloud")
public class KafkaConfiguration {
    enum TopicName {
        toRequestsParser, toMetadataParser, toDownloader
    }

    private final static Logger logger = LoggerFactory.getLogger(KafkaConfiguration.class);

    private Map<String, String> producer = new HashMap<>();

    private Map<String, String> consumer = new HashMap<>();

    @Autowired
    private Environment env;

    @Value("${kafka.clusterType}")
    @NotNull
    private String kafkaClusterType;

    @Autowired
    KafkaTopicsProperties topic;

    private KafkaEmbeded kafka;

    @PostConstruct
    public void initialize() throws IOException {
        if (isKafkaEmbededEnabled()) {
            kafka = new KafkaEmbeded();
            kafka.start();
            topic.getTopics().values()
                .forEach(kafka::createTopic);
        }
    }

    private boolean isKafkaEmbededEnabled() {
        logger.info("Kafka cluster type : " + kafkaClusterType);
        return KAFKA_EMBEDED_TYPE.toString().equals(kafkaClusterType);
    }

    @Bean
    @Qualifier("consumerConfig")
    public Properties getConsumerProps() throws IOException {
        if (isKafkaEmbededEnabled()) {
            return kafka.getDefaultConsumerConfig();
        } else
            return getKafkaConsumerConfig();
    }

    private Properties getKafkaConsumerConfig() {
        checkParamsNotNull(consumer, RequiredKafkaConfigurationProperties.ZOOKEEPER_CONNECT.toString(), RequiredKafkaConfigurationProperties.GROUP_ID.toString());
        logger.warn("zookeeper.connect : " + consumer.get(RequiredKafkaConfigurationProperties.ZOOKEEPER_CONNECT.toString()));
        return asProperties(consumer);
    }

    private Properties getKafkaProducerConfig() {
        checkParamsNotNull(producer, RequiredKafkaConfigurationProperties.BROKER_LIST.toString());
        return asProperties(producer);
    }

    private Properties asProperties(Map<String, String> propertiesMap) {
        Properties properties = new Properties();
        properties.putAll(propertiesMap);
        return properties;
    }

    private void checkParamsNotNull(Map<String, String> properties, String... propertyNames) {
        for (String propertyName : propertyNames) {
            Preconditions.checkNotNull(properties.get(propertyName), "Kafka property : "
                    + propertyName + ", cannot be empty");
        }
    }

    @Bean
    @Qualifier("producerConfig")
    public Properties getProducerProps() throws IOException {
        if (isKafkaEmbededEnabled()) {
            return kafka.getDefaultProducerConfig();
        } else
            return getKafkaProducerConfig();
    }

    @Bean
    public BlockingRequestIdQueue toRequestsParser() throws IOException {
        return KafkaRequestIdQueue.newJsonQueue(topic.getTopics().get(TopicName.toRequestsParser.name()), getProducerProps(), getConsumerProps());
    }

    @Bean
    public BlockingRequestIdQueue toDownloader() throws IOException {
        return KafkaRequestIdQueue.newJsonQueue(topic.getTopics().get(TopicName.toDownloader.name()), getProducerProps(), getConsumerProps());
    }

    @Bean
    public BlockingRequestIdQueue toMetadataParser() throws IOException {
        return KafkaRequestIdQueue.newJsonQueue(topic.getTopics().get(TopicName.toMetadataParser.name()), getProducerProps(), getConsumerProps());
    }

    public Map<String, String> getProducer() {
        return producer;
    }

    public void setProducer(Map<String, String> producer) {
        this.producer = producer;
    }

    public Map<String, String> getConsumer() {
        return consumer;
    }

    public void setConsumer(Map<String, String> consumer) {
        this.consumer = consumer;
    }

}
