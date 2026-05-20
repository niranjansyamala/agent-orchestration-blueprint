package com.recruiting.platform.orchestration.observability;

import com.langchain.smith.otel.OtelTraceExporter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class LangSmithTracingService {

    private static final Logger log = LoggerFactory.getLogger(LangSmithTracingService.class);

    private final Optional<Tracer> tracer;

    public LangSmithTracingService() {
        Optional<Tracer> localTracer;
        try {
            localTracer = Optional.of(OtelTraceExporter.fromEnv().getTracer());
        } catch (Exception ex) {
            log.info("LangSmith tracing not configured; orchestration service will continue without remote traces");
            localTracer = Optional.empty();
        }
        this.tracer = localTracer;
    }

    public <T> T inSpan(String spanName, Map<String, String> attributes, Supplier<T> supplier) {
        if (tracer.isEmpty()) {
            return supplier.get();
        }
        Span span = tracer.get().spanBuilder(spanName).startSpan();
        try {
            attributes.forEach(span::setAttribute);
            T result = supplier.get();
            span.setStatus(StatusCode.OK);
            return result;
        } catch (RuntimeException ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR, ex.getMessage());
            throw ex;
        } finally {
            span.end();
        }
    }
}
