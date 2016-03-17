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
package org.trustedanalytics.das.security;

import org.trustedanalytics.cloud.auth.AuthTokenRetriever;
import org.trustedanalytics.das.security.authorization.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

public class PermissionAcquireFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionAcquireFilter.class);
    public static final String ACCESSIBLE_ORGS = "accessibleOrgs";

    private Authorization authorization;
    private AuthTokenRetriever tokenRetriever;


    @Autowired
    public PermissionAcquireFilter(Authorization authorization, AuthTokenRetriever tokenRetriever){
        this.authorization = authorization;
        this.tokenRetriever = tokenRetriever;
    }

    @Override protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse httpServletResponse, FilterChain filterChain)
        throws ServletException, IOException {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authHeader = null;
        if(authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            authHeader = tokenRetriever.getAuthToken(authentication);
        }

        if(authHeader == null) {
            LOGGER.debug("Request has no authorization header.");
            httpServletResponse.sendError(401, "Unauthorized.");
        }
        else {
            UUID[] ids = authorization.getAccessibleOrgs(request).stream()
                    .map(org -> org.getOrganization().getGuid()).toArray(size -> new UUID[size]);
            request.setAttribute(ACCESSIBLE_ORGS, ids);

            if (ids.length > 0) {
                filterChain.doFilter(request, httpServletResponse);
            } else {
                LOGGER.debug("User access denied.");
                httpServletResponse.sendError(403, "Can't access this organization.");
            }
        }
    }
}
