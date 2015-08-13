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
package org.trustedanalytics.das;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.trustedanalytics.das.subservices.callbacks.CallbacksService;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import java.io.IOException;

public class AuthenticationDisabler implements BeanPostProcessor {
	private final static Logger logger = LoggerFactory.getLogger(CallbacksService.class);
	
    @Override public Object postProcessBeforeInitialization(Object bean, String beanName)
        throws BeansException {

        final String AUTHENTICATION_BEAN_TO_REPLACE = "OAuth2AuthenticationProcessingFilter";

        if (beanName.contains(AUTHENTICATION_BEAN_TO_REPLACE)) {

            OAuth2AuthenticationProcessingFilter authenticationMock =
                mock(OAuth2AuthenticationProcessingFilter.class);

            try {
                Mockito.doAnswer(this::authenticateEverything)
                    .when(authenticationMock).doFilter(any(), any(), any());

            } catch (IOException | ServletException e) {
            	logger.error("Error: {}", e);
                throw new BeanCreationException(
                    String.format("Cannot mock '%s'", AUTHENTICATION_BEAN_TO_REPLACE));
            }

            return authenticationMock;
        }

        return bean;
    }

    private String authenticateEverything(InvocationOnMock invocation)
        throws IOException, ServletException {

        ServletRequest req = (ServletRequest) invocation.getArguments()[0];
        ServletResponse res = (ServletResponse) invocation.getArguments()[1];
        FilterChain filterChain = (FilterChain) invocation.getArguments()[2];

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(req, res);

        return "method called";
    }

    @Override public Object postProcessAfterInitialization(Object bean, String beanName)
        throws BeansException {
        return bean;
    }
}
