CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    streamer_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(user_id, streamer_id)
);