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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.trustedanalytics.das.parser.Request;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.redis.core.BoundHashOperations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class RedisRequestRepositoryTest {

    @Mock
    private BoundHashOperations<String, String, Request> hashOps;

    private RedisRequestRepository repository;

    @Before
    public void before() {
        repository = new RedisRequestRepository(hashOps);
    }

    @Test
    public void get() throws URISyntaxException {
        Request expected = new Request.RequestBuilder(1, "file:///foo/bar.txt")
                .withOrgUUID("orgID1")
                .withId("key1")
                .build();
        Map<String, Request> entries = (Map<String, Request>) new HashMap<String, Request>();
        entries.put(expected.getOrgUUID() + ":" + expected.getId(), expected);

        when(hashOps.entries()).thenReturn(entries);

        Optional<Request> returned = repository.get(expected.getId());
        assertThat(returned.get().getId(), equalTo(expected.getId()));
    }

    @Test
    public void put() throws URISyntaxException {
        Request request = new Request.RequestBuilder(1, "file:///foo/bar.txt")
                .withOrgUUID("orgID1")
                .withId("key1")
                .build();
        repository.put(request);

        verify(hashOps).put(Mockito.eq(request.getOrgUUID() + ":" + request.getId()), Mockito.eq(request));
    }

    @Test
    public void getAll() throws URISyntaxException {
        String orgUUID = "orgID1";
        Request request = new Request.RequestBuilder(1, "file:///foo/bar.txt")
                .withOrgUUID(orgUUID).withId("key1").build();
        Request request2 = new Request.RequestBuilder(2, "file:///foo/bar.txt")
                .withOrgUUID(orgUUID).withId("key2").build();
        HashMap<String,Request> map = new HashMap<String, Request>();
        map.put(request.getOrgUUID() + ":" + request.getId(), request);
        map.put(request.getOrgUUID() + ":" +request2.getId(), request2);
        when(hashOps.entries()).thenReturn(map);

        Map<String, Request> all = repository.getAll(orgUUID);

        assertThat(all.size(), equalTo(2));
        assertThat(all.values(), hasItems(request, request2));
    }

    @Test
    public void delete() {
        String key = "key1";
        String orgUUID = "orgUUID1";

        HashMap<String,Request> map = new HashMap<String, Request>();
        Request request =  new Request.RequestBuilder(1, null).withOrgUUID(orgUUID).withId(key).build();

        map.put(orgUUID + ":" + key, request);
        when(hashOps.entries()).thenReturn(map);

        repository.delete(key);

        verify(hashOps).delete(Mockito.eq(orgUUID + ":" + key));
    }
}
