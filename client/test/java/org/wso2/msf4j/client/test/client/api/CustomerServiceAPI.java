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

package org.wso2.msf4j.client.test.client.api;

import feign.Param;
import feign.RequestLine;
import org.wso2.msf4j.client.exception.RestServiceException;
import org.wso2.msf4j.client.test.client.exception.CustomerNotFoundRestServiceException;
import org.wso2.msf4j.client.test.model.Customer;

public interface CustomerServiceAPI {
    // Customer service
    @RequestLine("GET /customer/{id}")
    Customer getCustomer(@Param("id") String id) throws CustomerNotFoundRestServiceException, RestServiceException;
}