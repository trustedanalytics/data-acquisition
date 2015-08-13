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
package org.trustedanalytics.das.store;

import org.trustedanalytics.das.parser.Request;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 
 * Representation of RequestStore
 */
public interface RequestStore {

    void put(Request request);

    Optional<Request> get(String key);

    Map<String, Request> getAll(String orgUUID);

    void delete(String key);

    default String getOrgPrefixedKey(String orgUUID, String key) {
        return String.format("%s" + ":" +"%s", orgUUID, key);
    }

    default Map<String, Request> filterByOrgId(Map<String, Request> requests, String orgUUID) {
        return requests.entrySet().stream().filter(x -> x.getKey().startsWith(orgUUID + ":"))
            .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }

    default Map<String, Request> filterByKey(Map<String, Request> requests, String keyUUID) {
        return requests.entrySet().stream()
            .filter(x -> x.getKey().endsWith(":" + keyUUID))
            .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }
}
