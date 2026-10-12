-- V3: Add registration code to bureaus and account lockout columns to users

ALTER TABLE `bureaus`
ADD COLUMN `bureau_code` VARCHAR(20) UNIQUE AFTER `name`;

ALTER TABLE `users`
ADD COLUMN `failed_login_attempts` INT NOT NULL DEFAULT 0 AFTER `is_blocked`,
ADD COLUMN `locked_until` DATETIME NULL AFTER `failed_login_attempts`;
