FROM openjdk:17-slim

WORKDIR /app
COPY build/otel/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
COPY build/otel/opentelemetry-exporter-otlp.jar /app/opentelemetry-exporter-otlp.jar
COPY build/libs/connector.jar /app/connector.jar
