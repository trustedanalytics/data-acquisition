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
package org.trustedanalytics.das.service;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang.StringUtils;
import org.trustedanalytics.das.security.permissions.PermissionVerifier;
import org.trustedanalytics.cloud.auth.AuthTokenRetriever;
import org.trustedanalytics.das.dataflow.FlowManager;
import org.trustedanalytics.das.helper.RequestIdGenerator;
import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.store.RequestStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(value="rest/das/requests")
public class RestDataAcquisitionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestDataAcquisitionService.class);

    private final FlowManager flowManager;
    private final RequestStore requestStore;
    private final RequestIdGenerator requestIdGenerator;
    private final AuthTokenRetriever tokenRetriever;
    private final PermissionVerifier permissionVerifier;
    private final Function<String, FlowHandler> flowDispatcher;

    @Autowired
    public RestDataAcquisitionService(
            FlowManager flowManager,
            RequestStore requestStore,
            RequestIdGenerator requestIdGenerator,
            AuthTokenRetriever tokenRetriever,
            PermissionVerifier permissionVerifier,
            Function<String, FlowHandler> flowDispatcher) {
        this.flowManager = flowManager;
        this.requestStore = requestStore;
        this.requestIdGenerator = requestIdGenerator;
        this.tokenRetriever = tokenRetriever;
        this.permissionVerifier = permissionVerifier;
        this.flowDispatcher = flowDispatcher;
    }

    @RequestMapping(method = POST)
    @ResponseBody
    @ResponseStatus(ACCEPTED)
    public Request addRequest(@RequestBody Request request, HttpServletRequest context)
        throws AccessDeniedException {
        permissionVerifier.throwForbiddenWhenNotAuthorized(context, request);

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String token = tokenRetriever.getAuthToken(authentication);

        request.setToken(token);
        LOGGER.debug("add({})", request);
        request.setId(requestIdGenerator.getId(request.getSource()));
        String source = request.getSource();
        if(StringUtils.isBlank(source)) {
            throw new BadRequestException("Missing field value: source");
        }


        flowDispatcher
            .apply(source.split(":")[0])
            .process(request, flowManager, requestStore);
        return excludeToken(request);
    }

    @RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public Request getRequest(@PathVariable String id, HttpServletRequest context)
        throws AccessDeniedException {
        LOGGER.debug("get({})", id);
        Request toReturn = requestStore.get(id).orElseThrow(NoSuchElementException::new);
        permissionVerifier.throwForbiddenWhenNotAuthorized(context, toReturn);
        return excludeToken(toReturn);
    }

    @RequestMapping(method = GET)
    @ResponseBody
    public List<Request> getAllRequests(@RequestParam(required = false) String orgs, HttpServletRequest context)
    throws AccessDeniedException {
        LOGGER.debug("getAllRequest()");

        Collection<UUID> hasAccess = Arrays.asList(permissionVerifier.getAccessibleOrgsIDs(context));

        String[] uuids = null;
        if(orgs != null) {
            uuids = orgs.split(",");
            for(String u : uuids) {
                permissionVerifier.throwForbiddenWhenIdNotListed(hasAccess, UUID.fromString(u));
            }
        } else {
            uuids = hasAccess.stream().map(uuid -> uuid.toString()).toArray(String[]::new);
        }

        Map<String, Request> result = new HashMap<>();
        for (String uuid : uuids) {
            result.putAll(requestStore.getAll(uuid));
        }

        return excludeToken(new ArrayList<>(result.values()));
    }

    @RequestMapping(value = "/{id}", method = DELETE)
    public DefaultResponse delete(@PathVariable String id, HttpServletRequest context)
        throws AccessDeniedException {
        LOGGER.debug("delete({})", id);
        Request toDelete = requestStore.get(id).orElseThrow(NoSuchElementException::new);
        permissionVerifier.throwForbiddenWhenNotAuthorized(context, toDelete);
        requestStore.delete(id);
        return DefaultResponse.newInstance("OK");
    }

    @ExceptionHandler(BadRequestException.class)
    public void badRequestHandler(BadRequestException exception, HttpServletResponse response ) throws IOException {
        LOGGER.warn("Invalid request: " + exception.getMessage());
        response.sendError(BAD_REQUEST.value(), exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(NOT_FOUND)
    public void noSuchElementExceptionHandler() {
    }

    @ExceptionHandler(AccessDeniedException.class)
    public void accessForbidden( HttpServletResponse response ) throws IOException {
        LOGGER.warn("Access forbidden.");
        response.sendError(FORBIDDEN.value(), "You do not have access to requested organization.");
    }
    
    public static final class DefaultResponse {
        private String message;

        private DefaultResponse() {
        }
        
        public static DefaultResponse newInstance(String message) {
            DefaultResponse response = new DefaultResponse();
            response.message = message;
            return response;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * DAS process requests on multiple threads and therefore content (along with token)
     * needs to be serialized and put into queue. However it must not to be present in
     * payload when communicating using rest.
     */
    private Request excludeToken(Request request) {
        return Request.newInstance(request, r -> r.setToken(null));
    }

    private List<Request> excludeToken(Collection<Request> requests) {
        return requests.stream()
            .map(this::excludeToken)
            .collect(Collectors.toList());
    }
}
