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
package org.wso2.msf4j.client.tracing;

import feign.Client;
import feign.Request;
import feign.Response;
import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import org.wso2.msf4j.client.FeignClientWrapper;
import org.wso2.msf4j.opentracing.core.Constants;
import org.wso2.msf4j.opentracing.core.OpenTracerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is client is a warpper client for the open tracing standard implementation.
 */
public class FeignOpenTracingClient extends FeignClientWrapper {
    private final Client clientDelegate;
    private String instanceName;
    private OpenTracerFactory openTracerFactory;

    public FeignOpenTracingClient(Client client, String instanceName) {
        super(client);
        this.clientDelegate = client;
        this.instanceName = instanceName;
        this.openTracerFactory = OpenTracerFactory.getInstance();
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        if (this.openTracerFactory.isTracingEnabled()) {
            Map<String, Object> parentSpan = this.openTracerFactory.getActiveSpans();
            Map<String, String> tags = new HashMap<>();
            tags.put(Tags.SPAN_KIND.getKey(), "client-send");
            tags.put(Constants.CONTEXT_NAME_TAG, request.url());
            tags.put(Constants.RESOURCE_OP_NAME_TAG, request.method());
            List<Span> span = this.openTracerFactory.
                    buildSpan("Client :- " + instanceName, parentSpan, tags, true);
            Map<String, ActiveSpan> activeSpanMap = this.openTracerFactory.getActiveSpans(parentSpan.keySet());
            MS4JRequestInjectorAdaptor requestInjectorAdaptor = new MS4JRequestInjectorAdaptor(request);
            this.openTracerFactory.inject(activeSpanMap, Format.Builtin.HTTP_HEADERS, requestInjectorAdaptor);
            Request wrappedRequest =
                    Request.create(request.method(), request.url(), requestInjectorAdaptor.getHeaders(),
                            request.body(), request.charset());

            Response response = clientDelegate.execute(wrappedRequest, options);

            this.openTracerFactory.finishSpan(span, parentSpan);
            return response;
        } else {
            return super.execute(request, options);
        }
    }
}
