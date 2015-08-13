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
import java.net.ServerSocket;
import java.util.List;
import java.util.Properties;

import kafka.admin.AdminUtils;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.MockTime;
import kafka.utils.TestUtils;
import kafka.utils.TestZKUtils;
import kafka.utils.Time;
import kafka.utils.ZKStringSerializer$;
import kafka.zk.EmbeddedZookeeper;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class KafkaEmbeded {

    private final static Logger logger = LoggerFactory.getLogger(KafkaEmbeded.class);

    private static final Properties EMPTY_PROPS = new Properties();

    private static final String LOCALHOST = "127.0.0.1";

    private static final String DEFAULT_GROUP = "group1";

    private static final int BROKER_ID = 0;

    private int brokerPort;

    private ZkClient zkClient;

    private KafkaServer kafkaServer;

    private EmbeddedZookeeper zkServer;

    private ServerSocket socket;

    public void start() throws IOException {

        String zkConnect = TestZKUtils.zookeeperConnect();
        zkServer = new EmbeddedZookeeper(zkConnect);
        zkClient =
                new ZkClient(zkServer.connectString(), 30000, 30000, ZKStringSerializer$.MODULE$);
        brokerPort = TestUtils.choosePort();
        logger.info("{}", brokerPort);;
        Properties props = TestUtils.createBrokerConfig(BROKER_ID, brokerPort);
        // props.setProperty("zookeeper.connect", String.valueOf(zkPort));
        props.setProperty("zookeeper.session.timeout.ms", "30000");
        props.setProperty("zookeeper.connection.timeout.ms", "30000");
        logger.info("{}", props);
        KafkaConfig config = new KafkaConfig(props);
        Time mock = new MockTime();
        kafkaServer = TestUtils.createServer(config, mock);
    }

    public void createTopic(String topic) {
        logger.debug("Adding kafka topic : " + topic);
        AdminUtils.createTopic(zkClient, topic, 1, 1, EMPTY_PROPS);
        List<KafkaServer> servers = Lists.newArrayList(kafkaServer);
        TestUtils.waitUntilMetadataIsPropagated(scala.collection.JavaConversions.asBuffer(servers),
                topic, 0, 5000);

    }

    public void shutdown() throws IOException {
        if (kafkaServer != null) kafkaServer.shutdown();
        if (zkClient != null) zkClient.close();
        if (zkServer != null) zkServer.shutdown();
        if (socket != null) socket.close();
    }

    public String getBrokerAddr() {
        return LOCALHOST + ":" + brokerPort;
    }

    public String getZkServerConnectString() {
        return zkServer.connectString();
    }

    public Properties getDefaultProducerConfig() {
        Properties properties =
                TestUtils.getProducerConfig(getBrokerAddr(), "kafka.producer.DefaultPartitioner");
        properties.put("serializer.class", JsonEncoder.class.getName());
        properties.put("key.serializer.class", "kafka.serializer.StringEncoder");
        return properties;
    }

    public Properties getDefaultConsumerConfig() {
        Properties props = new Properties();
        props.put("zookeeper.connect", getZkServerConnectString());
        props.put("group.id", DEFAULT_GROUP);
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "smallest");
        return props;
    }

}
