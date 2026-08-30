CREATE TABLE credit_card_method (
    id UUID NOT NULL,
    customer_id VARCHAR(255) NOT NULL,
    customer_card_id VARCHAR(255) NOT NULL,

    CONSTRAINT pk_credit_card_method PRIMARY KEY (id),
    CONSTRAINT fk_credit_card_method_payment FOREIGN KEY (id) REFERENCES payment_method (id) ON DELETE CASCADE
);