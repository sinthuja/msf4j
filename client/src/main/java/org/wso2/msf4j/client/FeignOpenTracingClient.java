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

import feign.Client;
import feign.Request;
import feign.Response;
import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;

import java.io.IOException;

/**
 * This is client is a warpper client for the open tracing standard implementation.
 */
public class FeignOpenTracingClient implements Client {
    private final Client clientDelegate;
    private String instanceName;
    private Tracer tracer;

    public FeignOpenTracingClient(Client client, String instanceName) {
        this.clientDelegate = client;
        this.instanceName = instanceName;
        this.tracer = GlobalTracer.get();
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        ActiveSpan parentSpan = this.tracer.activeSpan();
        Span span;
        if (parentSpan != null) {
            span = this.tracer.
                    buildSpan(instanceName + "##" + request.url() + "##" + request.method())
                    .withTag(Tags.SPAN_KIND.getKey(), "client-send")
                    .asChildOf(parentSpan)
                    .startManual();
        } else {
            span = this.tracer
                    .buildSpan(instanceName + "##" + request.url() + "##" + request.method())
                    .withTag(Tags.SPAN_KIND.getKey(), "client-send")
                    .startManual();
        }
        ActiveSpan requestSpan = this.tracer.makeActive(span);
        MS4JRequestInjectorAdaptor requestInjectorAdaptor = new MS4JRequestInjectorAdaptor(request);
        this.tracer.inject(requestSpan.context(), Format.Builtin.HTTP_HEADERS, requestInjectorAdaptor);

        Request wrappedRequest =
                Request.create(request.method(), request.url(), requestInjectorAdaptor.getHeaders(),
                        request.body(), request.charset());

        Response response = clientDelegate.execute(wrappedRequest, options);

        span.finish();
        if (parentSpan != null) {
            parentSpan.capture().activate();
        }
        return response;
    }
}
