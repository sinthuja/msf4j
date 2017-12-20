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
package org.wso2.msf4j.opentracing.core.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.Properties;

@Configuration(description = "Tracer Configuration")
public class TracerConfig {

    @Element(description = "Name of the tracer", required = true)
    private String name = "";

    @Element(description = "Whether the tracer is enabled or disabled", required = true)
    private boolean enabled = false;

    @Element(description = "Class name of the tracer implementation of " +
            "org.wso2.msf4j.opentracing.core.OpenTracer interface", required = true)
    private String className = "";

    @Element(description = "Configuration properties of the tracer")
    private Properties configuration = new Properties();

    TracerConfig() {
        this.name = "";
        this.enabled = false;
        this.className = "";
        this.configuration = new Properties();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Properties getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Properties configuration) {
        this.configuration = configuration;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
