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
package org.trustedanalytics.das.dataflow;

import com.google.common.collect.ImmutableSet;
import org.trustedanalytics.das.service.FlowHandler;
import org.trustedanalytics.das.service.RequestFlowForExistingFile;
import org.trustedanalytics.das.service.RequestFlowForNewFile;
import org.trustedanalytics.das.store.BlockingRequestIdQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.trustedanalytics.das.store.RequestStore;

import java.util.function.Function;

@Configuration
public class FlowConfiguration {

    @Bean
    public FlowManager flowManager(BlockingRequestIdQueue toRequestsParser,
                                   BlockingRequestIdQueue toDownloader, BlockingRequestIdQueue toMetadataParser,
                                   RequestStore requestStore) {
        return new FlowManager(toRequestsParser, toDownloader, toMetadataParser, requestStore);
    }

    @Bean
    public FlowHandler requestFlowForNewFile() {
        return new RequestFlowForNewFile();
    }

    @Bean
    public FlowHandler requestFlowForExistingFile() {
        return new RequestFlowForExistingFile();
    }

    @Bean
    public Function<String, FlowHandler> flowHandler() {
       return scheme -> {
           if(ImmutableSet.of("http", "https").contains(scheme)) {
                return requestFlowForNewFile();
           } else {
               return requestFlowForExistingFile();
           }
       };
    }
}
