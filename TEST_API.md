# 🧪 TEST GOOGLE LOGIN API

## ✅ SERVER ĐANG CHẠY
```
✓ Backend: http://localhost:8080
✓ Status: RUNNING
```

---

## 📋 CÁCH TEST

### **Bước 1: Lấy Google ID Token từ Frontend**

Có 2 cách:

#### **Cách 1: Dùng Google OAuth Playground (Nhanh nhất)**
1. Truy cập: https://developers.google.com/oauthplayground/
2. Click **Settings** (góc phải)
3. Check ✓ **Use your own OAuth credentials**
4. Nhập:
   - **OAuth Client ID**: `5340255641-jrr5augmt24tjn7losin8tkb8ltimavi.apps.googleusercontent.com`
   - **OAuth Client secret**: `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`
5. Chọn scope: `https://www.googleapis.com/auth/userinfo.email`
6. Click **Authorize APIs**
7. Đăng nhập Google
8. Copy **ID Token** (phần dài dài bắt đầu bằng `eyJ...`)

#### **Cách 2: Dùng Frontend React (Đúng flow)**
```jsx
import { GoogleLogin } from '@react-oauth/google';

<GoogleLogin
  onSuccess={(response) => {
    console.log('ID Token:', response.credential);
    // Copy ID Token này để test
  }}
/>
```

---

### **Bước 2: Test API với Postman hoặc cURL**

#### **A. Test Google Login**

**Endpoint:** `POST http://localhost:8080/api/auth/google/verify`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE4MmU0..."
}
```

**Expected Response (200 OK):**
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

**cURL Command:**
```bash
curl -X POST http://localhost:8080/api/auth/google/verify \
  -H "Content-Type: application/json" \
  -d "{\"idToken\":\"YOUR_GOOGLE_ID_TOKEN_HERE\"}"
```

---

#### **B. Test Validate JWT Token**

**Endpoint:** `GET http://localhost:8080/api/auth/validate`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Expected Response (200 OK):**
```json
true
```

**cURL Command:**
```bash
curl -X GET http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

---

#### **C. Test Refresh JWT Token**

**Endpoint:** `POST http://localhost:8080/api/auth/refresh`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Expected Response (200 OK):**
```json
{
  "message": "Token refreshed",
  "email": "user@gmail.com",
  "customerId": 1,
  "roles": ["ROLE_USER"],
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

---

#### **D. Test Authenticated API (Ví dụ: Get Customers)**

**Endpoint:** `GET http://localhost:8080/api/customers`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "email": "user@gmail.com",
    "firstName": "John",
    "lastName": "Doe",
    ...
  }
]
```

**cURL Command:**
```bash
curl -X GET http://localhost:8080/api/customers \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

---

## 🔍 KIỂM TRA DATABASE

Sau khi login Google thành công, check database:

```sql
-- Xem user mới được tạo
SELECT * FROM customers WHERE oauth_provider = 'GOOGLE';

-- Xem tất cả users
SELECT id, email, first_name, last_name, role, oauth_provider 
FROM customers;
```

**Kết quả mong đợi:**
- User mới được tạo với email từ Google
- `oauth_provider` = 'GOOGLE'
- `oauth_provider_id` = Google user ID
- `role` = 'USER'
- `password` = '' (empty)

---

## 📊 TEST SCENARIOS

### **Scenario 1: User mới login lần đầu**
```
1. User login Google
2. Backend verify ID Token
3. Email chưa có trong DB
4. Tạo user mới với role USER
5. Generate JWT token
6. Return JWT + user info
```

### **Scenario 2: User đã tồn tại login lại**
```
1. User login Google
2. Backend verify ID Token
3. Email đã có trong DB
4. Lấy thông tin user + role
5. Generate JWT token mới
6. Return JWT + user info
```

### **Scenario 3: User LOCAL login Google lần đầu**
```
1. User đã có account (đăng ký bằng email/password)
2. User login Google với cùng email
3. Backend update oauth_provider = 'GOOGLE'
4. User có thể login bằng cả 2 cách
```

---

## ⚠️ TROUBLESHOOTING

### **1. Error: Invalid Google ID Token**
**Nguyên nhân:**
- Token đã hết hạn (Google ID Token chỉ có hiệu lực 1 giờ)
- Token không đúng format
- Client ID không khớp

**Giải pháp:**
- Lấy token mới từ Google
- Verify Client ID trong application.properties

---

### **2. Error: 401 Unauthorized**
**Nguyên nhân:**
- JWT token không có trong header
- JWT token hết hạn
- JWT token không hợp lệ

**Giải pháp:**
- Check header: `Authorization: Bearer <token>`
- Login lại để lấy token mới
- Verify JWT secret key

---

### **3. Error: 403 Forbidden**
**Nguyên nhân:**
- User không có quyền truy cập endpoint
- Role không đúng (cần ADMIN nhưng chỉ có USER)

**Giải pháp:**
- Check role trong database
- Update role nếu cần: `UPDATE customers SET role='ADMIN' WHERE email='user@gmail.com'`

---

### **4. Error: Connection refused**
**Nguyên nhân:**
- Backend chưa chạy
- Port 8080 đã bị chiếm

**Giải pháp:**
- Start backend: `mvn spring-boot:run`
- Check port: `netstat -ano | findstr :8080`

---

## 📝 LOGS

Khi test, check logs trong terminal để debug:

```
✓ Google token verified - Email: user@gmail.com
✓ Created new customer from OAuth - Provider: GOOGLE, Email: user@gmail.com
✓ Updated existing customer with OAuth provider: GOOGLE
```

---

## 🎯 NEXT STEPS

Sau khi test backend thành công:

1. ✅ **Implement Frontend** - Xem file `GOOGLE_LOGIN_GUIDE.md`
2. ✅ **Setup CORS** - Đã config cho `http://localhost:5173`
3. ✅ **Test End-to-End** - Frontend → Backend → Database
4. ✅ **Deploy to Production** - Update CORS, JWT secret, Google credentials

---

## 💡 TIPS

1. **Dùng Postman Collection** để lưu các request test
2. **Save JWT Token** vào Postman Environment để dễ test
3. **Check Database** sau mỗi lần test để verify data
4. **Monitor Logs** để debug lỗi nhanh hơn
5. **Test cả 2 flows**: OAuth2 redirect và API verify

---

## 🚀 READY TO GO!

✅ Backend đã sẵn sàng
✅ API endpoints đã hoạt động
✅ JWT authentication đã implement
✅ Google OAuth đã integrate

**Bây giờ có thể test hoặc implement frontend!** 🎉
