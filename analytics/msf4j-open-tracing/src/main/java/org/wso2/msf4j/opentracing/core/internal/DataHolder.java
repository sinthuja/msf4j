/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.msf4j.opentracing.core.internal;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.config.provider.ConfigProvider;

/**
 * Data holder for open tracing core bundle.
 */
public class DataHolder {

    private static ConfigProvider configProvider;
    private static BundleContext bundleContext;

    public static ConfigProvider getConfigProvider() {
        return DataHolder.configProvider;
    }

    static void setConfigProvider(ConfigProvider configProvider) {
        DataHolder.configProvider = configProvider;
    }

    public static BundleContext getBundleContext() {
        return DataHolder.bundleContext;
    }

    static void setBundleContext(BundleContext bundleContext) {
        DataHolder.bundleContext = bundleContext;
    }
}
