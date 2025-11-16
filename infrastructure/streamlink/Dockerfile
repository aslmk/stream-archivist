FROM python:3.12-alpine AS builder

RUN apk add --no-cache \
    gcc \
    musl-dev \
    libffi-dev \
    zlib-dev \
    cargo \
    make

RUN pip install --no-cache-dir streamlink

FROM alpine:3.20

RUN apk add --no-cache \
    ffmpeg \
    bash \
    ca-certificates \
    python3 \
    py3-pip

COPY --from=builder /usr/local /usr/local

WORKDIR /recordings

CMD ["/bin/bash"]