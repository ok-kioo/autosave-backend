CREATE TABLE subscription_plan (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(19, 2) NOT NULL,
    billing_cycle VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    trial_days INTEGER NOT NULL,
    preapproval_plan_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT subscription_billing_check CHECK ( billing_cycle IN ('months', 'years') )
);

CREATE UNIQUE INDEX subscription_unique_name_idx ON subscription_plan(name);
CREATE UNIQUE INDEX subscription_unique_preapproval_idx ON subscription_plan(preapproval_plan_id);