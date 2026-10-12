-- V4: Add user status lifecycle enum and otp failed attempts tracking [FRD v2.0]
ALTER TABLE users ADD COLUMN status ENUM('PENDING', 'ACTIVE', 'PAUSED', 'HIDDEN', 'ARCHIVED', 'SUSPENDED', 'DELETED') NOT NULL DEFAULT 'PENDING';
ALTER TABLE otp_tokens ADD COLUMN failed_attempts INT NOT NULL DEFAULT 0;
