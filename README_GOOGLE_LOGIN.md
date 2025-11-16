# 🔐 GOOGLE LOGIN + JWT AUTHENTICATION

## 📚 TÀI LIỆU HƯỚNG DẪN

Project này đã implement **Google Login với JWT Authentication**. Dưới đây là các file hướng dẫn chi tiết:

---

## 📖 CÁC FILE HƯỚNG DẪN

### 1. **GOOGLE_LOGIN_IMPLEMENTATION_SUMMARY.md** ⭐
**Nội dung:** Tóm tắt chi tiết tất cả những gì đã implement
- Các file đã sửa/tạo
- Code chi tiết từng phần
- Flow hoạt động
- API endpoints
- Lỗi đã sửa

**Đọc file này để:** Hiểu rõ implementation và code

---

### 2. **GOOGLE_LOGIN_GUIDE.md** 🎨
**Nội dung:** Hướng dẫn implement Frontend (React)
- Cài đặt dependencies
- Setup Google OAuth Provider
- Tạo Google Login Button
- Config Axios Interceptor
- Call API với JWT Token
- Code examples đầy đủ

**Đọc file này để:** Implement frontend

---

### 3. **TEST_API.md** 🧪
**Nội dung:** Hướng dẫn test API
- Cách lấy Google ID Token
- Test với Postman/cURL
- Test scenarios
- Troubleshooting
- Check database

**Đọc file này để:** Test backend API

---

### 4. **SET_JAVA_HOME.md** ⚙️
**Nội dung:** Hướng dẫn set JAVA_HOME vĩnh viễn
- 3 cách set JAVA_HOME
- Verify installation
- Troubleshooting

**Đọc file này để:** Fix Java version issue

---

## 🚀 QUICK START

### **Backend:**

```powershell
# 1. Set JAVA_HOME (nếu chưa set vĩnh viễn)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# 2. Build project
mvn clean install

# 3. Run application
mvn spring-boot:run

# Server chạy tại: http://localhost:8080
```

### **Frontend:**

```bash
# 1. Install dependencies
npm install @react-oauth/google

# 2. Xem hướng dẫn chi tiết
# Đọc file: GOOGLE_LOGIN_GUIDE.md
```

---

## 🎯 FLOW HOẠT ĐỘNG

### **Flow 1: OAuth2 Redirect (Flow cũ)**
```
User → Backend → Google → Backend → JWT → Frontend
```
**URL:** `http://localhost:8080/oauth2/authorization/google`

### **Flow 2: API Verify (Flow mới)** ⭐
```
User → Frontend → Google → ID Token → Backend API → JWT → Frontend
```
**API:** `POST /api/auth/google/verify`

---

## 📋 API ENDPOINTS

### **Authentication:**
- `POST /api/auth/login` - Login thông thường
- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/google/verify` - **Verify Google ID Token** ⭐
- `POST /api/auth/refresh` - Refresh JWT token
- `GET /api/auth/validate` - Validate JWT token

### **Protected APIs:**
- `GET /api/customers` - Lấy danh sách customers (authenticated)
- `GET /api/electronics` - Lấy danh sách products (authenticated)
- `POST /api/electronics` - Tạo product (ADMIN only)
- `PUT /api/electronics/{id}` - Update product (ADMIN only)
- `DELETE /api/electronics/{id}` - Xóa product (ADMIN only)

---

## 🔐 AUTHENTICATION

### **1. Login với Google:**
```bash
curl -X POST http://localhost:8080/api/auth/google/verify \
  -H "Content-Type: application/json" \
  -d '{"idToken":"YOUR_GOOGLE_ID_TOKEN"}'
```

**Response:**
```json
{
  "message": "Google login successful",
  "email": "user@gmail.com",
  "customerId": 1,
  "roles": ["ROLE_USER"],
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

### **2. Call API với JWT:**
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/customers
```

---

## 🗄️ DATABASE

### **Table: customers**

Cần có các columns:
```sql
CREATE TABLE customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    country VARCHAR(100),
    role VARCHAR(50) DEFAULT 'USER',
    oauth_provider VARCHAR(50) DEFAULT 'LOCAL',
    oauth_provider_id VARCHAR(255)
);
```

**Nếu table đã tồn tại, thêm columns:**
```sql
ALTER TABLE customers 
ADD COLUMN oauth_provider VARCHAR(50) DEFAULT 'LOCAL',
ADD COLUMN oauth_provider_id VARCHAR(255);
```

---

## 🔑 CONFIGURATION

### **application.properties:**

```properties
# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/google

# JWT
jwt.secret=YOUR_SECRET_KEY_HERE
jwt.expiration=86400000

# CORS
# Đã config trong SecurityConfig.java cho http://localhost:5173
```

---

## 🛠️ TECH STACK

### **Backend:**
- Spring Boot 3.5.5
- Spring Security
- Spring OAuth2 Client
- JWT (jjwt 0.9.1)
- Google API Client 2.2.0
- MySQL
- MongoDB

### **Frontend (Khuyến nghị):**
- React
- @react-oauth/google
- Axios

---

## 📊 PROJECT STRUCTURE

```
src/main/java/com/websiteElectronics/websiteElectronics/
├── Config/
│   ├── SecurityConfig.java
│   ├── JwtAuthenticationFilter.java
│   ├── OAuth2LoginSuccessHandler.java
│   └── OAuth2LoginFailureHandler.java
├── Controllers/
│   └── AuthController.java ⭐
├── Services/
│   ├── AuthService.java ⭐
│   └── Impl/
│       ├── AuthServiceImpl.java ⭐
│       └── JwtService.java
├── Dtos/
│   ├── LoginRequest.java
│   ├── LoginResponse.java ⭐
│   └── GoogleTokenRequest.java ⭐
├── Entities/
│   └── Customers.java
└── Repositories/
    └── CustomersRepository.java
```

**⭐ = Files mới tạo/sửa cho Google Login**

---

## ✅ FEATURES

- ✅ Google OAuth2 Login
- ✅ JWT Token Generation
- ✅ JWT Token Validation
- ✅ JWT Token Refresh
- ✅ Auto Create User
- ✅ Role-based Authorization (USER/ADMIN)
- ✅ CORS Configuration
- ✅ Password Encryption (BCrypt)
- ✅ Session Stateless
- ✅ MongoDB + MySQL Support

---

## 🧪 TESTING

### **1. Test Backend:**
```powershell
# Compile
mvn clean compile

# Run tests
mvn test

# Run application
mvn spring-boot:run
```

### **2. Test API:**
Xem file **TEST_API.md** để biết chi tiết

### **3. Test Frontend:**
Xem file **GOOGLE_LOGIN_GUIDE.md** để implement frontend

---

## 🐛 TROUBLESHOOTING

### **1. Java Version Error**
**Lỗi:** `class file has wrong version 55.0, should be 52.0`

**Giải pháp:** Xem file **SET_JAVA_HOME.md**

---

### **2. Invalid Google ID Token**
**Lỗi:** `Invalid Google ID Token`

**Nguyên nhân:**
- Token đã hết hạn
- Client ID không khớp

**Giải pháp:**
- Lấy token mới từ Google
- Verify Client ID trong application.properties

---

### **3. 401 Unauthorized**
**Lỗi:** `Token không hợp lệ hoặc đã hết hạn`

**Nguyên nhân:**
- JWT token không có trong header
- JWT token hết hạn

**Giải pháp:**
- Check header: `Authorization: Bearer <token>`
- Login lại để lấy token mới

---

### **4. CORS Error**
**Lỗi:** `Access-Control-Allow-Origin`

**Giải pháp:**
- Check frontend URL trong `SecurityConfig.java`
- Default: `http://localhost:5173`
- Update nếu frontend chạy ở port khác

---

## 📝 NOTES

1. **Security:**
   - JWT secret key nên thay đổi trong production
   - Sử dụng HTTPS trong production
   - Không hardcode credentials

2. **Token Expiration:**
   - Google ID Token: 1 giờ
   - JWT Token: 24 giờ (có thể config)

3. **User Roles:**
   - Default role cho user mới: `USER`
   - Update role trong database nếu cần ADMIN

4. **OAuth Providers:**
   - Hiện tại support: GOOGLE, LOCAL
   - Có thể thêm Facebook, GitHub, etc.

---

## 🎓 LEARNING RESOURCES

- **Spring Security OAuth2:** https://spring.io/guides/tutorials/spring-boot-oauth2/
- **Google OAuth2:** https://developers.google.com/identity/protocols/oauth2
- **JWT:** https://jwt.io/introduction
- **React OAuth:** https://www.npmjs.com/package/@react-oauth/google

---

## 👥 SUPPORT

Nếu gặp vấn đề:
1. Đọc file **GOOGLE_LOGIN_IMPLEMENTATION_SUMMARY.md**
2. Check logs trong terminal
3. Test API với **TEST_API.md**
4. Verify configuration trong **application.properties**

---

## 🎉 READY TO GO!

✅ Backend đã hoàn thiện
✅ API endpoints đã sẵn sàng
✅ Documentation đầy đủ
✅ Test cases đã có

**Bây giờ có thể:**
1. Test backend API
2. Implement frontend
3. Deploy to production

**Happy Coding!** 🚀

---

## 📄 LICENSE

This project is part of websiteElectronics application.

---

## 📧 CONTACT

For questions or support, please contact the development team.
