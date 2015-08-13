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

import json
import random

ORG = "organisation_name"
NAME = "TEST-add_all_{}-{}"


def single_data(rand, i):
    data = {
        "source": "http://fake-csv-server.fake_domain.eu/fake-csv/100",
        "category": "health",
        "title": NAME.format(rand, i),
        "orgUUID": ORG,
        "publicRequest": False
        }
    return data

data_list = []

rand = random.randint(1,100)
data_list = [single_data(rand, i) for i in range(15)]


f = open("data_input.json", 'w')
f.write(json.dumps(data_list, sort_keys=True, indent=2, separators=(',', ': ')))
f.close()






