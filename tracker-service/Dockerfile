FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY common/pom.xml ./common/pom.xml
COPY tracker-service/pom.xml ./tracker-service/pom.xml

WORKDIR /build/tracker-service
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

RUN adduser --system tracker-user && addgroup --system tracker-group && adduser tracker-user tracker-group
USER tracker-user

WORKDIR /app
COPY --from=builder /build/tracker-service/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]