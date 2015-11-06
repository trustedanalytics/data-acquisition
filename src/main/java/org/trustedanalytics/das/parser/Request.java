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

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Information about submitted request : status etc.
 */
public class Request {
    private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);

    public enum State {
        NEW, VALIDATED, DOWNLOADED, FINISHED, ERROR
    }

    @Getter @Setter
    private String id;

    @Getter @Setter
    private int userId;

    @Getter @Setter
    private String source;

    @Getter @Setter
    private State state;

    @Getter @Setter
    private String idInObjectStore;

    @Getter @Setter
    private String category;

    @Getter @Setter
    private String title;

    @Getter
    private Map<State, Long> timestamps = new HashMap<>();

    @Getter @Setter
    private String orgUUID;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter @Setter
    private String token;

    @Getter @Setter
    private boolean publicRequest;

    public void setTimestamp(State state) {
        long epochSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        timestamps.put(state, epochSecond);
    }

    public void changeState(State state) {
        setTimestamp(state);
        setState(state);
    }

    public static Request newInstance(Request request) {
        final Request copy = new Request();
        copy.category = request.category;
        copy.id = request.id;
        copy.idInObjectStore = request.idInObjectStore;
        copy.orgUUID = request.orgUUID;
        copy.publicRequest = request.publicRequest;
        if(StringUtils.isNotBlank(request.source)) {
            copy.source = request.source;
        }
        else {
            LOGGER.error("Original request have empty source: " + request);
        }
        copy.state = request.state;
        copy.title = request.title;
        copy.token = request.token;
        copy.userId = request.userId;
        copy.timestamps = new HashMap<State, Long>(request.timestamps);
        return copy;
    }

    public static Request newInstance(Request request, Consumer<Request> customizations) {
        final Request copy = newInstance(request);
        customizations.accept(copy);
        return copy;
    }

    public static Request newInstance(int userId, String resource) {
        Request request = new Request();
        request.userId = userId;
        request.source = resource;
        request.state = State.NEW;
        return request;
    }

    public static Request newInstance(int userId, String requestId, String resource) {
        Request request = newInstance(userId, resource);
        request.id = requestId;
        return request;
    }

    public static Request newInstance(String orgUUID, int userId, String requestId, String resource) {
        Request request = newInstance(userId, requestId, resource);
        request.setOrgUUID(orgUUID);
        return request;
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
