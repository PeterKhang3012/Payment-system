# ERD KDNPay — cấu trúc được triển khai trong source

Ngày phân tích: 03/11/2026. Phạm vi: `Backend/`, `seed/`, `docker-compose.yml`, entity/model, repository, DTO, controller, client, service và cấu hình database. Đây là đối chiếu tĩnh; chưa truy vấn catalog của database đang chạy. Không sửa source hoặc schema.

## 1. Phân vùng dữ liệu

| Service | Database/kho dữ liệu | Entity lưu trữ | Số lượng |
|---|---|---|---:|
| Authentication | MongoDB; seed chọn `payment_system`, runtime theo `MONGO_URL` | Auth (`auths`) | 1 |
| User Management (`Backend/User`) | SQL Server `UserDB` | User (`users`) | 1 |
| Balance Management | SQL Server `BalanceDB` | Balance (`balance`), BalanceDeduction | 2 |
| Tuition Management | SQL Server `TuitionDB` | Tuition (`tuition`) | 1 |
| Payment Processing | SQL Server `PaymentDB` | PaymentTransaction (`payment_transaction`) | 1 |
| Payment Processing | Redis, database index không override trong source | PaymentAttempt (JSON có TTL) | 1 bổ sung |
| OTP Verification | MySQL `otp_service_db` | Otp (`otps`) | 1 |
| Email Notification (service bổ sung trong Compose) | Không có persistence trong source | Không có entity; nhận PaymentSuccessfulEvent rồi gửi mail | 0 |

Tổng: **7 entity MongoDB/SQL + 1 mô hình lưu tạm Redis**. Hai loại Redis lock được mô tả riêng, không đếm thành entity nghiệp vụ. Không có entity Student, Invoice, Role, Semester hoặc bảng nối N-N trong implementation đã kiểm tra.

SQL Server dùng chung container nhưng có bốn database độc lập. MongoDB, MySQL và Redis là các kho riêng. 

## 2. Quy ước đọc

- PK: khóa chính SQL hoặc `_id` MongoDB. UK: unique constraint/index được khai báo, chưa xác nhận index thực tế trên instance.
- NN = NOT NULL; NULL = được phép null. Với MongoDB, `required` là validation Mongoose, không phải SQL NOT NULL hoặc MongoDB collection validator.
- Mermaid dùng tên có tiền tố service/database để phân nhóm; mọi đường nét đứt có nhãn `logical`, không dùng ký hiệu FK cho logical reference. Nét đứt ở đây được quy ước là logical, không diễn giải thành FK theo identifying relationship của Mermaid.
- `Java_String`, `Java_BigDecimal`, `Java_LocalDateTime` trên sơ đồ nghĩa là source chưa chốt đầy đủ kiểu SQL. Không tự gán precision/scale cho cột chỉ khai báo BigDecimal.
- Cardinality logical mô tả luồng ứng dụng và tính duy nhất đã khai báo; không có ràng buộc bảo đảm bản ghi bên kia luôn tồn tại. Redis hết TTL hoặc bị xóa có thể làm mất đầu mối.

## 3. Entity và attributes

### 3.1 Auth — Authentication / MongoDB `auths`

Nguồn: [auth.model.js](../../Backend/auth-service/models/auth.model.js), [auth.service.js](../../Backend/auth-service/src/services/auth.service.js), [seed MongoDB](../../seed/mongo/01_init_auth.js), [db.js](../../Backend/auth-service/config/db.js).

| Attribute | Type | Khóa | Null/required | Vai trò |
|---|---|---|---|---|
| `_id` | BSON ObjectId | PK | Tự sinh, định danh document | Khóa MongoDB; không phải userId nghiệp vụ |
| `userId` | BSON ObjectId | UK | required | Định danh được đưa vào JWT |
| `username` | String | — | required | Tra cứu đăng nhập; **không unique** trong schema |
| `password` | String | — | required | Bcrypt hash, được so sánh khi login |
| `__v` | Number (version key mặc định Mongoose) | — | Không khai báo required | Có thể xuất hiện khi lưu qua Mongoose; seed insert trực tiếp không khai báo trường này |

Không cấu hình timestamps, `ref` hoặc collection validator. `userId` unique là index do Mongoose khai báo; script seed không tự tạo index này. Không đưa giá trị mật khẩu/hash vào tài liệu.

### 3.2 User — User Management / `UserDB.dbo.users`

Nguồn: [User.java](../../Backend/User/src/main/java/com/MyProject/User/entity/User.java), [DDL](../../seed/sqlServer/03_create_user_table.sql), [UserRepository](../../Backend/User/src/main/java/com/MyProject/User/repository/UserRepository.java), [UserService](../../Backend/User/src/main/java/com/MyProject/User/service/UserService.java).

| Column / Java attribute | SQL type / Java type | Khóa | Null | Vai trò |
|---|---|---|---|---|
| `id` | BIGINT IDENTITY / Long | PK | NN | Khóa nội bộ SQL |
| `student_id` / studentId | NVARCHAR(20) / String | UK | NN | Mã sinh viên trong hồ sơ |
| `user_id` / userId | NVARCHAR(50) / String | UK | NN | Chuỗi định danh từ JWT/Auth.userId |
| `full_name` / fullName | NVARCHAR(100) / String | — | NN | Tên người nộp tiền |
| `email` | NVARCHAR(100) / String | — | NN | Email nhận OTP/thông báo; không unique |
| `phone_number` / phoneNumber | NVARCHAR(15) / String | — | NULL | Số điện thoại |
| `created_at` / createdAt | DATETIME2 / LocalDateTime | — | NN | `@CreationTimestamp`, không update; DDL default SYSUTCDATETIME() |

DDL đặt tên UK là `UQ_users_student_id`, `UQ_users_user_id`. Không có FK. API `/me` tìm bằng userId; UserResponse chỉ là projection, không tạo bảng mới.

### 3.3 Balance — Balance Management / `BalanceDB.dbo.balance`

Nguồn: [Balance.java](../../Backend/balance-management/src/main/java/com/myproject/balance/entity/Balance.java), [DDL](../../seed/sqlServer/01_create_balance_table.sql), [repository](../../Backend/balance-management/src/main/java/com/myproject/balance/repository/BalanceRepository.java).

| Column / Java attribute | SQL type / Java type | Khóa | Null | Vai trò |
|---|---|---|---|---|
| `id` | BIGINT IDENTITY / Long | PK | NN | Khóa dòng số dư |
| `user_id` / userId | NVARCHAR(255) / String | — | NN | Tài khoản người trả tiền |
| `balance` | DECIMAL(19,2) theo seed / BigDecimal | — | NN | Số dư hiện có; Java khởi tạo ZERO, không phải SQL DEFAULT |

Không unique trên user_id. Repository trả Optional và khóa PESSIMISTIC_WRITE theo userId, cho thấy ứng dụng kỳ vọng tối đa một dòng/tài khoản; database khai báo vẫn cho phép nhiều dòng. Entity không ghi precision/scale cho balance; seed là căn cứ DECIMAL(19,2).

### 3.4 BalanceDeduction — Balance Management / `BalanceDB`

Nguồn: [BalanceDeduction.java](../../Backend/balance-management/src/main/java/com/myproject/balance/entity/BalanceDeduction.java), [repository](../../Backend/balance-management/src/main/java/com/myproject/balance/repository/BalanceDeductionRepository.java), [BalanceService](../../Backend/balance-management/src/main/java/com/myproject/balance/service/BalanceService.java).

| Column / Java attribute | Type theo mapping | Khóa | Null | Vai trò |
|---|---|---|---|---|
| `id` | Long → BIGINT IDENTITY | PK | NN | Khóa nhật ký khấu trừ |
| `payment_id` / paymentId | String, length 36 | UK | NN | UUID/idempotency key do PaymentService truyền |
| `user_id` / userId | String, length 50 | — | NN | Người bị trừ tiền |
| `amount` | BigDecimal, precision 19, scale 2 | — | NN | Số tiền đã trừ |
| `balance_after` / balanceAfterDeduction | BigDecimal, precision 19, scale 2 | — | NN | Snapshot số dư sau khấu trừ |

`@Table(name="Balance_Deduction")`; tên vật lý có thể được chuẩn hóa thành `balance_deduction` bởi naming strategy. Không có DDL tạo bảng này trong seed; Hibernate `ddl-auto=update` tạo/cập nhật từ mapping. String dự kiến VARCHAR theo mapping thông thường, nhưng chưa xác nhận catalog nên không khẳng định NVARCHAR. UK tên `uk_balance_deduction_payment_id`. Không có balance_id hoặc FK đến Balance. Service kiểm tra paymentId đã dùng, userId và amount để trả lại kết quả cũ mà không trừ lần hai.

### 3.5 Tuition — Tuition Management / `TuitionDB.dbo.tuition`

Nguồn: [Tuition.java](../../Backend/tuition-management/src/main/java/com/myproject/tuition/entity/Tuition.java), [DDL](../../seed/sqlServer/02_create_tuition_table.sql), [repository](../../Backend/tuition-management/src/main/java/com/myproject/tuition/repository/TuitionRepository.java).

| Column / Java attribute | SQL type / Java type | Khóa | Null | Vai trò |
|---|---|---|---|---|
| `id` | BIGINT IDENTITY / Long | PK | NN | ID khoản học phí; được lưu vào PaymentTransaction.tuition_id |
| `student_id` / studentId | BIGINT / Long | — | NN | Mã để tra cứu/claim học phí |
| `student_name` / studentName | NVARCHAR(255) / String | — | NN | Tên sinh viên trên khoản học phí |
| `amount` | DECIMAL(19,2) theo seed / BigDecimal | — | NN | Khoản tiền thanh toán toàn phần |
| `status` | NVARCHAR(255) / String | — | NN | UNPAID → PROCESSING → PAID; release trả về UNPAID |
| `processing_payment_id` / processingPaymentId | NVARCHAR(255) / String | — | NULL | UUID đang giữ claim; xóa khi complete/release |

Không unique student_id hoặc processing_payment_id. Repository tra cứu một kết quả theo studentId nhưng câu UPDATE claim/complete/release có thể tác động nhiều dòng nếu student_id trùng. Status là String, không enum hoặc CHECK constraint. Không có bảng Student trong source.

### 3.6 PaymentTransaction — Payment Processing / `PaymentDB.payment_transaction`

Nguồn: [entity](../../Backend/payment-service/src/main/java/com/myproject/payment/entity/PaymentTransaction.java), [repository](../../Backend/payment-service/src/main/java/com/myproject/payment/repository/PaymentTransactionRepository.java), [service](../../Backend/payment-service/src/main/java/com/myproject/payment/service/PaymentTransactionService.java).

| Column / Java attribute | Type theo mapping | Khóa | Null | Vai trò |
|---|---|---|---|---|
| `id` | Long → BIGINT IDENTITY | PK | NN | ID lịch sử tự tăng; **khác UUID paymentId** |
| `user_id` / userId | String, JPA length mặc định 255 | — | NN | Người nộp tiền từ JWT |
| `tuition_id` / tuitionId | Long → BIGINT | Logical ref, không FK | NN | Tuition.id trả về qua API |
| `amount` | BigDecimal; không khai báo precision/scale | — | NN | Snapshot số tiền thanh toán |
| `status` | String, JPA length mặc định 255 | — | NN | Service tạo lịch sử với PAID |
| `created_at` / createdAt | LocalDateTime | — | NN | Service gán LocalDateTime.now() |

Không có migration/DDL tạo bảng trong seed, chỉ tạo PaymentDB; bảng được Hibernate update từ entity. SQL String thường VARCHAR, timestamp phụ thuộc dialect/version; source không đủ chốt kiểu catalog/precision. Không có UK trên tuition_id, không có UUID paymentId, OTP ID hoặc deduction ID. Vì thế database không bảo đảm mỗi học phí chỉ có một lịch sử, và không có khóa bền vững để nối chính xác lịch sử này với OTP/khấu trừ.

### 3.7 Otp — OTP Verification / `otp_service_db.otps`

Nguồn: [Otp.java](../../Backend/otp-service/src/main/java/com/paymentsystem/otpservice/entity/Otp.java), [schema đang được cấu hình chạy](../../Backend/otp-service/src/main/resources/db/schema.sql), [application.yml](../../Backend/otp-service/src/main/resources/application.yml), [OtpRepository](../../Backend/otp-service/src/main/java/com/paymentsystem/otpservice/repository/OtpRepository.java).

| Column / Java attribute | SQL type / Java type | Khóa | Null | Vai trò/default |
|---|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT / Long | PK | NN | ID OTP |
| `payment_id` / paymentId | VARCHAR(100) / String | UK | NN | UUID của PaymentAttempt |
| `code` | VARCHAR(6) / String | — | NN | Mã OTP 6 chữ số |
| `email` | VARCHAR(100) theo schema / String length 150 theo entity | — | NN | Địa chỉ nhận OTP; có sai khác mapping/schema |
| `expires_at` / expiresAt | DATETIME / LocalDateTime | — | NN | Thời điểm hết hạn |
| `used` | TINYINT(1) / boolean | — | NN | DEFAULT 0; Java default false |
| `attempts` | INT / int | — | NN | DEFAULT 0; số lần nhập sai |
| `created_at` / createdAt | DATETIME / LocalDateTime | — | NN | DEFAULT CURRENT_TIMESTAMP, @CreationTimestamp |

UK `uk_otps_payment_id`; index thường `idx_otps_expires_at`, `idx_otps_used`. InnoDB, utf8mb4_unicode_ci. `ddl-auto=none`, Spring SQL init mode always chạy schema classpath. File `seed/mysql/init.sql` khai báo email VARCHAR(150) nhưng **không được mount vào otp-mysql trong Compose**; không coi đó là schema có hiệu lực mặc định. CREATE TABLE IF NOT EXISTS không sửa một bảng cũ.

Generate lại cùng paymentId cập nhật dòng cũ, reset used/attempts; không tạo lịch sử nhiều OTP cùng paymentId. TTL nghiệp vụ mặc định 5 phút, verify có kiểm tra used/expiry/max attempts. Scheduler chạy mỗi giờ, xóa OTP có expiresAt trước now−24 giờ; không xóa ngay khi attempt hết TTL.

### 3.8 PaymentAttempt — Payment Processing / Redis

Nguồn: [DTO](../../Backend/payment-service/src/main/java/com/myproject/payment/dto/PaymentAttempt.java), [PaymentAttemptService](../../Backend/payment-service/src/main/java/com/myproject/payment/service/PaymentAttemptService.java), [RedisConfig](../../Backend/payment-service/src/main/java/com/myproject/payment/config/RedisConfig.java).

Redis STRING key `payment-attempt:{paymentId}`, value JSON qua JacksonJsonRedisSerializer, TTL 5 phút. Đây là đối tượng thực sự lưu trữ dù class nằm trong dto, không phải JPA entity.

| JSON attribute | Java type | Khóa/reference | Null |
|---|---|---|---|
| `paymentId` | String (UUID sinh trong PaymentService) | Định danh logic của attempt; suffix Redis key | Không có constraint JSON; luồng hiện tại luôn gán |
| `userId` | String | Logical Auth.userId qua JWT | Không có constraint JSON; luồng gán từ JWT |
| `studentId` | Long | Selector Tuition.student_id | Không có constraint JSON; API kiểm tra >0 và tra cứu trước khi lưu |
| `amount` | BigDecimal | Snapshot amount từ TuitionResponse | Không có constraint JSON; luồng gán từ API |

PK trên sơ đồ của paymentId là **khóa logic**, không phải SQL PK/unique constraint trên JSON. Redis key duy nhất tại một thời điểm; bản thân payload không có schema validation. Không có tuitionId/status/createdAt trong payload, không tự thêm.

Hai key điều phối cùng TTL 5 phút, value String paymentId: `payment-active:{userId}` đặt SETNX khi saveAttempt để giới hạn một attempt hoạt động/người; `payment-processing:{userId}` đặt SETNX khi verify để khóa xử lý. Chúng là reservation/lock, không phải entity/bảng quan hệ. Xóa khóa bằng Lua chỉ khi value khớp paymentId. Attempt bị xóa sau luồng thanh toán thành công hoặc tự hết TTL; không phải lịch sử bền vững.

## 4. Relationships đã xác định

Mọi quan hệ bên dưới là **logical**, không được SQL FK hoặc MongoDB ref bảo vệ. `1` ở đầu cha nghĩa là đích kỳ vọng khi luồng thành công; bản ghi thiếu/orphan vẫn có thể xảy ra.

| Quan hệ | Cardinality và reference | Căn cứ/ý nghĩa |
|---|---|---|
| Auth → User | 1 → 0..1; stringify(Auth.userId) = users.user_id | JWT phát userId; UserService tìm userId; users.user_id unique. Không nối Auth._id với users.id |
| Auth → Balance | 1 → 0..N theo constraint; nghiệp vụ kỳ vọng 0..1; userId → user_id | BalanceController dùng JWT userId; seed dùng cùng định danh. Không có unique balance.user_id |
| Auth → BalanceDeduction | 1 → 0..N; userId → user_id | BalanceService ghi người bị khấu trừ từ JWT |
| Auth → PaymentTransaction | 1 → 0..N; userId → user_id | PaymentController lấy userId từ JWT, service lưu lịch sử |
| Auth → PaymentAttempt | 1 → 0..N theo key; thông thường tối đa 1 active; userId → userId | saveAttempt dùng JWT; payment-active SETNX giới hạn active, không phải constraint bền vững |
| Tuition → PaymentTransaction | 1 → 0..N; tuition.id → payment_transaction.tuition_id | PaymentService lấy TuitionResponse.tuitionId khi tạo lịch sử. Nhiều lịch sử được schema cho phép dù luồng mong thanh toán thành công một lần |
| Tuition ↔ PaymentAttempt (selector) | N ↔ N theo constraint; attempt.studentId = tuition.student_id | API tra học phí bằng studentId; không unique student_id. Bình thường mỗi attempt tra một khoản; một khoản có thể có nhiều attempt theo thời gian. Sơ đồ dùng N-N để không giả tạo uniqueness; không có bảng nối |
| PaymentAttempt → Otp | 1 → 0..1 khi attempt còn sống; paymentId → payment_id | requestOtp lưu attempt rồi generate; OTP unique payment_id, regenerate ghi đè dòng cũ |
| PaymentAttempt → BalanceDeduction | 1 → 0..1 khi còn sống; paymentId → payment_id | BalanceClient gửi UUID qua Idempotency-Key; deduction unique payment_id |
| PaymentAttempt → Tuition (claim) | 1 → 0..N theo constraint; paymentId → processing_payment_id | Claim theo studentId gán UUID; từng tuition có 0..1 UUID claim hiện tại; clear khi complete/release. Không unique processing_payment_id |
| Otp ↔ BalanceDeduction | 0..1 ↔ 0..1; cùng UUID payment_id | PaymentService verify OTP rồi deduct cùng UUID. Mỗi bên unique, nhưng OTP có thể chưa deduct hoặc đã bị cleanup; deduction có thể tồn tại khi Redis/OTP mất |

Balance và BalanceDeduction liên hệ nghiệp vụ qua cùng userId trong BalanceService, không có balance_id. Không vẽ 1-N trực tiếp như một join duy nhất vì balance.user_id không unique; một deduction chỉ gắn một balance đang được service chọn/khóa, không lưu định danh dòng đó.

Email OTP được lấy từ UserResponse qua UserClient ở PaymentController. Đây là dữ liệu nhận/snapshot, **không phải FK theo email** vì User.email không unique. PaymentSuccessfulEvent là message DTO, không có bảng lưu notification trong source.

## 5. Các điểm chưa thể chốt hoặc cần xác nhận

1. **User.student_id không nối Tuition.student_id:** một bên String NVARCHAR(20), seed kiểu SV000001; bên kia BIGINT, seed kiểu 100001. Không có mapping/chuyển đổi trong service, không suy ra cùng sinh viên từ tên/vị trí seed. Người trả tiền được xác thực và studentId nhập để tra học phí là hai định danh riêng trong luồng hiện tại.
2. **UUID paymentId không được lưu trong PaymentTransaction:** không nối `Otp.payment_id`, `BalanceDeduction.payment_id` hoặc `processing_payment_id` đến `PaymentTransaction.id`. Ghép theo amount/userId/thời gian không đủ chứng minh một giao dịch cụ thể.
3. **Balance.user_id và Tuition.student_id thiếu unique:** Optional trong repository thể hiện kỳ vọng, không tạo unique constraint. Cần xác nhận có constraint bổ sung ngoài source hoặc dữ liệu trùng thực tế hay không.
4. **Schema OTP không nhất quán:** email 100 trong schema classpath nhưng 150 trong entity/seed mysql. DDL init không sửa bảng cũ; catalog thực tế phụ thuộc lịch sử khởi tạo volume.
5. **JPA update và seed:** bốn service SQL Server dùng ddl-auto=update. Bảng khấu trừ/lịch sử không có DDL seed; kiểu SQL chính xác, naming strategy, precision/scale mặc định và constraint đã tồn tại cần kiểm tra catalog nếu cần ERD của instance đang chạy. Sơ đồ giữ kiểu Java khi source không chốt đủ, không tự suy diễn DECIMAL(19,2) cho PaymentTransaction.amount.
6. **MongoDB database runtime/index:** seed chọn payment_system nhưng kết nối thực tế lấy MONGO_URL; không đọc/xuất bí mật .env. Chưa kiểm chứng runtime database và index unique userId. Username không được khai báo unique dù login dùng findOne.
7. **Thời gian sống khác nhau:** attempt/lock 5 phút, OTP cleanup sau ngưỡng 24 giờ, deduction/lịch sử không có cleanup tương ứng. Cardinality 1-1 theo UUID không bảo đảm hai bản ghi cùng tồn tại. Không có distributed FK hoặc transaction xuyên database.
