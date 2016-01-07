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
package org.trustedanalytics.das.store.cloud;

import org.trustedanalytics.das.store.RequestStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.trustedanalytics.das.parser.Request;

@Configuration
@Profile("cloud")
public class CloudStoreConfig {

    @Value("requests")
    private String redisRequestsKey;

    @Bean
    public RequestStore redisRequestStore(RedisOperations<String, Request> redisTemplate) {
        return new RedisRequestRepository(redisTemplate.boundHashOps(redisRequestsKey));
    }

    @Bean
    public RedisOperations<String, Request> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Request> template = new RedisTemplate<String, Request>();

        template.setConnectionFactory(redisConnectionFactory);

        RedisSerializer<String> keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Request> requestSerializer = new Jackson2JsonRedisSerializer<Request>(Request.class);

        template.setKeySerializer(keySerializer);
        template.setValueSerializer(requestSerializer);
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(requestSerializer);

        return template;
    }
}