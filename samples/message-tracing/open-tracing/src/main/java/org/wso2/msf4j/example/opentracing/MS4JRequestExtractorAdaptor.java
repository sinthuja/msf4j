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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;

public class MS4JRequestExtractorAdaptor implements TextMap {
    private Request request;

    public MS4JRequestExtractorAdaptor(Request request) {
        this.request = request;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return new RequestIterator(this.request.getHeaders());
    }

    @Override
    public void put(String s, String s1) {
        throw new UnsupportedOperationException("This class should be used only with Tracer.extract()!");
    }

    private class RequestIterator implements Iterator<Map.Entry<String, String>> {
        private Iterator<Map.Entry<String, List<String>>> iterator;

        private RequestIterator(HttpHeaders headers) {
            this.iterator = headers.getRequestHeaders().entrySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public Map.Entry<String, String> next() {
            Map.Entry<String, List<String>> header = this.iterator.next();
            return new HeaderEntry(header.getKey(), header.getValue().get(0));
        }

        public class HeaderEntry implements Map.Entry<String, String> {
            private String key;
            private String value;

            public HeaderEntry(String key, String value) {
                this.key = key;
                this.value = value;
            }

            @Override
            public String getKey() {
                return this.key;
            }

            @Override
            public String getValue() {
                return this.value;
            }

            @Override
            public String setValue(String value) {
                this.value = value;
                return this.value;
            }
        }
    }
}
