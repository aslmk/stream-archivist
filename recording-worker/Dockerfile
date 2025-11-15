FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY common/pom.xml ./common/pom.xml
COPY recording-worker/pom.xml ./recording-worker/pom.xml

WORKDIR /build/recording-worker
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

RUN adduser --system recording-user && addgroup --system recording-group && adduser recording-user recording-group
USER recording-user

WORKDIR /app
COPY --from=builder /build/recording-worker/target/*.jar app.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]