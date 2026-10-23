ALTER TABLE interests ADD COLUMN note VARCHAR(200) NULL;
ALTER TABLE interests MODIFY COLUMN status ENUM('PENDING','ACCEPTED','DECLINED','EXPIRED') NOT NULL DEFAULT 'PENDING';
ALTER TABLE plans ADD COLUMN cross_bureau_search BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE plans SET cross_bureau_search = TRUE WHERE name IN ('Pro','Enterprise');
UPDATE plans SET gov_id_verification = TRUE WHERE name IN ('Pro','Enterprise');