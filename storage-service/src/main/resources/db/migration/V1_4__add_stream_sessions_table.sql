CREATE TABLE IF NOT EXISTS stream_sessions (
    stream_id UUID PRIMARY KEY,
    upload_id VARCHAR(255) UNIQUE NOT NULL,
    s3_object_key VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL
);