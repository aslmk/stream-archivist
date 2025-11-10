CREATE TABLE IF NOT EXISTS streamers (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    provider_name VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(provider_user_id, provider_name)
);
