FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY platform-common/pom.xml platform-common/pom.xml
COPY orchestration-service/pom.xml orchestration-service/pom.xml
COPY execution-service/pom.xml execution-service/pom.xml
COPY registry-service/pom.xml registry-service/pom.xml
COPY recruiting-mcp-server/pom.xml recruiting-mcp-server/pom.xml

COPY platform-common/src platform-common/src
COPY orchestration-service/src orchestration-service/src

RUN mvn -pl orchestration-service -am -DskipTests package

FROM eclipse-temurin:21-jre
RUN addgroup --system app && adduser --system --ingroup app app
WORKDIR /app

COPY --from=build /workspace/orchestration-service/target/orchestration-service-*.jar app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

