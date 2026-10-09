-- V1: create the full schema (14 tables)
-- Flyway runs this file once, in order. Never edit it after it has been applied.
-- To change the schema later, add a new file such as V3__add_something.sql.

CREATE TABLE `plans` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(50) UNIQUE NOT NULL,
  `price_per_month` decimal(10,2) NOT NULL DEFAULT 0,
  `member_limit` int COMMENT 'NULL means unlimited',
  `gov_id_verification` boolean NOT NULL DEFAULT false,
  `status` ENUM ('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE `bureaus` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(120) NOT NULL,
  `city` varchar(80) NOT NULL,
  `status` ENUM ('PENDING', 'ACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'PENDING',
  `share_members` boolean NOT NULL DEFAULT false,
  `search_other_bureaus` boolean NOT NULL DEFAULT false,
  `show_bureau_name` boolean NOT NULL DEFAULT false,
  `created_at` datetime NOT NULL DEFAULT (now())
);

CREATE TABLE `subscriptions` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `bureau_id` bigint NOT NULL,
  `plan_id` bigint NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `status` ENUM ('ACTIVE', 'EXPIRED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE `payments` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `subscription_id` bigint NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `gateway_order_id` varchar(100) NOT NULL,
  `gateway_payment_id` varchar(100),
  `status` ENUM ('CREATED', 'PAID', 'FAILED') NOT NULL DEFAULT 'CREATED',
  `created_at` datetime NOT NULL DEFAULT (now()),
  `paid_at` datetime
);

CREATE TABLE `users` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `bureau_id` bigint COMMENT 'NULL for Super Admin',
  `role` ENUM ('SUPER_ADMIN', 'HOST', 'END_USER') NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(150) UNIQUE NOT NULL,
  `phone` varchar(15) UNIQUE NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `email_verified` boolean NOT NULL DEFAULT false,
  `phone_verified` boolean NOT NULL DEFAULT false,
  `is_blocked` boolean NOT NULL DEFAULT false,
  `last_active_at` datetime,
  `created_at` datetime NOT NULL DEFAULT (now())
);

CREATE TABLE `profiles` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint UNIQUE NOT NULL,
  `gender` ENUM ('MALE', 'FEMALE', 'OTHER') NOT NULL,
  `date_of_birth` date NOT NULL,
  `marital_status` ENUM ('NEVER_MARRIED', 'DIVORCED', 'WIDOWED') NOT NULL,
  `religion` varchar(50),
  `mother_tongue` varchar(50),
  `city` varchar(80),
  `height_cm` smallint,
  `education` varchar(100),
  `occupation` varchar(100),
  `family_type` varchar(30),
  `about` text,
  `trust_level` tinyint NOT NULL DEFAULT 1 COMMENT '1 to 3. Kept on purpose for fast search',
  `visibility` ENUM ('VISIBLE', 'HIDDEN') NOT NULL DEFAULT 'VISIBLE',
  `updated_at` datetime NOT NULL DEFAULT (now())
);

CREATE TABLE `profile_photos` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `profile_id` bigint NOT NULL,
  `photo_url` varchar(500) NOT NULL,
  `is_primary` boolean NOT NULL DEFAULT false,
  `uploaded_at` datetime NOT NULL DEFAULT (now())
);

CREATE TABLE `verifications` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `profile_id` bigint NOT NULL,
  `type` ENUM ('SELFIE', 'GOV_ID') NOT NULL COMMENT 'SELFIE = Level 2, GOV_ID = Level 3',
  `file_url` varchar(500) NOT NULL,
  `file_url_back` varchar(500),
  `status` ENUM ('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
  `reviewed_by` bigint,
  `reject_reason` varchar(255),
  `submitted_at` datetime NOT NULL DEFAULT (now()),
  `reviewed_at` datetime
);

CREATE TABLE `interests` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `sender_profile_id` bigint NOT NULL,
  `receiver_profile_id` bigint NOT NULL,
  `status` ENUM ('PENDING', 'ACCEPTED', 'DECLINED') NOT NULL DEFAULT 'PENDING',
  `created_at` datetime NOT NULL DEFAULT (now()),
  `responded_at` datetime
);

CREATE TABLE `shortlists` (
  `profile_id` bigint NOT NULL,
  `shortlisted_profile_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT (now()),
  PRIMARY KEY (`profile_id`, `shortlisted_profile_id`)
);

CREATE TABLE `messages` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `interest_id` bigint NOT NULL COMMENT 'chat opens only after interest is ACCEPTED',
  `sender_profile_id` bigint NOT NULL,
  `body` text NOT NULL,
  `is_flagged` boolean NOT NULL DEFAULT false,
  `sent_at` datetime NOT NULL DEFAULT (now())
);

CREATE TABLE `reports` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `reporter_profile_id` bigint NOT NULL,
  `reported_profile_id` bigint NOT NULL,
  `reason` ENUM ('FAKE_PHOTOS', 'ASKED_FOR_MONEY', 'INACTIVE_PROFILE', 'WRONG_DETAILS', 'OTHER') NOT NULL,
  `details` varchar(500),
  `status` ENUM ('OPEN', 'WARNED', 'REMOVED', 'DISMISSED') NOT NULL DEFAULT 'OPEN',
  `handled_by` bigint,
  `created_at` datetime NOT NULL DEFAULT (now()),
  `resolved_at` datetime
);

CREATE TABLE `audit_logs` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `actor_user_id` bigint,
  `action` varchar(60) NOT NULL,
  `entity_type` varchar(40) NOT NULL,
  `entity_id` bigint,
  `details` varchar(500),
  `created_at` datetime NOT NULL DEFAULT (now())
);

CREATE TABLE `otp_tokens` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `purpose` ENUM ('EMAIL_VERIFY', 'PHONE_VERIFY', 'PASSWORD_RESET') NOT NULL,
  `code_hash` varchar(100) NOT NULL,
  `expires_at` datetime NOT NULL,
  `used` boolean NOT NULL DEFAULT false,
  `created_at` datetime NOT NULL DEFAULT (now())
);

CREATE INDEX `profiles_index_0` ON `profiles` (`visibility`, `trust_level`);

CREATE INDEX `profiles_index_1` ON `profiles` (`city`);

CREATE UNIQUE INDEX `interests_index_2` ON `interests` (`sender_profile_id`, `receiver_profile_id`);

ALTER TABLE `users` ADD FOREIGN KEY (`bureau_id`) REFERENCES `bureaus` (`id`);

ALTER TABLE `subscriptions` ADD FOREIGN KEY (`bureau_id`) REFERENCES `bureaus` (`id`);

ALTER TABLE `subscriptions` ADD FOREIGN KEY (`plan_id`) REFERENCES `plans` (`id`);

ALTER TABLE `payments` ADD FOREIGN KEY (`subscription_id`) REFERENCES `subscriptions` (`id`);

ALTER TABLE `profiles` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `profile_photos` ADD FOREIGN KEY (`profile_id`) REFERENCES `profiles` (`id`);

ALTER TABLE `verifications` ADD FOREIGN KEY (`profile_id`) REFERENCES `profiles` (`id`);

ALTER TABLE `verifications` ADD FOREIGN KEY (`reviewed_by`) REFERENCES `users` (`id`);

ALTER TABLE `interests` ADD FOREIGN KEY (`sender_profile_id`) REFERENCES `profiles` (`id`);

ALTER TABLE `interests` ADD FOREIGN KEY (`receiver_profile_id`) REFERENCES `profiles` (`id`);

ALTER TABLE `shortlists` ADD FOREIGN KEY (`profile_id`) REFERENCES `profiles` (`id`);

ALTER TABLE `shortlists` ADD FOREIGN KEY (`shortlisted_profile_id`) REFERENCES `profiles` (`id`);

ALTER TABLE `messages` ADD FOREIGN KEY (`interest_id`) REFERENCES `interests` (`id`);

ALTER TABLE `messages` ADD FOREIGN KEY (`sender_profile_id`) REFERENCES `profiles` (`id`);

ALTER TABLE `reports` ADD FOREIGN KEY (`reporter_profile_id`) REFERENCES `profiles` (`id`);

ALTER TABLE `reports` ADD FOREIGN KEY (`reported_profile_id`) REFERENCES `profiles` (`id`);

ALTER TABLE `reports` ADD FOREIGN KEY (`handled_by`) REFERENCES `users` (`id`);

ALTER TABLE `audit_logs` ADD FOREIGN KEY (`actor_user_id`) REFERENCES `users` (`id`);

ALTER TABLE `otp_tokens` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

-- Extra rules the diagram cannot express (need MySQL 8.0.16 or newer)
ALTER TABLE `profiles` ADD CONSTRAINT `chk_profiles_trust_level` CHECK (`trust_level` BETWEEN 1 AND 3);
ALTER TABLE `interests` ADD CONSTRAINT `chk_interests_not_self` CHECK (`sender_profile_id` <> `receiver_profile_id`);
