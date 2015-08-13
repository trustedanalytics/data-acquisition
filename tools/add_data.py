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

import requests
import json
import argparse
from argparse import RawTextHelpFormatter
import urlparse
import logging

from utils import Authorization, get_org_guid, setup_logging



HEADERS = {'Content-type': 'application/json', 'Accept': '*/*'}

def debug_req(req, print_text=True):
    '''
    debug function for printing request information
    '''
    logging.info(req.status_code)
    logging.info(req.url)
    if print_text:
        logging.info(req.text)

def add_single_data(token, data):
    '''
    add single dataset request into DAS service.
    '''
    add_dataset_url = urlparse.urljoin(URL, '/rest/das/requests')
    logging.info("adding data to org")
    logging.debug("data:" + str(data))
    data = json.dumps(data)

    r = requests.post(
            add_dataset_url,
            data=data,
            auth=Authorization(token),
            headers=HEADERS)

    if r.status_code == 202:
        logging.info("data accepted")
        logging.debug(r.text)
        return True
    else:
        logging.info("problem with adding data")
        logging.info(data)
        debug_req(r)
        return False


def change_org_name_for_guid(token, data):
    '''
    retrive guuid from organisation name. And change it in data set
    '''
    org_name = data["orgUUID"]
    org_guid = get_org_guid(org_name, token, CF_URL)
    if not org_guid:
        logging.error("guid for organisation {} wasnt found".format(org_name))
        return False
    data["orgUUID"] = org_guid
    return True


def parse_and_send_data(token, input_file):
    '''
    open given file, parse it and add send request for adding every dataset in file.
    '''
    f = open(input_file, 'r')
    json_data = f.read()
    data = json.loads(json_data)

    for row in data:
        guid_found = change_org_name_for_guid(token, row)
        if not guid_found:
            logging.error("sending data for current row aborted:")
            logging.error(row)
            continue
        added = add_single_data(token, row)
        if not added:
            logging.error("sending data to DAS failed. Aborting all requests")
            break



parser = argparse.ArgumentParser(formatter_class=RawTextHelpFormatter, description='''Script for adding data into data-aquistion service.
Input data must be in json format. Example data can be created using create_simple_data.py. Data format (json) is list of dictionaries, each dict contains basic information on dataset:
    [
      {
        "category": DATA_CATEGORY,
        "orgUUID": ORGANISATION NAME (NOT UUID, uuid will be found from name),
        "publicRequest": false/true,
        "source": URL OF FILE TO ADD,
        "title": FILE NAME
      },
      {
          ...
      },
      ...
    ]

''')
parser.add_argument('token',help="OAUTH token. For delete and insert it must have admin privileges")
parser.add_argument('file',help="Input file in json format")
parser.add_argument('--debug', action="store_true",  help="Debug logging")


args = parser.parse_args()

from cf_config import URL, CF_URL

setup_logging(debug=args.debug)
parse_and_send_data(args.token, args.file)










