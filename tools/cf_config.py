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

# data-aquisition URL
URL = ''
# cloud foundry api URL
CF_URL = ''


if not URL:
    print "fill URL information in cf_config file"
    raise Exception("URL variable not added")
if not CF_URL:
    print "fill cloud foundtry api URL information in cf_config file"
    raise Exception("CF URL variable not added")

