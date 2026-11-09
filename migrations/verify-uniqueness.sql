-- All inserts in existing tables are rolled back. Never deletes existing rows.
SET NOCOUNT ON;
USE UserDB;
BEGIN TRANSACTION;
BEGIN TRY
    INSERT dbo.users(user_id,full_name,email,created_at)
    SELECT N'UNIQUE_TEST',N'Unique test',email,SYSUTCDATETIME()
    FROM dbo.users WHERE id=1;
    THROW 51000, 'FAILED: duplicate email was accepted', 1;
END TRY
BEGIN CATCH
    IF ERROR_NUMBER() NOT IN (2601,2627) THROW;
    PRINT 'PASS: duplicate email rejected';
END CATCH;
ROLLBACK;
BEGIN TRANSACTION;
BEGIN TRY
    INSERT dbo.users(user_id,full_name,email,created_at)
    SELECT N'UNIQUE_TEST',N'Unique test',UPPER(email),SYSUTCDATETIME()
    FROM dbo.users WHERE id=1;
    THROW 51000, 'FAILED: email case-insensitive comparison changed', 1;
END TRY
BEGIN CATCH
    IF ERROR_NUMBER() NOT IN (2601,2627) THROW;
    PRINT 'PASS: email case-insensitive comparison preserved';
END CATCH;
ROLLBACK;
USE TuitionDB;
BEGIN TRANSACTION;
BEGIN TRY
    INSERT dbo.tuition(student_id,student_name,amount,status)
    SELECT student_id,N'Unique test',1,'UNPAID' FROM dbo.tuition WHERE id=1;
    THROW 51000, 'FAILED: duplicate tuition.studentId was accepted', 1;
END TRY
BEGIN CATCH
    IF ERROR_NUMBER() NOT IN (2601,2627) THROW;
    PRINT 'PASS: duplicate tuition.studentId rejected';
END CATCH;
ROLLBACK;
BEGIN TRANSACTION;
INSERT dbo.tuition(student_id,student_name,amount,status) VALUES(N'00000000000000000001',N'Unique test',1,'UNPAID');
IF NOT EXISTS (SELECT 1 FROM dbo.tuition WHERE student_id=N'00000000000000000001' AND DATALENGTH(student_id)=40)
    THROW 51000, 'FAILED: leading zeroes not preserved', 1;
PRINT 'PASS: 20-character string ID and leading zeroes preserved';
ROLLBACK;
