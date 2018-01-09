/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.wso2.msf4j.example;

import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.example.exception.CustomerNotFoundMapper;
import org.wso2.msf4j.example.exception.EntityNotFoundMapper;
import org.wso2.msf4j.example.exception.GenericServerErrorMapper;
import org.wso2.msf4j.example.exception.InvoiceNotFoundMapper;
import org.wso2.msf4j.example.service.CustomerService;
import org.wso2.msf4j.example.service.InvoiceService;
import org.wso2.msf4j.example.service.ReportService;
import org.wso2.msf4j.internal.MSF4JConstants;

import java.io.File;
import java.net.URL;

/**
 * Application entry point.
 */
public class Application {
    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.trustStore",
                "/Users/sinthu/wso2/sources/dev/git/sinthuja/msf4j2/msf4j/samples/message-tracing/open-tracing/src/main/resources/wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        URL resource = Application.class.getResource(File.separator + "data.agent.config.yaml");
        AgentHolder.setConfigPath(new File(resource.toURI().getPath()).getAbsolutePath());


        resource = Application.class.getResource(File.separator + "deployment.yaml");
        System.setProperty(MSF4JConstants.DEPLOYMENT_YAML_SYS_PROPERTY,
                new File(resource.toURI().getPath()).getAbsolutePath());

        new MicroservicesRunner(8081)
                .addExceptionMapper(new EntityNotFoundMapper(), new CustomerNotFoundMapper(), new
                        GenericServerErrorMapper())
                .deploy(new CustomerService())
                .start();

        new MicroservicesRunner(8082)
                .addExceptionMapper(new EntityNotFoundMapper(), new InvoiceNotFoundMapper(), new
                        GenericServerErrorMapper())
                .deploy(new InvoiceService())
                .start();

        new MicroservicesRunner()
                .addExceptionMapper(new EntityNotFoundMapper(), new CustomerNotFoundMapper(), new
                        InvoiceNotFoundMapper(), new GenericServerErrorMapper())
                .deploy(new ReportService())
                .start();
    }
}
