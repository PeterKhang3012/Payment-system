-- Tuition seed data
-- SQL Server

Use TuitionDB;

INSERT INTO tuition
    (student_id, student_name, amount, status)
select seed.student_id, seed.student_name, seed.amount, seed.status
from (
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
        (100010, 'Nguyen Thi Nga',   10000000.00, 'UNPAID')
)As seed(student_id, student_name, amount, status)
WHERE NOT EXISTS (
    SELECT 1
    FROM tuition AS t
    WHERE t.student_id = seed.student_id
);

-- Check inserted data
SELECT *
FROM tuition;
