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
package org.wso2.msf4j.client;

import feign.Request;
import io.opentracing.propagation.TextMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is the request interceptor, which is responsible to add the request span
 * information into the headers.
 */
public class MS4JRequestInjectorAdaptor implements TextMap {
    private Map<String, Collection<String>> headers = new HashMap<>();

    public MS4JRequestInjectorAdaptor(Request request) {
        Iterator<Map.Entry<String, Collection<String>>> requestHeaders = request.headers().entrySet().iterator();
        while (requestHeaders.hasNext()) {
            Map.Entry<String, Collection<String>> header = requestHeaders.next();
            headers.put(header.getKey(), header.getValue());
        }
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("This class should be used only with Tracer.inject()!");
    }

    @Override
    public void put(String key, String value) {
        this.headers.put(key, Collections.singleton(value));
    }

    public Map<String, Collection<String>> getHeaders() {
        return this.headers;
    }
}
