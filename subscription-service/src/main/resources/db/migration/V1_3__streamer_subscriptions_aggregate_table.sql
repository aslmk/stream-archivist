CREATE TABLE IF NOT EXISTS streamer_subscriptions_aggregate (
    streamer_id UUID PRIMARY KEY,
    subscriptions_count INTEGER NOT NULL
);