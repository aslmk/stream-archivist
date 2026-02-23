CREATE TABLE IF NOT EXISTS user_subscriptions (
    user_id UUID NOT NULL,
    streamer_id UUID NOT NULL,

    streamer_username VARCHAR(100) NOT NULL,
    streamer_profile_image_url VARCHAR(255) NOT NULL,
    provider_name VARCHAR(50) NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    UNIQUE(user_id, streamer_id)
);