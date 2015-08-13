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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// TODO: should be taken from Downloader code
@JsonIgnoreProperties(ignoreUnknown = true)
public class DownloadStatus {

    private String id;
    private String state; // it is enum in original code
    private String source;
    private long downloadedBytes;
    private String savedObjectId;
    private String objectStoreId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getDownloadedBytes() {
        return downloadedBytes;
    }

    public void setDownloadedBytes(long downloadedBytes) {
        this.downloadedBytes = downloadedBytes;
    }

    public String getSavedObjectId() {
        return savedObjectId;
    }

    public void setSavedObjectId(String savedObjectId) {
        this.savedObjectId = savedObjectId;
    }

    public String getObjectStoreId() {
        return objectStoreId;
    }

    public void setObjectStoreId(String objectStoreId) {
        this.objectStoreId = objectStoreId;
    }

    @Override public String toString() {
        return "DownloadStatus{" +
            "id='" + id + '\'' +
            ", state='" + state + '\'' +
            ", source='" + source + '\'' +
            ", downloadedBytes=" + downloadedBytes +
            ", savedObjectId='" + savedObjectId + '\'' +
            ", objectStoreId='" + objectStoreId + '\'' +
            '}';
    }
}
