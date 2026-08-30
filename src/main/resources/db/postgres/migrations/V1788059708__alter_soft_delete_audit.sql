CREATE OR REPLACE FUNCTION fn_soft_delete_cascade_user()
RETURNS TRIGGER AS $$
BEGIN

UPDATE email_content
SET is_active = FALSE,
    disabled_at = NEW.disabled_at
WHERE editor_id = NEW.id
  AND is_active = TRUE;

UPDATE payment_method
SET is_active = FALSE,
    disabled_at = NEW.disabled_at
WHERE user_id = NEW.id
  AND is_active = TRUE;


RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ####

DROP TRIGGER IF EXISTS trg_soft_delete_user
ON users;

CREATE TRIGGER trg_soft_delete_user
    AFTER UPDATE OF is_active, disabled_at ON users
    FOR EACH ROW
    WHEN (
        OLD.is_active = TRUE
            AND NEW.is_active = FALSE
        )
    EXECUTE FUNCTION fn_soft_delete_cascade_user();

CREATE OR REPLACE FUNCTION fn_soft_delete_cascade_email_content()
RETURNS TRIGGER AS $$
BEGIN

UPDATE email_campaign
SET disabled_at = NEW.disabled_at
WHERE email_content_id = NEW.id
  AND disabled_at IS NULL;


RETURN NEW;
END;
$$ LANGUAGE plpgsql;

   -- ####


DROP TRIGGER IF EXISTS trg_soft_delete_email_content
ON email_content;

CREATE TRIGGER trg_soft_delete_email_content
    AFTER UPDATE OF is_active, disabled_at ON email_content
    FOR EACH ROW
    WHEN (
        OLD.is_active = TRUE
            AND NEW.is_active = FALSE
        )
    EXECUTE FUNCTION fn_soft_delete_cascade_email_content();


CREATE OR REPLACE FUNCTION fn_soft_delete_cascade_email_campaign()
RETURNS TRIGGER AS $$
BEGIN

UPDATE email_campaign_review
SET is_active = FALSE,
    disabled_at = NEW.disabled_at
WHERE email_campaign_id = NEW.id
  AND is_active = TRUE;


RETURN NEW;
END;
$$ LANGUAGE plpgsql;

   -- ####

DROP TRIGGER IF EXISTS trg_soft_delete_email_campaign
ON email_campaign;

CREATE TRIGGER trg_soft_delete_email_campaign
    AFTER UPDATE OF disabled_at ON email_campaign
    FOR EACH ROW
    WHEN (
        OLD.disabled_at IS NULL
            AND NEW.disabled_at IS NOT NULL
        )
    EXECUTE FUNCTION fn_soft_delete_cascade_email_campaign();