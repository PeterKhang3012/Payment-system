-- ============================================================
-- user-service — Database Schema
-- DB: user_service_db  |  MySQL 8.0+
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    student_id      VARCHAR(20)     NOT NULL,
    username        VARCHAR(50)     NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    full_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(100)    NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uk_users_student_id UNIQUE (student_id),
    CONSTRAINT uk_users_username   UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tuition_fees (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    student_id  VARCHAR(20)     NOT NULL,
    semester    VARCHAR(50)     NOT NULL,
    amount      DECIMAL(15,2)   NOT NULL,
    status      VARCHAR(10)     NOT NULL DEFAULT 'UNPAID',
    description VARCHAR(500)    NULL,
    due_date    DATE            NULL,
    payment_id  VARCHAR(100)    NULL,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT chk_amount  CHECK (amount > 0),
    CONSTRAINT chk_status  CHECK (status IN ('UNPAID', 'PAID')),
    INDEX idx_tuition_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
