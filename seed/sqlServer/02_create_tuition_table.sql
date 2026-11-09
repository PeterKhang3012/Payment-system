Use TuitionDB;

IF OBJECT_ID(N'dbo.tuition', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.tuition (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        student_id NVARCHAR(20) NOT NULL,
        CONSTRAINT UQ_tuition_student_id UNIQUE (student_id),
        student_name NVARCHAR(255) NOT NULL,
        amount DECIMAL(19,2) NOT NULL,
        status NVARCHAR(255) NOT NULL,
        processing_payment_id NVARCHAR(255) NULL
    );
END;
GO