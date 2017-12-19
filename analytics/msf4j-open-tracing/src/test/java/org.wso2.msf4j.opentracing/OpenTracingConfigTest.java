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
package org.wso2.msf4j.opentracing;


import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.msf4j.opentracing.core.OpenTracerFactory;
import org.wso2.msf4j.opentracing.core.config.TracerConfig;

import static org.wso2.msf4j.internal.MSF4JConstants.DEPLOYMENT_YAML_SYS_PROPERTY;

public class OpenTracingConfigTest {

    @Test
    public void loadConfig(){
        System.setProperty(DEPLOYMENT_YAML_SYS_PROPERTY,
                "/Users/sinthu/wso2/sources/dev/git/sinthuja/msf4j/analytics/msf4j-open-tracing/src/test/resources/deployment.yaml");
        OpenTracerFactory config = OpenTracerFactory.getInstance();
        TracerConfig tracerConfig = config.getTracingConfig("jaeger");
        Assert.assertTrue(tracerConfig != null);
    }
}
