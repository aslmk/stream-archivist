CREATE TABLE IF NOT EXISTS stream_tracking_subscriptions (
    subscription_id UUID PRIMARY KEY,
    streamer_id UUID NOT NULL,
    subscription_type VARCHAR(30) NOT NULL,
    provider_name VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL
);