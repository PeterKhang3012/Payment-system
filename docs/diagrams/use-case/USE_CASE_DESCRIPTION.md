# Use Case Diagram — Hệ thống thanh toán học phí

Ngày đối chiếu: 03/11/2026. Phạm vi: các controller, client/service backend, `frontend/src/api.ts`, các trang và handler trong `frontend/src/main.tsx`. Sơ đồ mô tả chức năng hiện tại, không đưa các microservice nội bộ thành actor.

## 1. Actor và ranh giới

| Actor | Vai trò |
|---|---|
| Người nộp tiền / Sinh viên | Đăng nhập, xem hồ sơ/số dư/lịch sử, tra cứu khoản học phí, yêu cầu OTP và xác nhận thanh toán. Không khẳng định người trả tiền luôn là sinh viên sở hữu khoản học phí: source nhận studentId tra cứu riêng. |
| Dịch vụ Email | Hệ thống SMTP bên ngoài tiếp nhận yêu cầu gửi OTP và email xác nhận. OTP Service và email-notification-service là thành phần bên trong project, không phải actor. |

System boundary: **HỆ THỐNG THANH TOÁN HỌC PHÍ**. Actor chính bên trái; Email bên phải. Association là đường liền không mũi tên. Sơ đồ không hiển thị REST, JWT, Redis hoặc database; các thông tin kỹ thuật bên dưới chỉ dùng để truy vết implementation.

## 2. Danh sách Use Case

| Use Case | Mô tả / điều kiện |
|---|---|
| Đăng nhập | Nhập username/password; hệ thống đối chiếu tài khoản. Sai thông tin thì từ chối. |
| Đăng xuất | Frontend xóa token và danh tính trong bộ nhớ. Không có API logout hoặc thu hồi token phía server. |
| Xem thông tin người dùng | Đọc họ tên, email, điện thoại, thời điểm tạo của người dùng hiện tại. Không có chức năng sửa hồ sơ. |
| Xem số dư khả dụng | Đọc/làm mới số dư từ trang tổng quan hoặc trang số dư. Khác kiểm tra đủ tiền trong quy trình thanh toán. |
| Xem và lọc lịch sử giao dịch | Đọc/làm mới lịch sử của người hiện tại; lọc mã giao dịch/mã học phí và trạng thái trên frontend. Không suy ra có API tìm kiếm lịch sử phía server. |
| Tra cứu thông tin sinh viên và học phí | Nhập mã sinh viên dạng số, nhận tên sinh viên, khoản tiền và trạng thái học phí. Không phải tra cứu một hồ sơ sinh viên độc lập. |
| Yêu cầu mã OTP | Sau khi đồng ý điều khoản, hệ thống kiểm tra hồ sơ, học phí chưa thanh toán và số dư; tạo lượt thanh toán rồi sinh/gửi OTP đến email tài khoản. Bất kỳ kiểm tra nào lỗi đều dừng luồng. |
| Kiểm tra số dư khả dụng | So sánh số dư với toàn bộ học phí; dùng khi yêu cầu OTP và khi xác nhận thanh toán. Kiểm tra số dư lần đầu không bảo đảm lần khấu trừ sau sẽ thành công. |
| Gửi email OTP | Gửi mã theo lượt thanh toán qua SMTP. Đây là bước bắt buộc trên luồng yêu cầu OTP hợp lệ; có thể lỗi gửi. |
| Thanh toán học phí | Người dùng nhập mã OTP và xác nhận. Hệ thống kiểm tra lượt thanh toán thuộc người dùng rồi thực hiện các bước có điều kiện bên dưới. |
| Xác thực giao dịch bằng OTP | Kiểm tra mã, thời hạn, trạng thái sử dụng và giới hạn thử; thành công đánh dấu đã dùng. Không có một trang/API frontend riêng để xác thực mà không tiếp tục thanh toán. |
| Trừ số tiền khỏi tài khoản | Chỉ sau OTP hợp lệ, học phí được claim và kiểm tra đủ tiền; Balance còn kiểm tra lại trong thao tác ghi. Cùng paymentId/payload đã khấu trừ trả kết quả cũ, không trừ lần hai. |
| Cập nhật trạng thái thanh toán học phí | Hoàn tất học phí thành PAID khi đã xác nhận khấu trừ và lượt thanh toán sở hữu claim. Claim PROCESSING/release là xử lý nội bộ, không phải chức năng người dùng riêng. |
| Lưu lịch sử giao dịch | Ghi lịch sử PAID sau khi cập nhật học phí thành công. Lỗi lưu vẫn có thể xảy ra; không giả định tự rollback tiền/học phí. |
| Gửi email xác nhận giao dịch | Chỉ được gọi sau khấu trừ, cập nhật học phí và lưu lịch sử thành công. Là bước đồng bộ trong implementation, không phải lựa chọn bật/tắt của người dùng. |

Trang kết quả chỉ xuất hiện sau khi `api.verify()` trả thành công; không phải use case truy vấn trạng thái giao dịch riêng. Chấp nhận điều khoản là điều kiện của Yêu cầu mã OTP, không thêm một mục tiêu nghiệp vụ độc lập.

## 3. Ý nghĩa include/extend

`<<include>>` đi từ use case gốc đến use case được bao gồm. `<<extend>>` đi từ use case mở rộng về use case gốc **Thanh toán học phí**. Include mô tả hành vi bắt buộc trong luồng hợp lệ, không có nghĩa mọi lời gọi lỗi phải chạy hết tất cả hành vi hoặc hành vi luôn thành công.

| Quan hệ | Điều kiện/ý nghĩa |
|---|---|
| Yêu cầu mã OTP → Kiểm tra số dư khả dụng: include | Kiểm tra bắt buộc sau khi hồ sơ/học phí hợp lệ. |
| Yêu cầu mã OTP → Gửi email OTP: include | Mọi yêu cầu hoàn tất hợp lệ phải đi qua gửi email; từ chối điều khoản/học phí/số dư thì dừng trước bước gửi. |
| Thanh toán học phí → Xác thực giao dịch bằng OTP: include | Bắt buộc khi lượt thanh toán và quyền sở hữu hợp lệ; sai OTP thì dừng. |
| Thanh toán học phí → Kiểm tra số dư khả dụng: include | Bắt buộc trên luồng tiếp tục sau OTP và claim thành công. Không thực hiện nếu bị từ chối sớm. |
| Thanh toán học phí → Trừ số tiền khỏi tài khoản: include | Bước cốt lõi sau OTP hợp lệ, claim và kiểm tra đủ số dư. |
| Thanh toán học phí → Cập nhật trạng thái thanh toán học phí: include | Bước cốt lõi sau phản hồi khấu trừ thành công. |
| Thanh toán học phí → Lưu lịch sử giao dịch: include | Bước cốt lõi sau khi hoàn tất trạng thái học phí. |
| Gửi email xác nhận giao dịch → Thanh toán học phí: extend | Điều kiện [Thanh toán thành công]: sau khi khấu trừ, cập nhật học phí và ghi lịch sử thành công. |

Trừ tiền, cập nhật học phí và lưu lịch sử là hành vi cốt lõi được include trong Thanh toán học phí. Điều kiện tiếp tục và lỗi từng bước thuộc luồng thực thi, không phải lý do dùng extend. Include không bảo đảm bước đó thành công hoặc buộc chạy bước sau khi bước trước lỗi. Chỉ email xác nhận dùng extend tại điều kiện [Thanh toán thành công]; implementation gọi đồng bộ khi đạt điều kiện, không phải lựa chọn bỏ qua của người dùng. Sơ đồ không biểu diễn thứ tự thực thi; xem mục 4. Đăng nhập là tiền điều kiện, không phải include đăng nhập lại mỗi lần. Yêu cầu OTP diễn ra trước xác nhận, không sinh mã mới mỗi lần verify.

## 4. Thứ tự nghiệp vụ đã đối chiếu

1. Đăng nhập; frontend có thể đọc hồ sơ, số dư và lịch sử.
2. Nhập studentId để tra thông tin sinh viên trên khoản học phí; đồng ý điều khoản rồi yêu cầu OTP.
3. Backend đọc hồ sơ người trả tiền, kiểm tra điều khoản, học phí UNPAID và đủ số dư; tạo lượt thanh toán rồi yêu cầu sinh OTP/gửi email. Email đến từ tài khoản hiện tại, không do người dùng nhập tùy ý ở form thanh toán.
4. Người dùng nhập OTP, bấm xác nhận. Backend đọc lại hồ sơ, kiểm tra owner của lượt thanh toán và quyền xử lý trước khi verify OTP.
5. OTP hợp lệ được đánh dấu used; backend đọc lại học phí, yêu cầu UNPAID rồi claim PROCESSING cho lượt thanh toán.
6. Đọc/kiểm tra lại số dư, gọi khấu trừ toàn bộ học phí. Số tiền lấy từ dữ liệu học phí backend, không lấy số tiền client tự nhập.
7. Chỉ khi khấu trừ trả kết quả thành công mới chuyển học phí PAID, sau đó lưu lịch sử, sau đó gọi gửi email xác nhận.
8. Xóa lượt thanh toán sau gửi email thành công; giải phóng khóa xử lý trong finally. Frontend chuyển trang kết quả khi toàn bộ lời gọi verify trả thành công.

Đây là luồng thành công và các điều kiện tiếp tục, không phải bảo đảm mọi bước cùng thành công. Sau khi bắt đầu gọi khấu trừ, lỗi/timeout có thể để học phí PROCESSING; khi email xác nhận lỗi, tiền/học phí/lịch sử có thể đã cập nhật nhưng API vẫn báo lỗi. Không có hoàn tiền hoặc rollback xuyên service tự động. Khái niệm “Thanh toán thành công” trên nhánh email là khấu trừ + học phí PAID + lịch sử đã lưu, khác việc API cuối cùng chắc chắn thành công hoặc email chắc chắn được giao đến hộp thư.

## 5. Truy vết source và endpoint

Đường dẫn dưới đây là endpoint backend; frontend dùng proxy tương ứng trong `api.ts`.

| Căn cứ | Chức năng |
|---|---|
| [auth.route.js](../../../Backend/auth-service/src/routes/auth.route.js), [auth.service.js](../../../Backend/auth-service/src/services/auth.service.js) | POST /login; bcrypt và phát hành phiên đăng nhập bằng token |
| [UserController](../../../Backend/User/src/main/java/com/MyProject/User/controller/UserController.java) | GET /api/users/me |
| [BalanceController](../../../Backend/balance-management/src/main/java/com/myproject/balance/controller/BalanceController.java), [BalanceService](../../../Backend/balance-management/src/main/java/com/myproject/balance/service/BalanceService.java) | GET /api/balances/me; POST /api/balances/deduct dùng trong điều phối |
| [TuitionController](../../../Backend/tuition-management/src/main/java/com/myproject/tuition/controller/TuitionController.java) | GET /api/tuitions/{studentId}; PUT claim/paid/release là xử lý nội bộ của workflow |
| [PaymentController](../../../Backend/payment-service/src/main/java/com/myproject/payment/controller/PaymentController.java), [PaymentService](../../../Backend/payment-service/src/main/java/com/myproject/payment/service/PaymentService.java) | POST /api/payments/request-otp; POST /api/payments/verify; thứ tự các bước |
| [PaymentTransactionController](../../../Backend/payment-service/src/main/java/com/myproject/payment/controller/PaymentTransactionController.java) | GET /api/payment-transactions/history |
| [OtpController](../../../Backend/otp-service/src/main/java/com/paymentsystem/otpservice/controller/OtpController.java), [OtpService](../../../Backend/otp-service/src/main/java/com/paymentsystem/otpservice/service/OtpService.java), [EmailService](../../../Backend/otp-service/src/main/java/com/paymentsystem/otpservice/service/EmailService.java) | POST /api/otp/generate, /verify; sinh/xác thực và gửi OTP qua SMTP |
| [EmailController](../../../Backend/email-notification-service/src/main/java/com/myproject/emailnotification/controller/EmailController.java), [EmailNotificationService](../../../Backend/email-notification-service/src/main/java/com/myproject/emailnotification/service/EmailNotificationService.java) | POST /api/emails/payment-success; gửi email xác nhận qua SMTP |
| [api.ts](../../../frontend/src/api.ts), [main.tsx](../../../frontend/src/main.tsx) | Các trang, lời gọi API, lọc lịch sử ở client, logout cục bộ và trang kết quả |
