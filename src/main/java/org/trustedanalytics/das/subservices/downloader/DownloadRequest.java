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
package org.trustedanalytics.das.subservices.downloader;

import com.google.common.base.MoreObjects;

import java.util.UUID;

public class DownloadRequest {

    private UUID orgUUID;
    private String source;
    private String callback;

    public UUID getOrgUUID() {
        return orgUUID;
    }

    public void setOrgUUID(UUID orgUUID) {
        this.orgUUID = orgUUID;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("orgUUID", orgUUID)
            .add("source", source)
            .add("callback", callback)
            .toString();
    }
}
