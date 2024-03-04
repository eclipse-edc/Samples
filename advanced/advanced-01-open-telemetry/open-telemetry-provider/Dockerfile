FROM gradle:jdk17 AS build

WORKDIR /home/gradle/project/
COPY --chown=gradle:gradle . /home/gradle/project/
RUN gradle advanced:advanced-01-open-telemetry:open-telemetry-provider:build

FROM openjdk:17-slim

WORKDIR /app
COPY --from=build /home/gradle/project/advanced/advanced-01-open-telemetry/open-telemetry-provider/build/libs/opentelemetry-javaagent-*.jar /app/opentelemetry-javaagent.jar
COPY --from=build /home/gradle/project/advanced/advanced-01-open-telemetry/open-telemetry-provider/build/libs/connector.jar /app/connector.jar
