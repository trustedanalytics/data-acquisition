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

import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.store.RequestStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryRequestStore implements RequestStore {

    private final Map<String, Request> store = new HashMap<>();

    @Override
    public void put(Request value) {
        store.put(
            this.getOrgPrefixedKey(value.getOrgUUID(), value.getId()),
            value);
    }

    @Override
    public Optional<Request> get(String key) {
        return filterByKey(store, key).values()
            .stream()
            .findFirst();
    }

    @Override
    public Map<String, Request> getAll(String orgUUID) {
        return this.filterByOrgId(this.store, orgUUID);
    }

    @Override
    public void delete(String key) {
        store.remove(filterByKey(store, key).keySet().stream().findFirst().get());
    }
}
