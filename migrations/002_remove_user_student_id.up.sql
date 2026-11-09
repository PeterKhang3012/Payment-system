-- Removes the unused User.studentId only. Tuition.student_id is unchanged.
-- Run before deploying the updated User service against an existing database.
USE UserDB;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF COL_LENGTH(N'dbo.users', N'student_id') IS NOT NULL
BEGIN
    DECLARE @studentColumnId int = COLUMNPROPERTY(OBJECT_ID(N'dbo.users'), N'student_id', 'ColumnId');
    IF EXISTS (
        SELECT 1 FROM sys.foreign_key_columns
        WHERE (parent_object_id = OBJECT_ID(N'dbo.users') AND parent_column_id = @studentColumnId)
           OR (referenced_object_id = OBJECT_ID(N'dbo.users') AND referenced_column_id = @studentColumnId)
    )
    BEGIN
        ROLLBACK;
        THROW 51000, 'Foreign key depends on users.student_id: review required; no changes applied.', 1;
    END;

    IF EXISTS (
        SELECT 1 FROM sys.index_columns ic
        JOIN sys.indexes i ON i.object_id = ic.object_id AND i.index_id = ic.index_id
        WHERE ic.object_id = OBJECT_ID(N'dbo.users') AND ic.column_id = @studentColumnId
          AND i.name <> N'UQ_users_student_id'
    )
    BEGIN
        ROLLBACK;
        THROW 51000, 'Unexpected index depends on users.student_id: review required; no changes applied.', 1;
    END;

    IF EXISTS (SELECT 1 FROM sys.key_constraints
               WHERE parent_object_id = OBJECT_ID(N'dbo.users') AND name = N'UQ_users_student_id')
        ALTER TABLE dbo.users DROP CONSTRAINT UQ_users_student_id;

    ALTER TABLE dbo.users DROP COLUMN student_id;
END;
COMMIT;
GO
