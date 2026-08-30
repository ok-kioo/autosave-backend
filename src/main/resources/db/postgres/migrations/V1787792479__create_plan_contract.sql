CREATE TABLE plan_contract(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    subscription_plan_id UUID NOT NULL,
    payment_method_id UUID NOT NULL,
    contract_id VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    is_recurring BOOLEAN NOT NULL,
    started_at DATE NOT NULL DEFAULT CURRENT_DATE,
    ends_at DATE NOT NULL,

    CONSTRAINT contract_subscription_id_fk FOREIGN KEY (subscription_plan_id) REFERENCES subscription_plan(id),
    CONSTRAINT contract_payment_method_id_fk FOREIGN KEY (payment_method_id) REFERENCES payment_method(id),
    CONSTRAINT contract_status_check CHECK ( status IN ('PENDING', 'PAID', 'FAILED', 'CANCELED', 'REFUNDED') )
);