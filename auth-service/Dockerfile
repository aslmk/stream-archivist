FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY common/pom.xml ./common/pom.xml
COPY auth-service/pom.xml ./auth-service/pom.xml

WORKDIR /build/auth-service
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

RUN adduser --system auth-user && addgroup --system auth-group && adduser auth-user auth-group
USER auth-user

WORKDIR /app
COPY --from=builder /build/auth-service/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]