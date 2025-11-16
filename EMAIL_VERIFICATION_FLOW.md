# Luồng Xác Thực Email Khi Đăng Ký

## 🎯 Mục đích
Khi người dùng đăng ký tài khoản, hệ thống sẽ gửi mã OTP 4 số đến email để xác thực chính chủ trước khi tạo tài khoản.

## 🔄 Luồng hoạt động

### Bước 1: Người dùng đăng ký
```
POST /api/auth/register
Body: {
  "firstName": "Nguyen",
  "lastName": "Van A",
  "email": "example@gmail.com",
  "password": "123456"
}
```

**Xử lý:**
1. Kiểm tra email đã tồn tại chưa
2. Tạo mã OTP 4 số ngẫu nhiên (VD: 1234)
3. Lưu OTP + thông tin đăng ký vào bảng `email_verifications` (hết hạn sau 5 phút)
4. Gửi email chứa mã OTP
5. Trả về: "Mã OTP đã được gửi đến email của bạn"

### Bước 2: Người dùng xác thực OTP
```
POST /api/auth/verify-otp
Body: {
  "email": "example@gmail.com",
  "otp": "1234"
}
```

**Xử lý:**
1. Kiểm tra OTP có hợp lệ và chưa hết hạn không
2. Nếu đúng: Tạo tài khoản từ thông tin đã lưu
3. Đánh dấu OTP đã xác thực
4. Trả về: "Xác thực thành công! Tài khoản của bạn đã được tạo"

## 📊 Database

### Bảng mới: `email_verifications`
| Cột | Mô tả |
|-----|-------|
| id | Primary key |
| email | Email đăng ký |
| otp | Mã OTP 4 số |
| created_at | Thời gian tạo |
| expires_at | Thời gian hết hạn (5 phút) |
| verified | Đã xác thực chưa |
| first_name | Tên (lưu tạm) |
| last_name | Họ (lưu tạm) |
| password | Mật khẩu đã mã hóa (lưu tạm) |

## 📁 Files đã tạo/sửa

### Files mới:
1. `Entities/EmailVerification.java` - Entity lưu OTP
2. `Repositories/EmailVerificationRepository.java` - Repository
3. `Services/EmailVerificationService.java` - Interface
4. `Services/Impl/EmailVerificationServiceImpl.java` - Logic xử lý OTP
5. `Dtos/VerifyOtpRequest.java` - DTO cho verify request
6. `Schedulers/EmailVerificationScheduler.java` - Tự động xóa OTP hết hạn

### Files đã sửa:
1. `Controllers/AuthController.java` - Thêm endpoint `/verify-otp`, sửa logic `/register`
2. `Services/EmailService.java` - Thêm method `sendOtpEmail()`
3. `Services/Impl/EmailServiceImpl.java` - Implement gửi email OTP
4. `Config/SecurityConfig.java` - Cho phép truy cập `/api/auth/verify-otp`

## ⚙️ Cấu hình Email
Đã sử dụng cấu hình có sẵn trong `application.properties`:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=quyen12079@gmail.com
spring.mail.password=vbcnfwnddeoljubg
```

## 🔐 Bảo mật
- OTP chỉ có hiệu lực 5 phút
- Mỗi lần đăng ký mới sẽ xóa OTP cũ của email đó
- Scheduler tự động xóa OTP hết hạn mỗi giờ
- Mật khẩu được mã hóa trước khi lưu tạm

## 📧 Email Template
Email gửi đi có format HTML đẹp mắt với:
- Tiêu đề: "Xác thực đăng ký tài khoản"
- Mã OTP được highlight màu xanh, font size lớn
- Thông báo hết hạn sau 5 phút

---

# 📖 Giải Thích Chi Tiết Từng File

## 1️⃣ EmailVerification.java (Entity)
**Nhiệm vụ:** Đại diện cho bảng `email_verifications` trong database

**Chức năng:**
- Lưu trữ thông tin xác thực email tạm thời
- Mỗi bản ghi = 1 lần đăng ký chưa hoàn tất
- Chứa mã OTP, thời gian tạo, thời gian hết hạn
- Lưu tạm thông tin đăng ký (firstName, lastName, password đã mã hóa)

**Cách hoạt động:**
- Khi user đăng ký → Tạo 1 record mới
- Khi user verify OTP → Đánh dấu `verified = true`
- Sau khi tạo tài khoản thành công → Record này không còn dùng nữa
- Scheduler sẽ tự động xóa các record đã hết hạn

---

## 2️⃣ EmailVerificationRepository.java
**Nhiệm vụ:** Giao tiếp với database để thao tác bảng `email_verifications`

**Các phương thức quan trọng:**

### `findByEmailAndOtpAndVerifiedFalseAndExpiresAtAfter()`
- **Tìm:** Record có email + OTP khớp, chưa verify, chưa hết hạn
- **Dùng khi:** User nhập OTP để verify
- **Trả về:** Optional<EmailVerification>

### `findFirstByEmailAndVerifiedFalseOrderByCreatedAtDesc()`
- **Tìm:** Record mới nhất của email chưa verify
- **Dùng khi:** Cần kiểm tra hoặc gửi lại OTP

### `deleteByExpiresAtBefore()`
- **Xóa:** Tất cả record đã hết hạn
- **Dùng khi:** Scheduler chạy định kỳ để dọn dẹp

### `deleteByEmail()`
- **Xóa:** Tất cả record của 1 email
- **Dùng khi:** User đăng ký lại (xóa OTP cũ)

---

## 3️⃣ EmailService.java & EmailServiceImpl.java
**Nhiệm vụ:** Gửi email qua SMTP

**Phương thức mới: `sendOtpEmail()`**

**Cách hoạt động:**
1. Nhận email người nhận và mã OTP
2. Tạo nội dung email HTML với:
   - Tiêu đề: "Xác thực đăng ký tài khoản"
   - Mã OTP được format đẹp (màu xanh, font lớn)
   - Thông báo hết hạn 5 phút
3. Gửi email qua JavaMailSender (dùng config trong application.properties)
4. Nếu gửi thất bại → Throw exception

**Tại sao cần:**
- User cần nhận mã OTP qua email để xác thực
- Đảm bảo email đăng ký là thật và thuộc về user

---

## 4️⃣ EmailVerificationService.java & EmailVerificationServiceImpl.java
**Nhiệm vụ:** Quản lý toàn bộ logic xác thực email

### Phương thức 1: `sendOtpForRegistration(RegisterRequest)`
**Nhiệm vụ:** Tạo và gửi OTP khi user đăng ký

**Cách hoạt động:**
1. Xóa OTP cũ của email này (nếu có)
2. Tạo mã OTP 4 số ngẫu nhiên (1000-9999)
3. Tạo record EmailVerification:
   - Lưu email, OTP
   - Set thời gian tạo = hiện tại
   - Set thời gian hết hạn = hiện tại + 5 phút
   - Lưu tạm firstName, lastName, password (đã mã hóa)
4. Lưu vào database
5. Gửi email chứa OTP

**Tại sao mã hóa password ngay:**
- Không lưu password gốc vào database (bảo mật)
- Khi verify xong chỉ cần copy sang bảng customers

### Phương thức 2: `verifyOtpAndRegister(email, otp)`
**Nhiệm vụ:** Xác thực OTP và tạo tài khoản

**Cách hoạt động:**
1. Tìm record có email + OTP khớp, chưa verify, chưa hết hạn
2. Nếu không tìm thấy → Return false (OTP sai hoặc hết hạn)
3. Nếu tìm thấy:
   - Lấy thông tin đã lưu tạm (firstName, lastName, password)
   - Tạo Customer mới với thông tin đó
   - Lưu vào bảng `customers`
   - Đánh dấu record EmailVerification là `verified = true`
   - Return true

**Tại sao đánh dấu verified thay vì xóa:**
- Giữ lại lịch sử để audit/debug
- Tránh user verify lại OTP cũ nhiều lần

### Phương thức 3: `cleanupExpiredOtps()`
**Nhiệm vụ:** Xóa các OTP đã hết hạn

**Cách hoạt động:**
- Xóa tất cả record có `expiresAt < thời gian hiện tại`
- Giải phóng bộ nhớ database
- Được gọi tự động bởi Scheduler

---

## 5️⃣ VerifyOtpRequest.java (DTO)
**Nhiệm vụ:** Đại diện cho request body khi verify OTP

**Chứa:**
- `email`: Email cần verify
- `otp`: Mã OTP 4 số

**Validation:**
- Email không được trống và phải đúng format
- OTP không được trống và phải là 4 chữ số

**Tại sao cần:**
- Đảm bảo dữ liệu đầu vào hợp lệ
- Tránh lỗi khi xử lý
- Trả về message lỗi rõ ràng cho user

---

## 6️⃣ EmailVerificationScheduler.java
**Nhiệm vụ:** Tự động dọn dẹp OTP hết hạn

**Cách hoạt động:**
- Chạy tự động mỗi 1 giờ (3600000 milliseconds)
- Gọi `emailVerificationService.cleanupExpiredOtps()`
- Xóa các record đã hết hạn khỏi database

**Tại sao cần:**
- Database không bị phình to vì quá nhiều OTP cũ
- Tự động hóa, không cần can thiệp thủ công
- Đảm bảo hiệu suất hệ thống

**Thời gian chạy:**
- Lần đầu: Khi ứng dụng khởi động
- Sau đó: Mỗi 1 giờ một lần

---

## 7️⃣ AuthController.java (Đã sửa)
**Nhiệm vụ:** Xử lý các request liên quan đến authentication

### Endpoint đã sửa: `POST /api/auth/register`
**Trước đây:** Tạo tài khoản trực tiếp

**Bây giờ:**
1. Kiểm tra email đã tồn tại chưa
2. Gọi `emailVerificationService.sendOtpForRegistration()`
3. Trả về message: "Mã OTP đã được gửi đến email"

**Response:**
```json
{
  "message": "Mã OTP đã được gửi đến email của bạn...",
  "email": "example@gmail.com"
}
```

### Endpoint mới: `POST /api/auth/verify-otp`
**Nhiệm vụ:** Xác thực OTP và hoàn tất đăng ký

**Cách hoạt động:**
1. Nhận email + OTP từ request body
2. Gọi `emailVerificationService.verifyOtpAndRegister()`
3. Nếu thành công → Return "Xác thực thành công!"
4. Nếu thất bại → Return "Mã OTP không hợp lệ hoặc đã hết hạn"

**Response thành công:**
```json
{
  "message": "Xác thực thành công! Tài khoản của bạn đã được tạo."
}
```

**Response thất bại:**
```json
{
  "message": "Mã OTP không hợp lệ hoặc đã hết hạn"
}
```

---

## 8️⃣ SecurityConfig.java (Đã sửa)
**Nhiệm vụ:** Cấu hình bảo mật cho ứng dụng

**Thay đổi:**
- Thêm `/api/auth/verify-otp` vào danh sách endpoint công khai

**Tại sao:**
- User chưa có tài khoản → Chưa có token
- Cần cho phép truy cập không cần authentication
- Giống như `/register` và `/login`

**Trước:**
```java
.requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
```

**Sau:**
```java
.requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/verify-otp").permitAll()
```

---

# 🔄 Tổng Quan Luồng Dữ Liệu

```
User nhập form đăng ký
    ↓
AuthController.register() nhận request
    ↓
EmailVerificationService.sendOtpForRegistration()
    ↓
├─ Tạo mã OTP 4 số
├─ Lưu vào EmailVerification (database)
└─ EmailService.sendOtpEmail() → Gửi email
    ↓
User nhận email, nhập OTP
    ↓
AuthController.verifyOtp() nhận request
    ↓
EmailVerificationService.verifyOtpAndRegister()
    ↓
├─ Tìm record EmailVerification khớp
├─ Tạo Customer mới
├─ Lưu vào bảng customers
└─ Đánh dấu EmailVerification.verified = true
    ↓
Trả về "Xác thực thành công!"
    ↓
User có thể login
```

---

# ⏰ Timeline

| Thời điểm | Sự kiện |
|-----------|----------|
| T+0s | User gửi request đăng ký |
| T+1s | OTP được tạo và lưu vào database |
| T+2s | Email chứa OTP được gửi đi |
| T+3s | User nhận email |
| T+30s | User nhập OTP và verify |
| T+31s | Tài khoản được tạo thành công |
| T+5 phút | OTP hết hạn (nếu chưa verify) |
| T+1 giờ | Scheduler xóa OTP hết hạn khỏi database |
