CREATE TABLE pix_method (
    id UUID NOT NULL,
    customer_id VARCHAR(255),

    CONSTRAINT pk_pix_method PRIMARY KEY (id),
    CONSTRAINT fk_pix_method_payment FOREIGN KEY (id) REFERENCES payment_method (id) ON DELETE CASCADE
);