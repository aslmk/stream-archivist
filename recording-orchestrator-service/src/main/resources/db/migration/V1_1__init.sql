CREATE TABLE IF NOT EXISTS recorded_file_parts (
    stream_id UUID NOT NULL,
    part_index INTEGER NOT NULL,
    file_part_name VARCHAR(255) NOT NULL,
    file_part_path VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(stream_id, part_index)
);