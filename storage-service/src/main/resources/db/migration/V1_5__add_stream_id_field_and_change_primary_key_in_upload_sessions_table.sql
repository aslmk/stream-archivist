 ALTER TABLE upload_sessions
    ADD COLUMN stream_id UUID NOT NULL,
    DROP CONSTRAINT upload_sessions_pkey,
    ADD PRIMARY KEY (stream_id);
