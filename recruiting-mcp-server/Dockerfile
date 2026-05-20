FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY platform-common/pom.xml platform-common/pom.xml
COPY orchestration-service/pom.xml orchestration-service/pom.xml
COPY execution-service/pom.xml execution-service/pom.xml
COPY registry-service/pom.xml registry-service/pom.xml
COPY recruiting-mcp-server/pom.xml recruiting-mcp-server/pom.xml

COPY recruiting-mcp-server/src recruiting-mcp-server/src

RUN mvn -pl recruiting-mcp-server -am -DskipTests package

FROM eclipse-temurin:21-jre
RUN addgroup --system app && adduser --system --ingroup app app
WORKDIR /app

COPY --from=build /workspace/recruiting-mcp-server/target/recruiting-mcp-server-*.jar app.jar

USER app
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

