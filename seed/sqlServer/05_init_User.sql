USE UserDB;
GO

INSERT INTO dbo.users
    (user_id, full_name, email, phone_number, created_at)
select seed.user_id, seed.full_name, seed.email, seed.phone_number, seed.created_at
from(
    VALUES
        ('68d500000000000000000001', N'Nguyễn Văn An',  '9conso@gmail.com',   '0900000001', SYSDATETIME()),
        ('68d500000000000000000002', N'Trần Thị Bình',  'ngocduckhangnguyen@gmail.com', '0900000002', SYSDATETIME()),
        ('68d500000000000000000003', N'Lê Minh Cường',  'italya1412@gmail.com','0900000003', SYSDATETIME()),
        ('68d500000000000000000004', N'Phạm Thu Dung',  'dung.sv000004@example.com', '0900000004', SYSDATETIME()),
        ('68d500000000000000000005', N'Hoàng Quốc Huy', 'huy.sv000005@example.com',  '0900000005', SYSDATETIME()),
        ('68d500000000000000000006', N'Võ Ngọc Lan',    'lan.sv000006@example.com',  '0900000006', SYSDATETIME()),
        ('68d500000000000000000007', N'Đặng Minh Khoa', 'khoa.sv000007@example.com', '0900000007', SYSDATETIME()),
        ('68d500000000000000000008', N'Bùi Thanh Mai',  'mai.sv000008@example.com',  '0900000008', SYSDATETIME()),
        ('68d500000000000000000009', N'Đỗ Hoàng Nam',   'nam.sv000009@example.com',  '0900000009', SYSDATETIME()),
        ('68d500000000000000000010', N'Phan Thảo Vy',   'vy.sv000010@example.com',   '0900000010', SYSDATETIME())
)As seed(user_id, full_name, email, phone_number, created_at)
WHERE NOT EXISTS (
    SELECT 1
    FROM users AS u
    WHERE u.user_id = seed.user_id
);

SELECT *
FROM dbo.users
ORDER BY id;