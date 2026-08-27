CREATE TABLE email_campaign_subscription_plan (
    email_campaign_id UUID NOT NULL,
    subscription_plan_id UUID NOT NULL,

    CONSTRAINT pk_email_campaign_subscription_plan PRIMARY KEY (email_campaign_id, subscription_plan_id),
    CONSTRAINT fk_ecsp_email_campaign FOREIGN KEY (email_campaign_id) REFERENCES email_campaign (id) ON DELETE CASCADE,
    CONSTRAINT fk_ecsp_subscription_plan FOREIGN KEY (subscription_plan_id) REFERENCES subscription_plan (id) ON DELETE CASCADE
);