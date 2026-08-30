ALTER TABLE users
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN disabled_at TIMESTAMP;

ALTER TABLE subscription_plan
    ADD COLUMN disabled_at TIMESTAMP;

ALTER TABLE payment_method
    ADD COLUMN disabled_at TIMESTAMP;

ALTER TABLE email_content
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN disabled_at TIMESTAMP;

ALTER TABLE email_campaign
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN disabled_at TIMESTAMP;

ALTER TABLE email_campaign_review
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN disabled_at TIMESTAMP;