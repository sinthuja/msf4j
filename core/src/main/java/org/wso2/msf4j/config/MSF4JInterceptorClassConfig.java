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

import java.util.Locale;

/**
 * This is the configuration class which has the holder for the class name of the request/response interceptors
 * that is defined in {@link MSF4JInterceptorConfig}
 */

@Configuration(description = "This configuration wraps the class configuration")
public class MSF4JInterceptorClassConfig {

    @Element(description = "The class name of the interceptor", required = true)
    private String name = "";

    public MSF4JInterceptorClassConfig() {
        this.name = "";
    }

    public String getName() {
        if (this.name != null) {
            return name.trim();
        } else {
            return "";
        }
    }

    public void setName(String name) {
        if (name != null) {
            this.name = name.trim();
        }
    }

    public boolean equals(Object object) {
        if (object instanceof MSF4JInterceptorClassConfig) {
            MSF4JInterceptorClassConfig config = (MSF4JInterceptorClassConfig) object;
            if (config.name != null && this.name != null && config.name.equalsIgnoreCase(this.name)) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        if (this.name == null) {
            return 0;
        }
        return this.name.toLowerCase(Locale.ENGLISH).hashCode();
    }

}
