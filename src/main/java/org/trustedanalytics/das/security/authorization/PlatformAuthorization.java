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
package org.trustedanalytics.das.security.authorization;

import org.trustedanalytics.cloud.cc.api.CcOrgPermission;
import org.trustedanalytics.das.security.errors.OauthTokenMissingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class PlatformAuthorization implements Authorization {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformAuthorization.class);

    private final String userManagementBaseUrl;

    public PlatformAuthorization(String userManagementBaseUrl) {
        this.userManagementBaseUrl = userManagementBaseUrl;
    }

    @Override public Collection<CcOrgPermission> getAccessibleOrgs(HttpServletRequest request)
        throws IOException, ServletException {

        LOGGER.debug("Collecting user's orgs");

        String token;
        try {
            token = getToken(request);
        } catch (OauthTokenMissingException e) {
            LOGGER.debug(e.getMessage(), e);
            return new ArrayList<CcOrgPermission>() {
            };
        }

        String url = userManagementBaseUrl + "/rest/orgs/permissions";
        ResponseEntity<CcOrgPermission[]> access = RestOperationsHelpers.getForEntityWithToken(
            new RestTemplate(), token, url, CcOrgPermission[].class);
        return Arrays.asList(access.getBody());
    }

    private String getToken(HttpServletRequest request) throws OauthTokenMissingException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            throw new OauthTokenMissingException("Cannot find 'Authorization' header.");
        } else {
            return authHeader.replaceAll("(?i)bearer ", "");
        }
    }
}
