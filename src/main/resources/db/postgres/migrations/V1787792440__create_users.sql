CREATE TABLE users (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    plan_contract_id UUID NOT NULL,

    CONSTRAINT users_role_check CHECK (role IN ('ADMIN', 'EDITOR', 'REVIEWER', 'VIEWER', 'BILLING_MANAGER'))
);

CREATE UNIQUE INDEX user_unique_name_idx ON users(name);
CREATE UNIQUE INDEX user_unique_email_idx ON users(email);

