FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY common/pom.xml ./common/pom.xml
COPY recording-orchestrator-service/pom.xml ./recording-orchestrator-service/pom.xml

WORKDIR /build/recording-orchestrator-service
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

RUN adduser --system orchestrator-user && addgroup --system orchestrator-group && adduser orchestrator-user orchestrator-group
USER orchestrator-user

WORKDIR /app
COPY --from=builder /build/recording-orchestrator-service/target/*.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]