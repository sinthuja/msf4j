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

import io.opentracing.ActiveSpan;
import io.opentracing.BaseSpan;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.msf4j.opentracing.core.config.InvalidConfigurationException;
import org.wso2.msf4j.opentracing.core.config.OpenTracingConfig;
import org.wso2.msf4j.opentracing.core.config.TracerConfig;
import org.wso2.msf4j.opentracing.core.internal.DataHolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.wso2.msf4j.internal.MSF4JConstants.DEPLOYMENT_YAML_FILE;
import static org.wso2.msf4j.internal.MSF4JConstants.DEPLOYMENT_YAML_SYS_PROPERTY;

/**
 * This is the class which holds the tracers that are enabled, and bridges all tracers with instrumented code.
 */
public class OpenTracerFactory {

    private static final Logger logger = LoggerFactory.getLogger(OpenTracerFactory.class);
    private static OpenTracerFactory instance = new OpenTracerFactory();
    private OpenTracingConfig openTracingConfig;
    private Map<String, Tracer> tracers;

    private OpenTracerFactory() {
        try {
            setConfiguration();
            this.tracers = new HashMap<>();
            loadTracers();
        } catch (ConfigurationException | IllegalAccessException
                | InstantiationException | ClassNotFoundException | InvalidConfigurationException ex) {
            logger.error("Error while loading the open tracing configuration, " +
                    "failed to initialize the tracer instance", ex);
        }
    }


    public static OpenTracerFactory getInstance() {
        return instance;
    }

    public boolean isTracingEnabled() {
        return !this.tracers.isEmpty();
    }

    public TracerConfig getTracingConfig(String tracerName) {
        return this.openTracingConfig.getTracer(tracerName);
    }

    private void register(String tracerName, Tracer tracer) {
        TracerConfig tracerConfig = getTracingConfig(tracerName);
        if (tracerConfig.isEnabled() && this.tracers.get(tracerName.toLowerCase(Locale.ENGLISH)) == null) {
            this.tracers.put(tracerName.toLowerCase(Locale.ENGLISH), tracer);
        }
    }

    private void loadTracers() throws ClassNotFoundException, IllegalAccessException, InstantiationException
            , InvalidConfigurationException {
        for (TracerConfig tracerConfig : this.openTracingConfig.getTracers()) {
            if (tracerConfig.isEnabled()) {
                Class<?> openTracerClass = Class.forName(tracerConfig.getClassName()).asSubclass(OpenTracer.class);
                OpenTracer openTracer = (OpenTracer) openTracerClass.newInstance();
                Tracer tracer = openTracer.getTracer(tracerConfig.getName(),
                        tracerConfig.getConfiguration());
                register(tracerConfig.getName(), tracer);
            }
        }
    }

    private void setConfiguration() throws ConfigurationException {
        ConfigProvider configProvider = getConfigurationProvider();
        this.openTracingConfig = configProvider.getConfigurationObject(OpenTracingConfig.class);
    }

    private static ConfigProvider getConfigurationProvider() {
        ConfigProvider configProvider = DataHolder.getConfigProvider();
        if (configProvider == null) {
            if (DataHolder.getBundleContext() != null) {
                throw new RuntimeException(
                        "Failed to populate open tracing relared configuration since the config provider is not " +
                                "registered in the OSGi environment");
            }
            //Standalone mode
            String deploymentYamlPath = System.getProperty(DEPLOYMENT_YAML_SYS_PROPERTY);
            if (deploymentYamlPath == null || deploymentYamlPath.isEmpty()) {
                logger.info("System property '" + DEPLOYMENT_YAML_SYS_PROPERTY +
                        "' is not set. Default deployment.yaml file will be used.");
                deploymentYamlPath = DEPLOYMENT_YAML_FILE;
                try (InputStream configInputStream = OpenTracerFactory.class.getClassLoader()
                        .getResourceAsStream(DEPLOYMENT_YAML_FILE)) {
                    if (configInputStream == null) {
                        throw new RuntimeException("Couldn't find " + deploymentYamlPath);
                    }
                    if (Files.notExists(Paths.get(deploymentYamlPath))) {
                        Files.copy(configInputStream, Paths.get(deploymentYamlPath));
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Couldn't read configuration from file " + deploymentYamlPath, e);
                }
            } else if (!Files.exists(Paths.get(deploymentYamlPath))) {
                throw new RuntimeException("Couldn't find " + deploymentYamlPath);
            }

            try {
                configProvider = ConfigProviderFactory.getConfigProvider(Paths.get(deploymentYamlPath), null);
            } catch (ConfigurationException e) {
                throw new RuntimeException("Error loading deployment.yaml Configuration", e);
            }
        }
        return configProvider;
    }

    public Map<String, Object> extract(Format<TextMap> format, TextMap carrier) {
        Map<String, Object> spanContext = new HashMap<>();
        for (Map.Entry<String, Tracer> tracerEntry : this.tracers.entrySet()) {
            spanContext.put(tracerEntry.getKey(), tracerEntry.getValue().extract(format, carrier));
        }
        return spanContext;
    }

    public List<Span> buildSpan(String spanName, Map<String, Object> spanContextMap, String spanTag,
                                String spanTagValue, boolean makeActive) {
        List<Span> spanList = new ArrayList<>();
        for (Map.Entry spanContextEntry : spanContextMap.entrySet()) {
            Tracer tracer = this.tracers.get(spanContextEntry.getKey().toString());
            Tracer.SpanBuilder spanBuilder = tracer.buildSpan(spanName)
                    .withTag(spanTag, spanTagValue);
            if (spanContextEntry.getValue() != null) {
                if (spanContextEntry.getValue() instanceof SpanContext) {
                    spanBuilder = spanBuilder.asChildOf((SpanContext) spanContextEntry.getValue());
                } else if (spanContextEntry.getValue() instanceof BaseSpan) {
                    spanBuilder = spanBuilder.asChildOf((BaseSpan) spanContextEntry.getValue());
                } else {
                    throw new UnknownSpanContextTypeException("Unknown span context field - " +
                            spanContextEntry.getValue().getClass()
                            + "! Open tracing can span can be build only by using "
                            + SpanContext.class + " or " + BaseSpan.class);
                }
            }
            Span span = spanBuilder.startManual();
            if (makeActive) {
                tracer.makeActive(span);
            }
            spanList.add(span);
        }
        return spanList;
    }


    public void finishSpan(List<Span> spanList) {
        spanList.forEach(Span::finish);
    }

    public Map<String, Object> getActiveSpans() {
        Map<String, Object> activeSpanMap = new HashMap<>();
        for (Map.Entry<String, Tracer> tracerEntry : this.tracers.entrySet()) {
            activeSpanMap.put(tracerEntry.getKey(), tracerEntry.getValue().activeSpan());
        }
        return activeSpanMap;
    }

    public Map<String, ActiveSpan> getActiveSpans(Set<String> tracerNames) {
        Map<String, ActiveSpan> activeSpanMap = new HashMap<>();
        for (String tracerName : tracerNames) {
            activeSpanMap.put(tracerName.toLowerCase(Locale.ENGLISH),
                    this.tracers.get(tracerName.toLowerCase(Locale.ENGLISH)).activeSpan());
        }
        return activeSpanMap;
    }


    public void inject(Map<String, ActiveSpan> activeSpanMap, Format<TextMap> format, TextMap carrier) {
        for (Map.Entry<String, ActiveSpan> activeSpanEntry : activeSpanMap.entrySet()) {
            Tracer tracer = this.tracers.get(activeSpanEntry.getKey());
            if (tracer != null) {
                tracer.inject(activeSpanEntry.getValue().context(), format, carrier);
            }
        }
    }

    public void finishSpan(List<Span> span, Map<String, Object> parent) {
        finishSpan(span);
        for (Map.Entry<String, Object> parentSpan : parent.entrySet()) {
            if (parentSpan.getValue() != null) {
                if (parentSpan.getValue() instanceof ActiveSpan) {
                    ((ActiveSpan) parentSpan.getValue()).capture().activate();
                } else {
                    throw new UnknownSpanContextTypeException("Only " + ActiveSpan.class
                            + " as parent span can be captured " +
                            "and activated! But found " + parentSpan.getClass());
                }
            }
        }
    }

}
