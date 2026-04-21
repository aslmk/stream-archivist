# Stream-Archivist

> Automatic Twitch stream recorder — set it and forget it.

![Java](https://img.shields.io/badge/Java-Spring%20Boot-6DB33F?logo=springboot&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-Tailwind-DD0031?logo=angular&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-336791?logo=postgresql&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-231F20?logo=apachekafka&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?logo=rabbitmq&logoColor=white)
![S3](https://img.shields.io/badge/S3-Object%20Storage-569A31?logo=amazons3&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?logo=grafana&logoColor=white)


## Overview

Stream-Archivist is a self-hosted service for recording Twitch streams automatically.

You provide a streamer's nickname — the system monitors their channel and starts recording the moment they go live. No manual actions required after the streamer is added.

If the system fails during a stream, part of the recording may be lost, but recording will continue once it is restored.


## Architecture

The system is built as a set of loosely coupled services connected through HTTP APIs and message queues.

User requests first reach an Nginx reverse proxy, which serves the Angular frontend and routes API requests to the API Gateway. The gateway then forwards requests to dedicated backend services.

Stream lifecycle events are propagated asynchronously through Kafka and RabbitMQ, enabling both background processing (recording, uploading) and real-time stream status delivery to clients.

![Architecture Overview](docs/architecture.png)

**Main components:**

- **Frontend (Angular)** — provides the user interface and runs in the user's browser.
- **Nginx** — serves frontend static files and routes API requests to the API Gateway.
- **API gateway** — routes incoming requests to backend services.
- **auth-service** — handles authentication via Twitch OAuth2. Processes OAuth callbacks and issues JWT-based sessions stored in cookies.
- **subscription-service** — acts as the entry point for streamer subscriptions and orchestrates streamer tracking by delegating to tracker-service.
- **stream-status-service** — aggregates stream lifecycle events and exposes real-time stream status updates to clients via Server-Sent Events (SSE).
- **tracker-service** — stores streamer information, manages Twitch webhook subscriptions, and detects stream start/end events from Twitch.
- **recording-orchestrator-service** — handles stream and recorded parts lifecycle events. Orchestrates the recording and upload pipeline via RabbitMQ and persists recorded parts.
- **recording-worker** — records live streams to local disk and emits a completion event after the stream ends.
- **uploading-worker** — uploads recorded files to S3 in a resumable and idempotent manner using pre-signed URLs.
- **storage-service** — manages multipart uploads, determines missing parts, issues pre-signed URLs, and finalizes uploads.
- **Temporary storage** — used only as a buffering layer before uploading to object storage.

## Observability

The stack includes a pre-configured observability setup that starts automatically with `docker compose up`.

All Grafana datasources and dashboards are provisioned automatically. No manual setup required.

| Tool       | Purpose                                              | URL                      |
|------------|------------------------------------------------------|--------------------------|
| Prometheus | Metrics collection                                   | `http://localhost:9090`  |
| Loki       | Log aggregation                                      | `http://localhost:3100`  |
| Tempo      | Distributed tracing                                  | `http://localhost:3200`  |
| Grafana    | Dashboards                                           | `http://localhost:3000`  |
| Alloy      | Unified telemetry collector (metrics, logs, traces)  | -                        |


## How to Run

### Prerequisites

This project relies on Twitch integration for its core functionality, so proper configuration is required before running it.

Ensure the following tools are installed:
- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)

You also need to obtain the following Twitch-related credentials:

| Variable               | Description                                                                           |
|------------------------|---------------------------------------------------------------------------------------|
| `TWITCH_CLIENT_ID`     | Your Twitch application client ID                                                     |
| `TWITCH_CLIENT_SECRET` | Your Twitch application client secret                                                 |
| `TWITCH_WEBHOOK_URL`   | A publicly accessible URL for Twitch webhooks (e.g., via [ngrok](https://ngrok.com/)) |

> Without these, the system will start but will not be functional.

### 1. Clone the repository

```bash
git clone https://github.com/aslmk/stream-archivist
cd stream-archivist
```

### 2. Configure environment variables

```bash
cp .env.example .env
```

Open `.env` and fill in all required values, especially the Twitch credentials listed above.

### 3. Run the system

```bash
docker compose up --build -d
```

The application will be available at `http://localhost`.