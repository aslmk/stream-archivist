CREATE TABLE IF NOT EXISTS webhook_subscriptions (
    streamer_internal_id UUID NOT NULL,
    streamer_provider_id VARCHAR(255) NOT NULL,
    provider_name VARCHAR(30) NOT NULL,
    subscription_id UUID, -- NULL until the subscription is actually created on the provider's side
    subscription_type VARCHAR(30) NOT NULL,
    subscription_status VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    UNIQUE(streamer_internal_id, subscription_type)
);