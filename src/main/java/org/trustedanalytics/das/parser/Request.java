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

package org.trustedanalytics.das.parser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.das.service.RequestDTO;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * Information about submitted request : status etc.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Request {
    private static final String OTHER = "other";

    public static class RequestBuilder {

        private String id;

        private int userId;

        private String source;

        private State state;

        private String idInObjectStore;

        private String category;

        private String title;

        private String orgUUID;

        private String token;

        private Map<State, Long> timestamps;

        private boolean publicRequest;

        public RequestBuilder(int userId, String source) {
            this.userId = userId;
            this.source = source;
            this.timestamps = new HashMap<>();
            this.state = State.NEW;
        }

        public RequestBuilder(Request original) {
            category = original.getCategory();
            id = original.getId();
            idInObjectStore = original.getIdInObjectStore();
            orgUUID = original.getOrgUUID();
            publicRequest = original.isPublicRequest();
            if(StringUtils.isNotBlank(original.getSource())) {
                source = original.getSource();
            }
            else {
                LOGGER.error("Original request have empty source: " + original);
            }
            state = original.getState();
            title = original.getTitle();
            token = original.getToken();
            userId = original.getUserId();
            timestamps = original.getTimestamps();
        }

        public RequestBuilder(RequestDTO dto) {
            category = StringUtils.isBlank(dto.getCategory()) ? OTHER : dto.getCategory();
            id = dto.getId();
            idInObjectStore = dto.getIdInObjectStore();
            orgUUID = dto.getOrgUUID();
            publicRequest = dto.isPublicRequest();
            source = dto.getSource();
            state = dto.getState();
            title = dto.getTitle();
            userId = dto.getUserId();
            timestamps = dto.getTimestamps();
        }

        public RequestBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public RequestBuilder withState(State state) {
            this.state = state;
            return this;
        }

        public RequestBuilder withIdInObjectStore(String idInObjectStore) {
            this.idInObjectStore = idInObjectStore;
            return this;
        }

        public RequestBuilder withCategory(String category) {
            this.category = category;
            return this;
        }

        public RequestBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public RequestBuilder withOrgUUID(String orgUUID) {
            this.orgUUID = orgUUID;
            return this;
        }

        public RequestBuilder withToken(String token) {
            this.token = token;
            return this;
        }

        public RequestBuilder withPublicRequest(boolean publicRequest) {
            this.publicRequest = publicRequest;
            return this;
        }

        public RequestBuilder withTimestamps(Map<State, Long> timestamps) {
            this.timestamps = timestamps;
            return this;
        }

        public Request build() {
            return new Request(id, userId, source, state, idInObjectStore, category,
                    title, orgUUID, token, publicRequest, timestamps);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);


    @Getter
    private final String id;

    @Getter
    private final int userId;

    @Getter
    private final String source;

    @Getter
    private final State state;

    @Getter
    private final String idInObjectStore;

    @Getter
    private final String category;

    @Getter
    private final String title;

    @Getter
    private final Map<State, Long> timestamps;

    @Getter
    private final String orgUUID;

    @Getter
    private final String token;

    @Getter
    private final boolean publicRequest;

    @JsonCreator
    private Request(@JsonProperty("id") String id, @JsonProperty("userId") int userId,
                    @JsonProperty("source") String source, @JsonProperty("state") State state,
                    @JsonProperty("idInObjectStore") String idInObjectStore, @JsonProperty("category") String category,
                    @JsonProperty("title") String title, @JsonProperty("orgUUID") String orgUUID,
                    @JsonProperty("token") String token, @JsonProperty("publicRequest") boolean publicRequest,
                    @JsonProperty("timestamps") Map<State, Long> timestamps) {
        this.id = id;
        this.userId = userId;
        this.source = source;
        this.state = state;
        this.idInObjectStore = idInObjectStore;
        this.category = category;
        this.title = title;
        this.orgUUID = orgUUID;
        this.token = token;
        this.publicRequest = publicRequest;
        this.timestamps = timestamps;
    }

    public RequestDTO toDto() {
        RequestDTO dto = new RequestDTO();
        dto.setCategory(category);
        dto.setId(id);
        dto.setIdInObjectStore(idInObjectStore);
        dto.setOrgUUID(orgUUID);
        dto.setPublicRequest(publicRequest);
        dto.setSource(source);
        dto.setState(state);
        dto.setTimestamps(timestamps);
        dto.setTitle(title);
        dto.setUserId(userId);
        return dto;
    }


    public Request setCurrentTimestamp(State newState) {
        long epochSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        Map<State, Long> newTimestamps = new HashMap<State, Long>(timestamps);
        newTimestamps.put(newState, epochSecond);

        return new RequestBuilder(this)
                .withState(newState)
                .withTimestamps(newTimestamps)
                .build();
    }

    public Request changeState(State newState) {
        Request withNewTimestamp = setCurrentTimestamp(newState);
        return new RequestBuilder(withNewTimestamp)
                .withState(newState)
                .build();
    }

    public Request setIdInObjectStore(String idInObjectStore) {
        return new RequestBuilder(this)
                .withIdInObjectStore(idInObjectStore)
                .build();
    }

   @Override public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Request [id=");
        builder.append(id);
        builder.append(", userId=");
        builder.append(userId);
        builder.append(", orgUUID=");
        builder.append(orgUUID);
        builder.append(", source=");
        builder.append(source);
        builder.append(", status=");
        builder.append(state);
        builder.append(", title=");
        builder.append(title);
        builder.append(", category=");
        builder.append(category);
        builder.append(", timestamps=");
        builder.append(timestamps);
        builder.append("]");
        return builder.toString();
    }

    private int createHashCode(int  previousHash, Object object) {
        return previousHash + ((object == null) ? 0 : object.hashCode());
    }

    @Override public int hashCode() {
        final int prime = 31;

        int result = createHashCode(prime, id);
        result = createHashCode(prime * result, source);
        result = createHashCode(prime * result, state);
        result = createHashCode(prime * result, title);
        result = createHashCode(prime * result, category);
        result = createHashCode(prime * result, orgUUID);

        result = prime * result + userId;
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Request other = (Request) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        if (orgUUID == null) {
            if (other.orgUUID != null)
                return false;
        } else if (!orgUUID.equals(other.orgUUID))
            return false;
        if (state != other.state)
            return false;
        if (userId != other.userId)
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (category == null) {
            if (other.category != null)
                return false;
        } else if (!category.equals(other.category))
            return false;
        return true;
    }
}
