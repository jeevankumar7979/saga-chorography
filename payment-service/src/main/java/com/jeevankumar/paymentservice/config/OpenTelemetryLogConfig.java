package com.jeevankumar.paymentservice.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OpenTelemetryLogConfig {

    private final OpenTelemetry openTelemetry;

    public OpenTelemetryLogConfig(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void installAppender() {
        OpenTelemetryAppender.install(openTelemetry);
    }
}
