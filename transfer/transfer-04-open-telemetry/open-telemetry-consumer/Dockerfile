FROM gradle:jdk17 AS build

WORKDIR /home/gradle/project/
COPY --chown=gradle:gradle . /home/gradle/project/
RUN gradle transfer:transfer-04-open-telemetry:open-telemetry-consumer:build

FROM openjdk:17-slim

WORKDIR /app
COPY --from=build /home/gradle/project/transfer/transfer-04-open-telemetry/opentelemetry-javaagent.jar /app/libs/opentelemetry-javaagent.jar
COPY --from=build /home/gradle/project/transfer/transfer-04-open-telemetry/open-telemetry-consumer/build/libs/consumer.jar /app/connector.jar