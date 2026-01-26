FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY common/pom.xml ./common/pom.xml

COPY gateway-service/pom.xml gateway-service/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY tracker-service/pom.xml tracker-service/pom.xml
COPY storage-service/pom.xml storage-service/pom.xml
COPY recording-worker/pom.xml recording-worker/pom.xml
COPY uploading-worker/pom.xml uploading-worker/pom.xml
COPY subscription-service/pom.xml subscription-service/pom.xml

COPY recording-orchestrator-service/pom.xml ./recording-orchestrator-service/pom.xml


COPY common ./common
COPY recording-orchestrator-service ./recording-orchestrator-service

RUN --mount=type=cache,target=/root/.m2 mvn -pl recording-orchestrator-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre

RUN adduser --system orchestrator-user && addgroup --system orchestrator-group && adduser orchestrator-user orchestrator-group
USER orchestrator-user

WORKDIR /app
COPY --from=builder /build/recording-orchestrator-service/target/*.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]