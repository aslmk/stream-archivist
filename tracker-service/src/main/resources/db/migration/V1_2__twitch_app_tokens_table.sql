CREATE TABLE IF NOT EXISTS twitch_app_tokens (
    id UUID PRIMARY KEY,
    access_token VARCHAR(100) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    token_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);