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

package org.wso2.msf4j.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.msf4j.config.MSF4JConfig;
import org.wso2.msf4j.config.MSF4JInterceptorClassConfig;
import org.wso2.msf4j.config.MSF4JInterceptorConfig;
import org.wso2.msf4j.interceptor.RequestInterceptor;
import org.wso2.msf4j.interceptor.ResponseInterceptor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This is the class which loads the configuration {@link MSF4JInterceptorConfig} from the MSF4J configuration,
 * and attach to the global request/response interceptors in {@link org.wso2.msf4j.MicroservicesRunner}.
 */
public class MSF4JInterceptorLoader {
    private static final Logger log = LoggerFactory.getLogger(MSF4JInterceptorLoader.class);
    private MSF4JInterceptorConfig msf4JInterceptorConfig;

    public MSF4JInterceptorLoader() {
        ConfigProvider configProvider = DataHolder.getInstance().getConfigProvider();
        if (configProvider == null) {
            if (DataHolder.getInstance().getBundleContext() != null) {
                throw new RuntimeException("Failed to populate MSF4J Configuration. Config Provider is Null.");
            }
            //Standalone mode
            String deploymentYamlPath = System.getProperty(MSF4JConstants.DEPLOYMENT_YAML_SYS_PROPERTY);

            try {
                if (deploymentYamlPath != null && Files.exists(Paths.get(deploymentYamlPath))) {
                    configProvider = ConfigProviderFactory.getConfigProvider(Paths.get(deploymentYamlPath), null);
                    DataHolder.getInstance().setConfigProvider(configProvider);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("MSF4J Configuration file is not provided. either system property '"
                                + MSF4JConstants.DEPLOYMENT_YAML_SYS_PROPERTY + "' is not set or provided file path " +
                                "not exist. Hence using default configuration.");
                    }
                }
            } catch (ConfigurationException e) {
                throw new RuntimeException("Error loading deployment.yaml Configuration", e);
            }
        }

        try {
            if (configProvider != null) {
                this.msf4JInterceptorConfig = DataHolder.getInstance().getConfigProvider().
                        getConfigurationObject(MSF4JInterceptorConfig.class);
            } else {
                this.msf4JInterceptorConfig = MSF4JInterceptorConfig.class.newInstance();
            }
        } catch (ConfigurationException e) {
            throw new RuntimeException("Error while loading " + MSF4JConfig.class.getName() + " from config provider",
                    e);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Error while creating instance of the class " + MSF4JConfig.class.getName(), e);
        }
    }

    public RequestInterceptor[] getRequestInterceptors() {
        Set<MSF4JInterceptorClassConfig> classConfigs = this.msf4JInterceptorConfig.getRequest();
        List<RequestInterceptor> requestInterceptors = new ArrayList<>();
        processInterceptors(classConfigs, RequestInterceptor.class, requestInterceptors);
        return requestInterceptors.toArray(new RequestInterceptor[requestInterceptors.size()]);
    }

    private void processInterceptors(Set<MSF4JInterceptorClassConfig> classConfigs,
                                     Class interceptorInterface, List interceptors) {
        if (classConfigs != null) {
            for (MSF4JInterceptorClassConfig classConfig : classConfigs) {
                if (!classConfig.getName().isEmpty()) {
                    try {
                        Class<?> requestInterceptorClass = Class.forName(classConfig.getName())
                                .asSubclass(interceptorInterface);
                        interceptors.add(requestInterceptorClass.newInstance());
                    } catch (ClassNotFoundException e) {
                        log.error("Cannot load request interceptor - " + classConfig.getName(), e);
                    } catch (InstantiationException e) {
                        log.error("Unable to instantiate the request interceptor class - " + classConfig.getName(), e);
                    } catch (IllegalAccessException e) {
                        log.error("Unable to access the class defined in the request interceptor - "
                                + classConfig.getName(), e);
                    }
                }
            }
        }
    }

    public ResponseInterceptor[] getResponseInterceptors() {
        Set<MSF4JInterceptorClassConfig> classConfigs = this.msf4JInterceptorConfig.getResponse();
        List<ResponseInterceptor> responseInterceptors = new ArrayList<>();
        processInterceptors(classConfigs, ResponseInterceptor.class, responseInterceptors);
        return responseInterceptors.toArray(new ResponseInterceptor[responseInterceptors.size()]);
    }
}
