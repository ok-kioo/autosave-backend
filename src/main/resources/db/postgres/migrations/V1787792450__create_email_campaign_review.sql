CREATE TABLE email_campaign_review (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    comment VARCHAR(255) NOT NULL,
    email_campaign_id UUID NOT NULL,
    reviewer_id UUID NOT NULL,

    CONSTRAINT email_campaign_id_fk FOREIGN KEY (email_campaign_id) REFERENCES email_campaign(id) ON DELETE CASCADE,
    CONSTRAINT reviewer_id_fk FOREIGN KEY (reviewer_id) REFERENCES users(id),
    CONSTRAINT email_campaign_status_check CHECK ( status IN ('APPROVED', 'REJECTED', 'UPDATED', 'PENDING') )
);