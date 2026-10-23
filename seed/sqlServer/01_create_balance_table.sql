Use BalanceDB;
GO

IF OBJECT_ID(N'dbo.balance', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.balance (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id NVARCHAR(255) NOT NULL,
        balance DECIMAL(19, 2) NOT NULL
    );
END;