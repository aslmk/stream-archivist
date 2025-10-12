CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users (id) NOT NULL,
    provider_id UUID REFERENCES providers (id) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    provider_name provider NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(provider_user_id, provider_name)
);