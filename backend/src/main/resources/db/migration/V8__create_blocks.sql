-- V8: candidate blocks [FRD SAFE]
CREATE TABLE blocks (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  blocker_profile_id BIGINT NOT NULL,
  blocked_profile_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_blocks UNIQUE (blocker_profile_id, blocked_profile_id),
  CONSTRAINT fk_blocks_blocker FOREIGN KEY (blocker_profile_id) REFERENCES profiles (id),
  CONSTRAINT fk_blocks_blocked FOREIGN KEY (blocked_profile_id) REFERENCES profiles (id)
);