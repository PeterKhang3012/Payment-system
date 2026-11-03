%% KDNPay: all dashed edges below are LOGICAL, never physical FK.
%% Prefixes identify independent microservice/database boundaries.
%% PK on Redis paymentId means logical key, not SQL constraint.
%% Java_* types: exact SQL type/precision is not fixed in source.
%% NN = not null; NULL = nullable; required = Mongoose validation.
```mermaid
erDiagram
    direction LR
    AUTH_MONGO_auths {
        ObjectId _id PK
        ObjectId userId UK "required; JWT identity"
        String username "required; not unique"
        String password "required; bcrypt hash"
        Number __v "Mongoose default; may be absent in seed"
    }
    USER_UserDB_users {
        BIGINT id PK "NN IDENTITY"
        NVARCHAR(20) student_id UK "NN"
        NVARCHAR(50) user_id UK "NN logical Auth.userId"
        NVARCHAR(100) full_name "NN"
        NVARCHAR(100) email "NN"
        NVARCHAR(15) phone_number "NULL"
        DATETIME2 created_at "NN"
    }
    BAL_BalanceDB_balance {
        BIGINT id PK "NN IDENTITY"
        NVARCHAR(255) user_id "NN not unique"
        DECIMAL(19,2) balance "NN seed type"
    }
    BAL_BalanceDB_BalanceDeduction {
        BIGINT id PK "NN IDENTITY"
        Java_String payment_id UK "NN length 36 UUID"
        Java_String user_id "NN length 50"
        DECIMAL(19,2) amount "NN explicit mapping"
        DECIMAL(19,2) balance_after "NN explicit mapping"
    }
    TUITION_TuitionDB_tuition {
        BIGINT id PK "NN IDENTITY"
        BIGINT student_id "NN not unique; not User.student_id"
        NVARCHAR(255) student_name "NN"
        DECIMAL(19,2) amount "NN seed type"
        NVARCHAR(255) status "NN"
        NVARCHAR(255) processing_payment_id "NULL UUID claim"
    }
    PAY_PaymentDB_payment_transaction {
        BIGINT id PK "NN IDENTITY; not UUID paymentId"
        Java_String user_id "NN length 255 default"
        BIGINT tuition_id "NN logical Tuition.id"
        Java_BigDecimal amount "NN precision unspecified"
        Java_String status "NN length 255 default"
        Java_LocalDateTime created_at "NN"
    }
    PAY_Redis_PaymentAttempt {
        String paymentId PK "logical key suffix; UUID; TTL 5m"
        String userId "logical JWT identity"
        Long studentId "logical tuition selector; not unique"
        BigDecimal amount "snapshot; JSON no null constraint"
    }
    OTP_MySQL_otps {
        BIGINT id PK "NN AUTO_INCREMENT"
        VARCHAR(100) payment_id UK "NN logical UUID"
        VARCHAR(6) code "NN"
        VARCHAR(100) email "NN active DDL; entity length 150"
        DATETIME expires_at "NN indexed"
        TINYINT(1) used "NN default 0 indexed"
        INT attempts "NN default 0"
        DATETIME created_at "NN default CURRENT_TIMESTAMP"
    }
    AUTH_MONGO_auths ||..o| USER_UserDB_users : "logical userId = user_id"
    AUTH_MONGO_auths ||..o{ BAL_BalanceDB_balance : "logical userId; DB permits many"
    AUTH_MONGO_auths ||..o{ BAL_BalanceDB_BalanceDeduction : "logical payer userId"
    AUTH_MONGO_auths ||..o{ PAY_PaymentDB_payment_transaction : "logical payer userId"
    AUTH_MONGO_auths ||..o{ PAY_Redis_PaymentAttempt : "logical userId; active reservation"
    TUITION_TuitionDB_tuition ||..o{ PAY_PaymentDB_payment_transaction : "logical id = tuition_id"
    TUITION_TuitionDB_tuition }o..o{ PAY_Redis_PaymentAttempt : "logical student_id selector; no UK"
    PAY_Redis_PaymentAttempt ||..o| OTP_MySQL_otps : "logical paymentId; target may outlive Redis"
    PAY_Redis_PaymentAttempt ||..o| BAL_BalanceDB_BalanceDeduction : "logical UUID idempotency key"
    PAY_Redis_PaymentAttempt o|..o{ TUITION_TuitionDB_tuition : "logical UUID processing claim; may expire"
    OTP_MySQL_otps o|..o| BAL_BalanceDB_BalanceDeduction : "logical same UUID payment_id"
```