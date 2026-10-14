-- V6: candidate cross-bureau sharing consent (default OFF) [FRD SRC-05, SRC-08]
ALTER TABLE profiles
  ADD COLUMN share_consent BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN share_consent_at DATETIME NULL;