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
package org.wso2.msf4j.interceptor;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.msf4j.InterceptorTestBase;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.conf.Constants;
import org.wso2.msf4j.internal.DataHolder;
import org.wso2.msf4j.internal.MSF4JConstants;
import org.wso2.msf4j.internal.MSF4JInterceptorLoader;
import org.wso2.msf4j.service.InterceptorTestMicroService;
import org.wso2.msf4j.service.sub.Team;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import static org.testng.AssertJUnit.assertEquals;

/**
 * This test class validates the behaviour of global request/response interceptors
 * added via MSF4J configuration.
 */
public class GlobalInterceptorLoaderConfigTest extends InterceptorTestBase {
    private static final int port = Constants.PORT + 10;
    private final InterceptorTestMicroService interceptorTestMicroService = new InterceptorTestMicroService();
    private ConfigProvider existingConfigProvider;
    private String msf4jConfigPath;

    @BeforeClass
    public void init() throws URISyntaxException, ConfigurationException {
        this.msf4jConfigPath = System.getProperty(MSF4JConstants.DEPLOYMENT_YAML_SYS_PROPERTY);
        this.existingConfigProvider = DataHolder.getInstance().getConfigProvider();
    }

    private void validateInterceptors(String deploymentFile, int requestInterceptors, int responseInterceptors)
            throws URISyntaxException {
        setup(deploymentFile);
        MSF4JInterceptorLoader msf4JInterceptorLoader = new MSF4JInterceptorLoader();
        assertEquals(msf4JInterceptorLoader.getRequestInterceptors().length, requestInterceptors);
        assertEquals(msf4JInterceptorLoader.getResponseInterceptors().length, responseInterceptors);
    }

    private void setup(String deploymentFile) throws URISyntaxException {
        DataHolder.getInstance().setConfigProvider(null);
        URL resource = GlobalInterceptorLoaderConfigTest.class.getResource(File.separator + "interceptors"
                + File.separator + deploymentFile);
        System.setProperty(MSF4JConstants.DEPLOYMENT_YAML_SYS_PROPERTY,
                new File(resource.toURI().getPath()).getAbsolutePath());
    }

    @Test(description = "Tests the deployment.yaml with one request/response interceptors")
    public void loadOneInterceptorConfiguration() throws URISyntaxException {
        String deploymentFilePath = "deployment.yaml";
        validateInterceptors(deploymentFilePath, 1, 1);
    }

    @Test(dependsOnMethods = "loadOneInterceptorConfiguration", description = "Tests the deployment.yaml with " +
            "multiple request/response interceptors")
    public void loadMultipleInterceptorConfiguration() throws URISyntaxException {
        String deploymentFilePath = "deployment-multiple-interceptors.yaml";
        validateInterceptors(deploymentFilePath, 2, 3);
    }

    @Test(dependsOnMethods = "loadMultipleInterceptorConfiguration", description = "Tests the deployment.yaml with " +
            "no wso2.msf4j.interceptor.configuration")
    public void loadEmptyInterceptorConfiguration() throws URISyntaxException {
        String deploymentFilePath = "deployment-empty.yaml";
        validateInterceptors(deploymentFilePath, 0, 0);
    }

    @Test(dependsOnMethods = "loadEmptyInterceptorConfiguration", description = "Tests the deployment.yaml with " +
            "no request interceptors")
    public void loadNoRequestInterceptorConfiguration() throws URISyntaxException {
        String deploymentFilePath = "deployment-no-request.yaml";
        validateInterceptors(deploymentFilePath, 0, 3);
    }

    @Test(dependsOnMethods = "loadNoRequestInterceptorConfiguration", description = "Tests the deployment.yaml with " +
            "no response interceptors")
    public void loadNoResponseInterceptorConfiguration() throws URISyntaxException {
        String deploymentFilePath = "deployment-no-response.yaml";
        validateInterceptors(deploymentFilePath, 2, 0);
    }

    @Test(dependsOnMethods = "loadNoResponseInterceptorConfiguration", description = "Tests the deployment.yaml " +
            "with empty request interceptors")
    public void loadEmptyRequestInterceptorConfiguration() throws URISyntaxException {
        String deploymentFilePath = "deployment-empty-request.yaml";
        validateInterceptors(deploymentFilePath, 0, 1);
    }

    @Test(dependsOnMethods = "loadEmptyRequestInterceptorConfiguration", description = "Tests the deployment.yaml " +
            "with empty request interceptors")
    public void loadEmptyResponseInterceptorConfiguration() throws URISyntaxException {
        String deploymentFilePath = "deployment-empty-response.yaml";
        validateInterceptors(deploymentFilePath, 1, 0);
    }

    @Test(dependsOnMethods = "loadEmptyResponseInterceptorConfiguration", description = "Tests the deployment.yaml " +
            "with empty name for interceptors")
    public void loadEmptyNameInterceptorConfiguration() throws URISyntaxException {
        String deploymentFilePath = "deployment-with-only-name.yaml";
        validateInterceptors(deploymentFilePath, 0, 0);
    }

    @Test(dependsOnMethods = "loadEmptyNameInterceptorConfiguration", description = "Tests the global attachment" +
            " of the request/reponse interceptors ")
    public void testGlobalInterceptorInvocation() throws IOException, URISyntaxException {
        String deploymentFile = "deployment.yaml";
        setup(deploymentFile);
        MicroservicesRunner microservicesRunner = new MicroservicesRunner(port);
        microservicesRunner
                .deploy(interceptorTestMicroService)
                .start();
        baseURI = URI.create("http://" + Constants.HOSTNAME + ":" + port);
        String microServiceBaseUrl = "/test/interceptorTest/";
        Team team = doGetAndGetResponseObject(microServiceBaseUrl + "subResourceLocatorTest/SL/",
                false, Team.class, Collections.unmodifiableMap(Collections.emptyMap()));
        assertEquals("Cricket", team.getTeamType());
        assertEquals("SL", team.getCountryId());
        assertEquals(TestRequestGlobalInterceptor.getNumberOfRequestsHandled(), 1);
        assertEquals(TestResponseGlobalInterceptor.getNumberOfResponseHandled(), 1);
    }

    @AfterClass
    public void cleanup() {
        if (this.msf4jConfigPath != null) {
            System.setProperty(MSF4JConstants.DEPLOYMENT_YAML_SYS_PROPERTY, this.msf4jConfigPath);
        } else {
            System.clearProperty(MSF4JConstants.DEPLOYMENT_YAML_SYS_PROPERTY);
        }
        DataHolder.getInstance().setConfigProvider(this.existingConfigProvider);
    }
}
