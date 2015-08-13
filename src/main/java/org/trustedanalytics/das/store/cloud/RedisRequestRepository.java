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

import java.util.Map;
import java.util.Optional;

import org.trustedanalytics.das.store.RequestStore;
import org.springframework.data.redis.core.BoundHashOperations;

import org.trustedanalytics.das.parser.Request;

public class RedisRequestRepository implements RequestStore {

    private final BoundHashOperations<String, String, Request> hashOps;
    
    public RedisRequestRepository(BoundHashOperations<String, String, Request> hashOps) {
        this.hashOps = hashOps;
    }

    @Override
    public void put(Request request) {
        hashOps.put(
            this.getOrgPrefixedKey(request.getOrgUUID(), request.getId()),
            request);
    }

    @Override
    public Optional<Request> get(String key) {
        return this.filterByKey(this.hashOps.entries(), key).values()
            .stream()
            .findFirst();
    }

    @Override
    public Map<String, Request> getAll(String orgUUID) {
        return this.filterByOrgId(this.hashOps.entries(), orgUUID);
    }

    @Override
    public void delete(String key) {
        String fullKey =
            this.filterByKey(this.hashOps.entries(), key).keySet().stream().findFirst().get();
        hashOps.delete(fullKey);
    }
}
