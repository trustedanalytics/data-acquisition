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

from requests.auth import AuthBase
import urlparse
import requests
import json

import logging

class Authorization(AuthBase):

    def __init__(self, token):
        self.token = token

    def __call__(self, request):
        request.headers['Authorization'] = self.token
        return request


def get_org_guid(name, token, cf_url):
    logging.info("looking for guid of organisation: {}".format(name))

    filter_q = "name IN {}".format(name)
    params = {"q": filter_q}
    url = urlparse.urljoin(cf_url, '/v2/organizations')
    r = requests.get(url, auth=Authorization(token), params=params)

    data = json.loads(r.content)
    for row in data['resources']:
        logging.debug("found row with name {} and guid {}".format(
            row['entity']['name'],
            row['metadata']['guid']))
        if row['entity']['name'] == name:
            return row['metadata']['guid']
    return None

def setup_logging(debug=False):

    log_format = '%(levelname)s: %(message)s'
    level = logging.DEBUG if debug else logging.INFO
    logging.basicConfig(format=log_format, level=level)

    # silencing requests logger
    logging.getLogger("requests").setLevel(logging.WARNING)
    logging.getLogger("urllib3").setLevel(logging.WARNING)



