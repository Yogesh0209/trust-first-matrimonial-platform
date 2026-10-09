-- V2: starting data. These plans match the Super Admin plans screen.
INSERT INTO `plans` (`name`, `price_per_month`, `member_limit`, `gov_id_verification`, `status`) VALUES
('Free', 0, 100, FALSE, 'ACTIVE'),
('Pro', 499, 1000, TRUE, 'ACTIVE'),
('Premium', 999, NULL, TRUE, 'ACTIVE');
