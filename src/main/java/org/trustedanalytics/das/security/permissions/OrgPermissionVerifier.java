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
package org.trustedanalytics.das.security.permissions;

import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.security.PermissionAcquireFilter;
import org.springframework.stereotype.Component;
import org.trustedanalytics.das.service.RequestDTO;
import org.trustedanalytics.das.service.BadRequestException;
import scala.math.Ordering;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

@Component
public class OrgPermissionVerifier implements PermissionVerifier {
    @Override
    public UUID[] getAccessibleOrgsIDs(HttpServletRequest context) {
        return (UUID[]) context.getAttribute(PermissionAcquireFilter.ACCESSIBLE_ORGS);
    }

    @Override
    public void throwForbiddenWhenNotAuthorized(HttpServletRequest context, RequestDTO request)
        throws AccessDeniedException {
        UUID[] hasAccess = getAccessibleOrgsIDs(context);

        throwBadRequestIfInvalidUuid(request.getOrgUUID());
        UUID uuid = UUID.fromString(request.getOrgUUID());

        throwForbiddenWhenIdNotListed(Arrays.asList(hasAccess), uuid);
    }

    @Override
    public void throwForbiddenWhenIdNotListed(Collection<UUID> hasAccess, UUID uuid)
        throws AccessDeniedException {
        if(!hasAccess.contains(uuid)) {
            throw new AccessDeniedException(String.format("You have not access to org: %s", uuid));
        }
    }

    @Override
    public void throwBadRequestIfInvalidUuid(String uuid)
        throws BadRequestException {
        try {
            UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("Organization UUID %s is not a valid UUID", uuid));
        }
    }
}
