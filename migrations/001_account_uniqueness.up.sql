-- Run with sqlcmd -b, with application writers stopped. No data is rewritten.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
USE UserDB;
-- Locks are held until both database changes commit.
SELECT COUNT_BIG(*) AS users_before FROM dbo.users WITH (TABLOCKX, HOLDLOCK);
IF EXISTS (SELECT 1 FROM dbo.users GROUP BY LTRIM(RTRIM(email)) HAVING COUNT(*) > 1)
BEGIN
    SELECT id,user_id,email FROM dbo.users WHERE LTRIM(RTRIM(email)) IN
        (SELECT LTRIM(RTRIM(email)) FROM dbo.users GROUP BY LTRIM(RTRIM(email)) HAVING COUNT(*) > 1);
    THROW 51000, 'Duplicate email: migration stopped; no data changed.', 1;
END;
IF EXISTS (SELECT 1 FROM dbo.users WHERE DATALENGTH(email) <> DATALENGTH(LTRIM(RTRIM(email))))
    THROW 51000, 'Untrimmed email: manual review required.', 1;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.users') AND name=N'UQ_users_email')
    ALTER TABLE dbo.users ADD CONSTRAINT UQ_users_email UNIQUE (email);

USE TuitionDB;
SELECT COUNT_BIG(*) AS tuitions_before FROM dbo.tuition WITH (TABLOCKX, HOLDLOCK);
IF EXISTS (SELECT 1 FROM dbo.tuition GROUP BY student_id HAVING COUNT(*) > 1)
BEGIN
    SELECT id,student_id FROM dbo.tuition WHERE student_id IN
        (SELECT student_id FROM dbo.tuition GROUP BY student_id HAVING COUNT(*) > 1);
    THROW 51000, 'Duplicate studentId: migration stopped; no data changed.', 1;
END;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID(N'dbo.tuition') AND name=N'student_id' AND TYPE_NAME(user_type_id)=N'bigint')
BEGIN
    IF EXISTS (SELECT 1 FROM sys.foreign_key_columns WHERE
        (parent_object_id=OBJECT_ID(N'dbo.tuition') AND parent_column_id=COLUMNPROPERTY(OBJECT_ID(N'dbo.tuition'),N'student_id','ColumnId')) OR
        (referenced_object_id=OBJECT_ID(N'dbo.tuition') AND referenced_column_id=COLUMNPROPERTY(OBJECT_ID(N'dbo.tuition'),N'student_id','ColumnId')))
        THROW 51000, 'Foreign key depends on tuition.student_id: manual review required.', 1;
    IF EXISTS (SELECT 1 FROM sys.index_columns WHERE object_id=OBJECT_ID(N'dbo.tuition')
        AND column_id=COLUMNPROPERTY(OBJECT_ID(N'dbo.tuition'),N'student_id','ColumnId'))
        THROW 51000, 'Existing index depends on tuition.student_id: manual review required.', 1;
    IF EXISTS (SELECT 1 FROM sys.sql_expression_dependencies WHERE referenced_id=OBJECT_ID(N'dbo.tuition'))
        THROW 51000, 'SQL dependency on tuition: manual review required.', 1;
    IF EXISTS (SELECT 1 FROM dbo.tuition WHERE TRY_CONVERT(bigint,student_id) <= 0)
        THROW 51000, 'Nonpositive existing studentId: manual review required.', 1;
    ALTER TABLE dbo.tuition ALTER COLUMN student_id NVARCHAR(20) NOT NULL;
END;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID(N'dbo.tuition') AND name=N'student_id'
    AND TYPE_NAME(user_type_id)=N'nvarchar' AND max_length=40)
    THROW 51000, 'Unexpected student_id schema: migration stopped.', 1;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.tuition') AND name=N'UQ_tuition_student_id')
    ALTER TABLE dbo.tuition ADD CONSTRAINT UQ_tuition_student_id UNIQUE (student_id);
SELECT COUNT_BIG(*) AS tuitions_after FROM dbo.tuition;
USE UserDB;
SELECT COUNT_BIG(*) AS users_after FROM dbo.users;
COMMIT TRANSACTION;
