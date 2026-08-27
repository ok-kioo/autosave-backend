CREATE TABLE email_campaign(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    text_preview VARCHAR(255) NOT NULL,
    email_content_id UUID NOT NULL,
    is_available BOOLEAN NOT NULL,

    CONSTRAINT email_content_id_fk FOREIGN KEY (email_content_id) REFERENCES email_content(id) ON DELETE CASCADE
);