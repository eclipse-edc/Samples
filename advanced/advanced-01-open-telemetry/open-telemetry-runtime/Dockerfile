FROM openjdk:17-slim

WORKDIR /app
COPY build/libs/opentelemetry-javaagent-2.6.0.jar /app/opentelemetry-javaagent.jar
COPY build/libs/opentelemetry-exporter-otlp-1.40.0.jar /app/opentelemetry-exporter-otlp.jar
COPY build/libs/connector.jar /app/connector.jar
