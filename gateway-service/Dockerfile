FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY common/pom.xml ./common/pom.xml
COPY gateway-service/pom.xml ./gateway-service/pom.xml

WORKDIR /build/gateway-service
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

RUN adduser --system gateway-user && addgroup --system gateway-group && adduser gateway-user gateway-group
USER gateway-user

WORKDIR /app
COPY --from=builder /build/gateway-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]