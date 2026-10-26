# Frontend Payment System

React + TypeScript + Vite + React Router + Axios. Giao diện tiếng Việt, responsive, màu #F37021. Không dùng dữ liệu giả.

## Cấu trúc thư mục dự án

Cây dưới đây mô tả các thành phần chính từ thư mục gốc `Payment System/`; lược bỏ thư mục Git, dependency và sản phẩm build.

```text
Payment System/
├── AGENTS.md                     # Hướng dẫn làm việc với dự án
├── .gitignore
├── .env                          # Cấu hình môi trường backend (không công khai)
├── .env.example                  # Mẫu cấu hình môi trường
├── docker-compose.yml            # Cấu hình các service và hạ tầng
├── Backend/
│   ├── auth-service/             # Đăng nhập, phát hành JWT (Node.js)
│   │   ├── app.js
│   │   ├── config/
│   │   ├── models/
│   │   └── src/
│   │       ├── controller/
│   │       ├── routes/
│   │       └── services/
│   ├── User/                     # Thông tin tài khoản (Spring Boot)
│   ├── balance-management/       # Số dư và khấu trừ tiền (Spring Boot)
│   ├── tuition-management/       # Tra cứu, trạng thái học phí (Spring Boot)
│   ├── payment-service/          # Điều phối thanh toán, lịch sử (Spring Boot)
│   └── otp-service/              # Sinh, gửi và xác thực OTP (Spring Boot)
├── frontend/                     # Ứng dụng React + TypeScript + Vite
│   ├── src/
│   │   ├── api.ts                # Axios, DTO và các lời gọi API
│   │   ├── main.tsx              # Khởi tạo React, router, trang và xác thực
│   │   └── styles.css            # Giao diện responsive, màu chủ đạo FPT
│   ├── index.html                # HTML đầu vào
│   ├── package.json              # Dependency và các lệnh chạy/kiểm tra
│   ├── package-lock.json         # Khóa phiên bản dependency
│   ├── tsconfig.json             # Cấu hình TypeScript
│   ├── vite.config.ts            # Cấu hình Vite và proxy API khi phát triển
│   ├── eslint.config.js          # Quy tắc lint
│   ├── .gitignore
│   └── README.md                 # Hướng dẫn frontend và tài liệu tích hợp
├── keys/                         # Khóa phục vụ xác thực; không đưa vào frontend
├── postman/                      # Collection, environment và tài liệu API
├── .postman/                     # Cấu hình workspace Postman
├── scripts/
│   └── generateKeys.js           # Script tạo khóa
└── seed/                         # Dữ liệu khởi tạo
    ├── mongo/
    ├── mysql/
    └── sqlServer/
```

Các service Spring Boot tổ chức mã nguồn trong `src/main/java/`, cấu hình trong `src/main/resources/` và kiểm thử trong `src/test/`. Các trang frontend hiện được định nghĩa trong `src/main.tsx`, chưa tách thành thư mục `pages/` hoặc `components/`.

## Chạy

```sh
npm install
npm run dev
```

Mở http://localhost:5173. Backend cần được vận hành riêng; frontend không khởi động hay thay đổi hạ tầng. JWT chỉ giữ trong bộ nhớ: tải lại trang cần đăng nhập lại.

Vite proxy `/gateway/auth`, `/gateway/user`, `/gateway/balance`, `/gateway/tuition`, `/gateway/payment` tới localhost:3000, 3006, 3001, 3002, 8080. Có thể đặt AUTH_TARGET, USER_TARGET, BALANCE_TARGET, TUITION_TARGET, PAYMENT_TARGET trong frontend/.env.local để đổi đích proxy (chỉ URL công khai, không thông tin bí mật).

Production cần reverse proxy các đường dẫn `/gateway/*` tương tự Vite và fallback SPA về index.html. Hoặc cấu hình VITE_AUTH_URL, VITE_USER_URL, VITE_BALANCE_URL, VITE_TUITION_URL, VITE_PAYMENT_URL bằng URL dịch vụ công khai đã cho phép CORS rồi build lại. `vite preview` không thay thế reverse proxy production. Không đặt secret vào VITE_*.

## Trang

/dang-nhap, / (dashboard), /tai-khoan, /so-du, /lich-su, /thanh-toan (tra cứu, điều khoản và OTP), /ket-qua. Các trang nội bộ yêu cầu đăng nhập. Lịch sử hỗ trợ lọc mã và trạng thái tại client.

## API được đối chiếu từ backend

| Service | API | Dữ liệu |
|---|---|---|
| Auth | POST /login | username, password → token, user |
| User | GET /api/users/me | fullName, email, phoneNumber, createdAt |
| Balance | GET /api/balances/me | balance |
| Tuition | GET /api/tuitions/{studentId} | tuitionId, studentId, studentName, status, amount |
| Payment | GET /api/payment-transactions/history | transactionId, tuitionId, amount, status, createdAt |
| Payment | POST /api/payments/request-otp | studentId, acceptedTerms → paymentId, email, expiresAt, expiresInSeconds, message |
| Payment | POST /api/payments/verify | paymentId, code → HTTP 2xx, không có body |

Chỉ gọi payment-service cho quy trình thanh toán; không gọi trực tiếp OTP nội bộ, deduct, claim, paid hay release. Người nộp tiền chỉ đọc từ /me; không tự suy ra studentId từ hồ sơ vì DTO không cung cấp trường này. Chỉ cho phép toàn bộ học phí UNPAID với số dư đủ và đã đồng ý điều khoản. Backend vẫn quyết định tính hợp lệ. Thông báo thành công chỉ sau verify trả 2xx. Khi timeout/lỗi, không tự động retry POST hoặc kết luận tiền chưa bị trừ.

## Giới hạn backend

- Chưa có API nạp tiền, cập nhật hồ sơ, đổi mật khẩu, refresh/logout token, tải biên lai.
- Chưa có API gửi lại OTP cho cùng paymentId, hủy attempt, hoặc truy vấn trạng thái bằng paymentId. OTP hết hạn cho phép tra cứu lại; không tự gọi endpoint nội bộ.
- Hồ sơ không trả mã sinh viên; tra cứu cần nhập mã sinh viên dạng số. Học phí không có học kỳ, hạn đóng hay chi tiết môn học nên không tự hiển thị các dữ liệu này.
- Nội dung điều khoản hiện là thông báo xác nhận phía frontend; backend chỉ nhận acceptedTerms, chưa có API điều khoản có phiên bản.
- Lịch sử không có phân trang server; frontend lọc dữ liệu được trả về. Trạng thái PAID là giá trị backend hiện ghi nhận.
- Chưa kiểm chứng giao dịch thực tế với tài khoản và các service đang chạy. Không sử dụng thông tin đăng nhập/secret từ file môi trường backend.

## Kiểm tra

```sh
npm run typecheck
npm run lint
npm run build
```
