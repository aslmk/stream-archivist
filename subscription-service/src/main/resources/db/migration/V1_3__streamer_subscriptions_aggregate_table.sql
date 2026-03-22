CREATE TABLE IF NOT EXISTS streamer_subscriptions_aggregate (
    id UUID PRIMARY KEY,
    streamer_id UUID NOT NULL,
    subscriptions_count INTEGER NOT NULL
);