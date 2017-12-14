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
package org.wso2.msf4j.example.opentracing;

import io.opentracing.propagation.TextMap;
import org.wso2.msf4j.Request;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class MS4JRequestInjectorAdaptor implements TextMap {
    private Request request;

    public MS4JRequestInjectorAdaptor(Request request) {
        this.request = request;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("This class should be used only with Tracer.inject()!");
    }

    @Override
    public void put(String key, String value) {
        request.getHeaders().getRequestHeaders().put(key, Collections.singletonList(value));
    }
}
