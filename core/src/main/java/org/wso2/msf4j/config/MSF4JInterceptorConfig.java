/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.msf4j.config;


import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.Set;

/**
 * This is the configuration class about the request/response interceptors added via MSF4J conf.
 */
@Configuration(namespace = "wso2.msf4j.interceptor.configuration", description = "MSF4J configuration")
public class MSF4JInterceptorConfig {

    @Element(description = "This element holds the list of request interceptors that are attached globally")
    private Set<MSF4JInterceptorClassConfig> request;

    @Element(description = "This element holds the list of response interceptors that are attached globally")
    private Set<MSF4JInterceptorClassConfig> response;

    public Set<MSF4JInterceptorClassConfig> getRequest() {
        return request;
    }

    public void setRequest(Set<MSF4JInterceptorClassConfig> request) {
        this.request = request;
    }

    public Set<MSF4JInterceptorClassConfig> getResponse() {
        return response;
    }

    public void setResponse(Set<MSF4JInterceptorClassConfig> response) {
        this.response = response;
    }
}


