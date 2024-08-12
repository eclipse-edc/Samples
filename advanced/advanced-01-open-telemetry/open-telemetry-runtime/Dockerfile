FROM openjdk:17-slim

WORKDIR /app
COPY build/libs/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
COPY build/libs/opentelemetry-exporter-otlp.jar /app/opentelemetry-exporter-otlp.jar
COPY build/libs/connector.jar /app/connector.jar
