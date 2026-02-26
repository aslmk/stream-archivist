FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

RUN adduser --system storage-user && addgroup --system storage-group && adduser storage-user storage-group
USER storage-user

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]