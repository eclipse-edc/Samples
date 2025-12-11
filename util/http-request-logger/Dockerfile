FROM gradle:jdk17 AS build

WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . /home/gradle/project
RUN gradle build

FROM eclipse-temurin:25-alpine

WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/http-request-logger.jar /app/http-request-logger.jar

CMD ["java","-jar","/app/http-request-logger.jar"]
