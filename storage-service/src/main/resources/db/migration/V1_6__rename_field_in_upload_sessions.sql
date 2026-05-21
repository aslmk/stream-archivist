ALTER TABLE upload_sessions RENAME COLUMN s3_object_path TO s3_object_key;
ALTER TABLE upload_sessions ADD CONSTRAINT upload_sessions_s3_key_unique UNIQUE (s3_object_key);
