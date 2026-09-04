CREATE TABLE payment_method (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(50) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    document_number VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    user_id UUID NOT NULL,

    CONSTRAINT fk_payment_method_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT payment_method_type_check CHECK ( type IN ('PIX', 'CREDIT_CARD') )
);