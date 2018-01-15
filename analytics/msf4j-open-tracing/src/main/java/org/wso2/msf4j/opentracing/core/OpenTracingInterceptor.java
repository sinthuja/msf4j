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
package org.wso2.msf4j.opentracing.core;

import io.opentracing.Span;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.interceptor.RequestInterceptor;
import org.wso2.msf4j.interceptor.ResponseInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the open tracing interceptors that will be attached globally in the
 * {@link org.wso2.msf4j.MicroservicesRunner}
 */
public class OpenTracingInterceptor implements RequestInterceptor, ResponseInterceptor {

    private static final String REQUEST_SPAN = "REQUEST_SPAN";
    private OpenTracerFactory openTracerFactory;

    public OpenTracingInterceptor() {
        this.openTracerFactory = OpenTracerFactory.getInstance();
    }

    @Override
    public boolean interceptRequest(Request request, Response response) throws Exception {
        if (this.openTracerFactory.isTracingEnabled()) {
            Map<String, Object> spanContext = this.openTracerFactory.extract(Format.Builtin.HTTP_HEADERS,
                    new MS4JRequestExtractorAdaptor(request));
            Map<String, String> tags = new HashMap<>();
            tags.put(Tags.SPAN_KIND.getKey(), "server-receive");
            tags.put(Constants.CONTEXT_NAME_TAG, request.getUri());
            tags.put(Constants.RESOURCE_OP_NAME_TAG, request.getHttpMethod());
            List<Span> span = this.openTracerFactory.buildSpan("Service :- " + Utils.getServiceName(request.getUri()),
                    spanContext, tags, true);
            request.setProperty(REQUEST_SPAN, span);
        }
        return true;
    }

    @Override
    public boolean interceptResponse(Request request, Response response) throws Exception {
        if (openTracerFactory.isTracingEnabled()) {
            OpenTracerFactory.getInstance().finishSpan((List<Span>) request.getProperty(REQUEST_SPAN));
        }
        return true;
    }
}
