-- Explicit rollback only, with application writers stopped and the old application ready.
-- Refuse any conversion that could lose leading zeroes, overflow, or merge distinct strings.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
USE TuitionDB;
SELECT COUNT_BIG(*) AS tuition_count FROM dbo.tuition WITH (TABLOCKX,HOLDLOCK);
IF EXISTS (SELECT 1 FROM dbo.tuition WHERE TRY_CONVERT(bigint,student_id) IS NULL
    OR CONVERT(nvarchar(20),TRY_CONVERT(bigint,student_id)) COLLATE Latin1_General_100_BIN2 <> student_id COLLATE Latin1_General_100_BIN2
    OR DATALENGTH(CONVERT(nvarchar(20),TRY_CONVERT(bigint,student_id))) <> DATALENGTH(student_id))
    THROW 51000, 'Rollback would change studentId or lose information: manual review required.', 1;
IF EXISTS (SELECT 1 FROM dbo.tuition GROUP BY TRY_CONVERT(bigint,student_id) HAVING COUNT(*) > 1)
    THROW 51000, 'Rollback would merge studentIds: stopped.', 1;
IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE parent_object_id=OBJECT_ID(N'dbo.tuition') AND name=N'UQ_tuition_student_id')
    ALTER TABLE dbo.tuition DROP CONSTRAINT UQ_tuition_student_id;
ALTER TABLE dbo.tuition ALTER COLUMN student_id BIGINT NOT NULL;
USE UserDB;
IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE parent_object_id=OBJECT_ID(N'dbo.users') AND name=N'UQ_users_email')
    ALTER TABLE dbo.users DROP CONSTRAINT UQ_users_email;
COMMIT TRANSACTION;
