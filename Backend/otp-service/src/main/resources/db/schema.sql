-- ============================================================
-- otp-service — Database Schema
-- DB: otp_service_db  |  MySQL 8.0+
-- ============================================================
CREATE TABLE IF NOT EXISTS otps (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    payment_id  VARCHAR(100)    NOT NULL,
    code        VARCHAR(6)      NOT NULL,
    email       VARCHAR(100)    NOT NULL,
    expires_at  DATETIME        NOT NULL,
    used        TINYINT(1)      NOT NULL DEFAULT 0,
    attempts    INT             NOT NULL DEFAULT 0,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uk_otps_payment_id UNIQUE (payment_id),
    INDEX idx_otps_expires_at (expires_at),
    INDEX idx_otps_used (used)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
