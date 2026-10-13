-- V5: align plans with FRD v2.0 (Basic 50 / Pro 500 / Enterprise unlimited)
UPDATE plans SET name = 'Basic', price_per_month = 0, member_limit = 50, gov_id_verification = FALSE WHERE name = 'Free';
UPDATE plans SET member_limit = 500 WHERE name = 'Pro';
UPDATE plans SET name = 'Enterprise', member_limit = NULL WHERE name = 'Premium';