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
package org.trustedanalytics.das.config;

import java.util.Arrays;
import java.util.List;

import org.trustedanalytics.das.subservices.downloader.DownloaderClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import org.trustedanalytics.das.subservices.callbacks.CallbackUrlListener;
import org.trustedanalytics.das.subservices.metadata.MetadataParser;

@Configuration
@Profile("test")
public class DasLocalConfig {

    // FIXME make setting this variable less hacky
    private static String callbackUrl;

    @Value("${callback.url}")
    private String callbackUrlPrivate;
    
    @Autowired
    private MetadataParser metadataParser;
 
    @Autowired
    private DownloaderClient downloaderClient;
    
    @Bean
    public ApplicationListener<EmbeddedServletContainerInitializedEvent> portGrabber() {
        List<CallbackUrlListener> listeners = Arrays.asList((CallbackUrlListener)metadataParser, 
                (CallbackUrlListener)downloaderClient);

        DasLocalConfig.callbackUrl = callbackUrlPrivate;

        return new PortGrabber(listeners);
    }

    public static class PortGrabber implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

        private final Iterable<CallbackUrlListener> listeners;
        
        public PortGrabber(Iterable<CallbackUrlListener> listeners) {
            this.listeners = listeners;
        }

        @Override
        public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
            for (CallbackUrlListener l: listeners) {
                l.setCallbacksUrl(callbackUrl);
            }
        }
    }
}
