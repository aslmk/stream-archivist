FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache \
    python3 \
    ca-certificates \
    py3-pip \
    bash \
    gcc \
    musl-dev \
    libffi-dev \
    zlib-dev \
    cargo \
    make \
    su-exec \
    ffmpeg

RUN pip3 install --no-cache-dir --break-system-packages streamlink

RUN adduser --system recording-user && addgroup --system recording-group && adduser recording-user recording-group

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8085

ENTRYPOINT ["/entrypoint.sh"]