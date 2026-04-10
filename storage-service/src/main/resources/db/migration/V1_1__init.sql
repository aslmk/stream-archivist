CREATE TABLE upload_sessions (
    s3_object_path VARCHAR(255) PRIMARY KEY,
    upload_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);