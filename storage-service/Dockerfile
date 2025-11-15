FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY common/pom.xml ./common/pom.xml
COPY storage-service/pom.xml ./storage-service/pom.xml

WORKDIR /build/storage-service
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

RUN adduser --system storage-user && addgroup --system storage-group && adduser storage-user storage-group
USER storage-user

WORKDIR /app
COPY --from=builder /build/storage-service/target/*.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]