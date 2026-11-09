USE UserDB;
GO

IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id NVARCHAR(50) NOT NULL,
        full_name NVARCHAR(100) NOT NULL,
        email NVARCHAR(100) NOT NULL,
        phone_number NVARCHAR(15) NULL,
        created_at DATETIME2 NOT NULL
            CONSTRAINT DF_users_created_at DEFAULT SYSUTCDATETIME(),

        CONSTRAINT UQ_users_email UNIQUE (email),
        CONSTRAINT UQ_users_user_id UNIQUE (user_id)
    );
END;
GO