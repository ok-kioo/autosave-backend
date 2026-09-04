CREATE TABLE payment_payload(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    amount NUMERIC(19, 2) NOT NULL,
    payment_id BIGINT NOT NULL,
    type VARCHAR(255) NOT NULL,
    plan_contract_id UUID NOT NULL,

    CONSTRAINT payment_payload_contract_fk FOREIGN KEY (plan_contract_id) REFERENCES plan_contract(id),
    CONSTRAINT payment_payload_type_check CHECK ( type IN ('REFUND', 'PAYMENT') )
);