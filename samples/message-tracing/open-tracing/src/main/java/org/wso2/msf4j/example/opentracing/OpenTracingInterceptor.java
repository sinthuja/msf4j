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

import brave.Tracing;
import brave.opentracing.BraveTracer;
import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.shared.Options;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.interceptor.RequestInterceptor;
import org.wso2.msf4j.interceptor.ResponseInterceptor;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.Sender;
import zipkin.reporter.okhttp3.OkHttpSender;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

public class OpenTracingInterceptor implements RequestInterceptor, ResponseInterceptor {
    private static final Logger log = LoggerFactory.getLogger(OpenTracingInterceptor.class);

    private static OpenTracingInterceptor INSTANCE = new OpenTracingInterceptor();
    private static final String REQUEST_SPAN = "REQUEST_SPAN";
    private boolean initialized = false;

    private Tracer tracer;

    private OpenTracingInterceptor() {
        try {
            Properties config = loadConfig();
            if (config != null && !configureGlobalTracer(config, "testcomponent")) {
                throw new Exception("The tracer is not configured correctly .. ");
            }
            this.tracer = GlobalTracer.get();
            this.initialized = true;
        } catch (Exception ex) {
            this.initialized = false;
            log.error("Unable to initialized open tracing interceptor! ", ex);
        }
    }

    public static OpenTracingInterceptor getInstance() throws Exception {
        if (!INSTANCE.initialized) {
            throw new Exception("Tracer is not initialized properly!");
        }
        return INSTANCE;
    }

    @Override
    public boolean interceptRequest(Request request, Response response) throws Exception {
        SpanContext spanContext = this.tracer.extract(Format.Builtin.HTTP_HEADERS,
                new MS4JRequestExtractorAdaptor(request));
        Span span;
        if (spanContext != null) {
            span = this.tracer.
                    buildSpan(request.getUri() + "##" + request.getHttpMethod())
                    .asChildOf(spanContext)
                    .withTag(Tags.SPAN_KIND.getKey(), "server-receive")
                    .startManual();
        } else {
            span = this.tracer.
                    buildSpan(request.getUri() + "##" + request.getHttpMethod())
                    .withTag(Tags.SPAN_KIND.getKey(), "server-receive")
                    .startManual();
        }
        this.tracer.makeActive(span);
        request.setProperty(REQUEST_SPAN, span);
        return true;
    }

    @Override
    public boolean interceptResponse(Request request, Response response) throws Exception {
        Span span = (Span) request.getProperty(REQUEST_SPAN);
        span.finish();
        return true;
    }

    private Properties loadConfig()
            throws IOException {
        String file = "/Users/sinthu/wso2/testing/open-tracing/java-opentracing-walkthrough/microdonuts/tracer_config.properties";
        FileInputStream fs = new FileInputStream(file);
        Properties config = new Properties();
        config.load(fs);
        return config;
    }

    private boolean configureGlobalTracer(Properties config, String componentName)
            throws MalformedURLException {
        String tracerName = config.getProperty("tracer");
        if ("jaeger".equals(tracerName)) {
            GlobalTracer.register(
                    new com.uber.jaeger.Configuration(
                            componentName,
                            new com.uber.jaeger.Configuration.SamplerConfiguration("const", 1),
                            new com.uber.jaeger.Configuration.ReporterConfiguration(
                                    true,  // logSpans
                                    config.getProperty("jaeger.reporter_host"),
                                    Integer.decode(config.getProperty("jaeger.reporter_port")),
                                    1000,   // flush interval in milliseconds
                                    10000)  // max buffered Spans
                    ).getTracer());
        } else if ("zipkin".equals(tracerName)) {
            Sender sender = OkHttpSender.create(
                    "http://" +
                            config.getProperty("zipkin.reporter_host") + ":" +
                            config.getProperty("zipkin.reporter_port") + "/api/v1/spans");
            Reporter reporter = AsyncReporter.builder(sender).build();
            GlobalTracer.register(BraveTracer.create(Tracing.newBuilder()
                    .localServiceName(componentName)
                    .reporter(reporter)
                    .build()));
        } else if ("lightstep".equals(tracerName)) {
            Options opts = new Options.OptionsBuilder()
                    .withAccessToken(config.getProperty("lightstep.access_token"))
                    .withCollectorHost(config.getProperty("lightstep.collector_host"))
                    .withCollectorPort(Integer.decode(config.getProperty("lightstep.collector_port")))
                    .withComponentName(componentName)
                    .build();
            Tracer tracer = new JRETracer(opts);
            GlobalTracer.register(tracer);
        } else {
            return false;
        }
        return true;
    }
}
