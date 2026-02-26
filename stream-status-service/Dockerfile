FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

RUN adduser --system stream-status-user && addgroup --system stream-status-group && adduser stream-status-user stream-status-group
USER stream-status-user

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]