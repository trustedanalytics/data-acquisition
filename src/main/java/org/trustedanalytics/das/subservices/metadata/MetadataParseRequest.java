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
package org.trustedanalytics.das.subservices.metadata;

import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.UUID;

public class MetadataParseRequest {
    @Getter @Setter
    private String id;

    @Getter @Setter
    private String source;

    @Getter @Setter
    private String idInObjectStore;

    @Getter @Setter
    private URI callbackUrl;

    @Getter @Setter
    private String title;

    @Getter @Setter
    private String category;

    @Getter @Setter
    private UUID orgUUID;

    @Getter @Setter
    private boolean publicRequest;
}
