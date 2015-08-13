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
package org.trustedanalytics.das.subservices;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ServiceManager;
import org.trustedanalytics.das.parser.Request;
import org.trustedanalytics.das.store.BlockingRequestQueue;
import org.trustedanalytics.das.subservices.downloader.DownloadStatus;
import org.trustedanalytics.das.subservices.downloader.RestDownloaderClient;

import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestClientException;

@RunWith(MockitoJUnitRunner.class)
public class RestDownloaderClientTest {
    ServiceManager serviceManager;

    PoolingThreadedService poolingService;

    @Mock
    BlockingRequestQueue toDownload;

    @Mock
    RestDownloaderClient client;

    @Before
    public void setUp() {
        poolingService = new PoolingThreadedService(toDownload, client::download, "download");
        serviceManager = new ServiceManager(Lists.newArrayList(poolingService));
    }

    @Test
    public void download_test_success() throws InterruptedException {
        Request request = new Request();
        when(client.download(request)).thenReturn(new DownloadStatus());
        when(toDownload.take()).thenReturn(request, null);

        serviceManager.startAsync();
        verify(toDownload, timeout(1000).atLeastOnce()).take();
        Assert.assertTrue(poolingService.isRunning());

    }

    @Test
    public void download_downloader_unavailable_failover() throws InterruptedException, URISyntaxException {
        Request request = new Request();
        when(client.download(request)).thenThrow(new RestClientException(""));
        when(toDownload.take()).thenReturn(request, null);

        serviceManager.startAsync();
        verify(toDownload, timeout(1000).atLeastOnce()).take();
        Assert.assertTrue(poolingService.isRunning());
    }

    @After
    public void tearDown() {
        serviceManager.stopAsync();
    }
}
