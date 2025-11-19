FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY common/pom.xml ./common/pom.xml

COPY gateway-service/pom.xml gateway-service/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY tracker-service/pom.xml tracker-service/pom.xml
COPY storage-service/pom.xml storage-service/pom.xml
COPY recording-worker/pom.xml recording-worker/pom.xml
COPY recording-orchestrator-service/pom.xml recording-orchestrator-service/pom.xml

COPY uploading-worker/pom.xml ./uploading-worker/pom.xml

RUN --mount=type=cache,target=/root/.m2 mvn -pl uploading-worker -am dependency:go-offline

COPY common ./common
COPY uploading-worker ./uploading-worker

RUN --mount=type=cache,target=/root/.m2 mvn -pl uploading-worker -am clean package -DskipTests

FROM eclipse-temurin:21-jre

RUN adduser --system uploading-user && addgroup --system uploading-group && adduser uploading-user uploading-group
USER uploading-user

WORKDIR /app
COPY --from=builder /build/uploading-worker/target/*.jar app.jar

EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]