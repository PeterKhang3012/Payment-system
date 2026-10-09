USE UserDB;
GO

INSERT INTO dbo.users
    (student_id, user_id, full_name, email, phone_number, created_at)
VALUES
    ('SV000001', '68d500000000000000000001', N'Nguyễn Văn An',  'an.sv000001@example.com',   '0900000001', SYSDATETIME()),
    ('SV000002', '68d500000000000000000002', N'Trần Thị Bình',  'binh.sv000002@example.com', '0900000002', SYSDATETIME()),
    ('SV000003', '68d500000000000000000003', N'Lê Minh Cường',  'cuong.sv000003@example.com','0900000003', SYSDATETIME()),
    ('SV000004', '68d500000000000000000004', N'Phạm Thu Dung',  'dung.sv000004@example.com', '0900000004', SYSDATETIME()),
    ('SV000005', '68d500000000000000000005', N'Hoàng Quốc Huy', 'huy.sv000005@example.com',  '0900000005', SYSDATETIME()),
    ('SV000006', '68d500000000000000000006', N'Võ Ngọc Lan',    'lan.sv000006@example.com',  '0900000006', SYSDATETIME()),
    ('SV000007', '68d500000000000000000007', N'Đặng Minh Khoa', 'khoa.sv000007@example.com', '0900000007', SYSDATETIME()),
    ('SV000008', '68d500000000000000000008', N'Bùi Thanh Mai',  'mai.sv000008@example.com',  '0900000008', SYSDATETIME()),
    ('SV000009', '68d500000000000000000009', N'Đỗ Hoàng Nam',   'nam.sv000009@example.com',  '0900000009', SYSDATETIME()),
    ('SV000010', '68d500000000000000000010', N'Phan Thảo Vy',   'vy.sv000010@example.com',   '0900000010', SYSDATETIME());
GO

SELECT *
FROM dbo.users
ORDER BY id;

Update users
SET email = 'default@example.com'
WHERE email IS NULL OR email = 'email của student muốn đổi';