#!/bin/bash
#
# Copyright (c) 2015 Intel Corporation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


set -e

DAS_URL=$1
URL=$2
TOKEN=$3
ORG_UUID=$4

function jsonValue() {
                KEY=$1
                num=$2
                awk -F"[,:}]" '{for(i=1;i<=NF;i++){if($i~/'$KEY'\042/){print $(i+1)}}}' | tr -d '"' | sed -n ${num}p
}

CURL_OPTS="-s -S -H \"Content-Type: application/json\" -H \"Authorization: $TOKEN\" $DAS_URL"
POST="curl -X POST $CURL_OPTS/rest/das/requests -d '{\"source\":\"$URL\", \"orgUUID\":\"$ORG_UUID\"}'}"
request=`eval $POST`
request_id=`echo $request | jsonValue id 1`
echo "Request ID: " $request_id

GET="curl -X GET $CURL_OPTS/rest/das/requests/$request_id"
stored_data=`eval $GET`
echo "Stored item: " $stored_data



