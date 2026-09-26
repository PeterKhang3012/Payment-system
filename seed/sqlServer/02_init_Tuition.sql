sql
-- Tuition seed data
-- SQL Server

INSERT INTO tuition
    (student_id, student_name, amount, status)
VALUES
    (100001, 'Nguyen Van An',    5000000.00,  'UNPAID'),
    (100002, 'Tran Thi Binh',    7500000.00,  'UNPAID'),
    (100003, 'Le Van Cuong',     6500000.00,  'UNPAID'),
    (100004, 'Pham Thi Dung',    4500000.00,  'PAID'),
    (100005, 'Hoang Van Em',     8000000.00,  'UNPAID'),
    (100006, 'Vo Thi Hoa',       5500000.00,  'UNPAID'),
    (100007, 'Dang Van Khang',   9000000.00,  'PAID'),
    (100008, 'Bui Thi Lan',      6000000.00,  'UNPAID'),
    (100009, 'Do Van Minh',      7000000.00,  'UNPAID'),
    (100010, 'Nguyen Thi Nga',   10000000.00, 'UNPAID');

-- Check inserted data
SELECT *
FROM tuition;
