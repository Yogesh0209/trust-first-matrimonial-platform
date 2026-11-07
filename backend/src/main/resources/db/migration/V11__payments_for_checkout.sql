ALTER TABLE payments
    MODIFY subscription_id BIGINT NULL,
    ADD COLUMN bureau_id BIGINT NULL,
    ADD COLUMN plan_id BIGINT NULL,
    ADD COLUMN months INT NULL;