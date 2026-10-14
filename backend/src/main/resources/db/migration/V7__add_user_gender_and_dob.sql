-- V7: keep gender and date of birth from registration [FRD AUTH-01, PRO-04]
ALTER TABLE users
  ADD COLUMN gender ENUM ('MALE', 'FEMALE', 'OTHER') NULL,
  ADD COLUMN date_of_birth DATE NULL;