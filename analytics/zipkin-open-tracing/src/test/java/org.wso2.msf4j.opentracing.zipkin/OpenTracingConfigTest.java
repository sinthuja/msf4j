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
package org.wso2.msf4j.opentracing.zipkin;


import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.msf4j.internal.MSF4JConstants;
import org.wso2.msf4j.opentracing.core.OpenTracerFactory;
import org.wso2.msf4j.opentracing.core.config.InvalidConfigurationException;
import org.wso2.msf4j.opentracing.core.config.TracerConfig;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

public class OpenTracingConfigTest {

    private void setup(String deploymentFileName) throws URISyntaxException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        String instanceFieldName = "instance";
        URL resource = OpenTracingConfigTest.class.getResource(File.separator + deploymentFileName);
        System.setProperty(MSF4JConstants.DEPLOYMENT_YAML_SYS_PROPERTY,
                new File(resource.toURI().getPath()).getAbsolutePath());
        Constructor<OpenTracerFactory> constructor = OpenTracerFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        OpenTracerFactory openTracerFactoryInstance = constructor.newInstance();
        Field instanceField = OpenTracerFactory.class.getDeclaredField(instanceFieldName);
        instanceField.setAccessible(true);
        instanceField.set(OpenTracerFactory.class, openTracerFactoryInstance);
    }

    @Test(description = "Load opentracing jaeger config with all default params configured")
    public void loadConfigWithAllParams() throws URISyntaxException, InvocationTargetException, NoSuchMethodException,
            NoSuchFieldException, InstantiationException, IllegalAccessException, InvalidConfigurationException {
        setup("deployment.yaml");
        OpenTracerFactory config = OpenTracerFactory.getInstance();
        TracerConfig tracerConfig = config.getTracingConfig(Constants.TRACER_NAME);
        Assert.assertTrue(tracerConfig != null);
        validateConfigs(tracerConfig.getConfiguration(), "localhost", 9411);
    }

    @Test(dependsOnMethods = "loadConfigWithAllParams", description = "Load opentracing jaeger config with custom " +
            "params configured")
    public void loadConfigWithCustomParams() throws URISyntaxException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        setup("deployment-custom-params.yaml");
        OpenTracerFactory config = OpenTracerFactory.getInstance();
        TracerConfig tracerConfig = config.getTracingConfig(Constants.TRACER_NAME);
        Assert.assertTrue(tracerConfig != null);
        validateConfigs(tracerConfig.getConfiguration(), "test.com", 9921);

    }

    @Test(dependsOnMethods = "loadConfigWithCustomParams", description = "Load opentracing jaeger config with " +
            "no params configured")
    public void loadConfigWithNoParams() throws URISyntaxException, InvocationTargetException, NoSuchMethodException,
            NoSuchFieldException, InstantiationException, IllegalAccessException {
        setup("deployment-no-params.yaml");
        OpenTracerFactory config = OpenTracerFactory.getInstance();
        TracerConfig tracerConfig = config.getTracingConfig(Constants.TRACER_NAME);
        Assert.assertTrue(tracerConfig != null);
        validateConfigs(tracerConfig.getConfiguration(),
                Constants.DEFAULT_REPORTER_HOSTNAME, Constants.DEFAULT_REPORTER_PORT);
    }

    private void validateConfigs(Properties properties, String hostname, int port) {
        Assert.assertEquals(properties.get(Constants.REPORTER_HOST_NAME_CONFIG), hostname);
        Assert.assertEquals(properties.get(Constants.REPORTER_PORT_CONFIG), port);
    }
}
