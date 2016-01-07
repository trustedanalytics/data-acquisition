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

import org.trustedanalytics.das.dataflow.FlowManager;
import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.parser.State;

public class RequestFlowForExistingFile implements FlowHandler {

    @Override
    public void process(Request request, FlowManager flowManager){
        Request newRequest = request.changeState(State.NEW);
        Request validatedRequest = newRequest.changeState(State.VALIDATED);

        // from now on this should be treated like any other download
        flowManager.requestDownloaded(validatedRequest);
   }
}
