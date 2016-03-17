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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.trustedanalytics.cloud.auth.AuthTokenRetriever;
import org.trustedanalytics.das.dataflow.FlowManager;
import org.trustedanalytics.das.helper.RequestIdGenerator;
import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.security.permissions.PermissionVerifier;
import org.trustedanalytics.das.store.RequestStore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

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

    @ApiOperation(
            value = "Add new acquisition request for file transfer",
            notes = "Privilege level: Consumer of this endpoint must be a member of organization based on valid access token"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK",response = RequestDTO.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Can't access this organization."),
            @ApiResponse(code = 500, message = "Internal server error, see logs for details.")
    })
    @RequestMapping(method = POST)
    @ResponseBody
    @ResponseStatus(ACCEPTED)
    public RequestDTO addRequest(@RequestBody RequestDTO requestDto, HttpServletRequest context)
        throws AccessDeniedException {
        permissionVerifier.throwForbiddenWhenNotAuthorized(context, requestDto);

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String token = tokenRetriever.getAuthToken(authentication);

        final Request request =
                new Request.RequestBuilder(requestDto)
                .withToken(token)
                .withId(requestIdGenerator.getId(requestDto.getSource()))
                .build();

        LOGGER.debug("add({})", requestDto);
        if(StringUtils.isBlank(request.getSource())) {
            throw new BadRequestException("Missing field value: source");
        }

        flowDispatcher
            .apply(request.getSource().split(":")[0])
            .process(request, flowManager);
        return request.toDto();
    }

    @ApiOperation(
            value = "Get specific acquisition request of file transfer with given id",
            notes = "Privilege level: Consumer of this endpoint must be a member of organization based on valid access token"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK",response = RequestDTO.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Can't access this organization."),
            @ApiResponse(code = 500, message = "Internal server error, see logs for details.")
    })
    @RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public RequestDTO getRequest(@PathVariable String id, HttpServletRequest context)
            throws AccessDeniedException {
        LOGGER.debug("get({})", id);
        Request request = requestStore.get(id).orElseThrow(NoSuchElementException::new);
        RequestDTO toReturn = request.toDto();
        permissionVerifier.throwForbiddenWhenNotAuthorized(context, toReturn);
        return toReturn;
    }

    @ApiOperation(
            value = "Get acquisition requests for all file transfers",
            notes = "Privilege level: Consumer of this endpoint must be a member of organization based on valid access token"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK",response = List.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Can't access this organization."),
            @ApiResponse(code = 500, message = "Internal server error, see logs for details.")
    })
    @RequestMapping(method = GET)
    @ResponseBody
    public List<RequestDTO> getAllRequests(@RequestParam(required = false) String orgs, HttpServletRequest context)
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

        return result.values().stream().map(r -> r.toDto()).collect(Collectors.toList());
    }

    @ApiOperation(
            value = "Delete specific acquisition request with given id",
            notes = "Privilege level: Consumer of this endpoint must be a member of organization based on valid access token"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Can't access this organization."),
            @ApiResponse(code = 500, message = "Internal server error, see logs for details")
    })
    @RequestMapping(value = "/{id}", method = DELETE)
    public DefaultResponse delete(@PathVariable String id, HttpServletRequest context)
        throws AccessDeniedException {
        LOGGER.debug("delete({})", id);
        RequestDTO toDelete = requestStore.get(id).orElseThrow(NoSuchElementException::new).toDto();
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
}
