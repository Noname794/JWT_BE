# 🔧 OAuth2 Client Setup Guide

## 📋 Bước 1: Tạo OAuth2 Client trong Database

Bạn cần insert một record vào bảng `oauth2_clients` để đăng ký client app.

### SQL Script

```sql
-- Tạo bảng oauth2_clients (nếu chưa có)
CREATE TABLE IF NOT EXISTS oauth2_clients (
    client_id VARCHAR(255) PRIMARY KEY,
    client_secret VARCHAR(255) NOT NULL,
    client_name VARCHAR(255) NOT NULL,
    redirect_uris TEXT,
    scopes TEXT,
    authorization_grant_types TEXT,
    client_authentication_methods TEXT,
    access_token_validity_seconds INT DEFAULT 3600,
    refresh_token_validity_seconds INT DEFAULT 86400
);

-- Insert React client
INSERT INTO oauth2_clients (
    client_id,
    client_secret,
    client_name,
    redirect_uris,
    scopes,
    authorization_grant_types,
    client_authentication_methods,
    access_token_validity_seconds,
    refresh_token_validity_seconds
) VALUES (
    'react-client',
    '{bcrypt}$2a$10$XYZ...', -- Mã hóa BCrypt của 'react-secret'
    'React Web Application',
    'http://localhost:3000/oauth2/callback',
    'read,write,openid',
    'authorization_code,refresh_token',
    'client_secret_basic,client_secret_post',
    3600,  -- 1 hour
    2592000  -- 30 days
);
```

### Tạo BCrypt Password

**Option 1: Dùng Online Tool**
- Vào: https://bcrypt-generator.com/
- Nhập: `react-secret`
- Rounds: 10
- Copy hash và thêm prefix `{bcrypt}`

**Option 2: Dùng Java Code**

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateBcrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String secret = "react-secret";
        String encoded = encoder.encode(secret);
        System.out.println("{bcrypt}" + encoded);
    }
}
```

**Option 3: Dùng Spring Boot Application**

Tạo endpoint tạm thời:

```java
@RestController
@RequestMapping("/api/dev")
public class DevController {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/encode-password")
    public String encodePassword(@RequestParam String password) {
        return "{bcrypt}" + passwordEncoder.encode(password);
    }
}

// Gọi: http://localhost:8080/api/dev/encode-password?password=react-secret
```

---

## 📋 Bước 2: Cấu hình Route trong React

Thêm route cho OAuth2 callback page.

### File: `App.js` hoặc `Router.jsx`

```javascript
import OAuth2CallbackPage from './components/Auth/OAuth2CallbackPage';

// Trong routes
<Route 
  path="/oauth2/callback" 
  element={
    <OAuth2CallbackPage 
      showSuccess={showSuccess}
      showError={showError}
    />
  } 
/>
```

---

## 📋 Bước 3: Test OAuth2 Flow

### 3.1. Khởi động Backend

```bash
cd PQ_FE-main
mvn spring-boot:run
```

### 3.2. Khởi động Frontend

```bash
cd a_ReactApp_WebsiteElectronics/WebsiteElectronics
npm start
```

### 3.3. Test Flow

1. **Vào trang login**: http://localhost:3000/LoginPage

2. **Click nút "Đăng nhập bằng OAuth2"**

3. **Browser redirect đến**: 
   ```
   http://localhost:8080/oauth2/authorize?
     response_type=code&
     client_id=react-client&
     redirect_uri=http://localhost:3000/oauth2/callback&
     scope=read write openid&
     state=abc123
   ```

4. **Nếu chưa login → Hiện form login**
   - Nhập email/password
   - Submit

5. **Sau khi login → Hiện Consent Screen**
   ```
   ┌─────────────────────────────────────┐
   │  React Web Application              │
   │  muốn truy cập tài khoản của bạn    │
   │                                     │
   │  ☑ Read your data                   │
   │  ☑ Write data                       │
   │  ☑ Access your profile              │
   │                                     │
   │  [Approve]  [Deny]                  │
   └─────────────────────────────────────┘
   ```

6. **Click Approve**

7. **Redirect về frontend với code**:
   ```
   http://localhost:3000/oauth2/callback?
     code=abc123xyz&
     state=abc123
   ```

8. **OAuth2CallbackPage xử lý**:
   - Validate state
   - Exchange code for token
   - Save token
   - Redirect to /home

---

## 🔍 Troubleshooting

### Lỗi 1: "Invalid client"

**Nguyên nhân**: Client chưa được tạo trong database

**Giải pháp**:
```sql
-- Kiểm tra client có tồn tại không
SELECT * FROM oauth2_clients WHERE client_id = 'react-client';

-- Nếu không có, insert lại
```

### Lỗi 2: "Invalid redirect_uri"

**Nguyên nhân**: Redirect URI không match với database

**Giải pháp**:
```sql
-- Kiểm tra redirect_uris
SELECT redirect_uris FROM oauth2_clients WHERE client_id = 'react-client';

-- Update nếu sai
UPDATE oauth2_clients 
SET redirect_uris = 'http://localhost:3000/oauth2/callback'
WHERE client_id = 'react-client';
```

### Lỗi 3: "Invalid client credentials"

**Nguyên nhân**: Client secret sai hoặc không đúng format BCrypt

**Giải pháp**:
```sql
-- Update client_secret với BCrypt hash mới
UPDATE oauth2_clients 
SET client_secret = '{bcrypt}$2a$10$...'
WHERE client_id = 'react-client';
```

### Lỗi 4: "State mismatch"

**Nguyên nhân**: State parameter không khớp (CSRF protection)

**Giải pháp**:
- Clear sessionStorage
- Thử lại từ đầu
- Kiểm tra code trong LoginPage.jsx

### Lỗi 5: CORS Error

**Nguyên nhân**: Backend chưa cho phép CORS từ frontend

**Giải pháp**:

```java
// SecurityConfig.java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

---

## 📊 Database Schema

### Bảng: `oauth2_clients`

| Column | Type | Description |
|--------|------|-------------|
| client_id | VARCHAR(255) | ID của client app (PK) |
| client_secret | VARCHAR(255) | Secret đã mã hóa BCrypt |
| client_name | VARCHAR(255) | Tên hiển thị của app |
| redirect_uris | TEXT | Comma-separated redirect URIs |
| scopes | TEXT | Comma-separated scopes |
| authorization_grant_types | TEXT | authorization_code, refresh_token |
| client_authentication_methods | TEXT | client_secret_basic, client_secret_post |
| access_token_validity_seconds | INT | Thời gian hết hạn access token (giây) |
| refresh_token_validity_seconds | INT | Thời gian hết hạn refresh token (giây) |

### Bảng: `oauth2_authorization`

Spring Authorization Server tự động tạo và quản lý bảng này.

---

## 🎯 Multiple Clients Example

Nếu bạn có nhiều client apps (Mobile, Web, Third-party):

```sql
-- Web App
INSERT INTO oauth2_clients VALUES (
    'react-client',
    '{bcrypt}$2a$10$...',
    'React Web Application',
    'http://localhost:3000/oauth2/callback',
    'read,write,openid',
    'authorization_code,refresh_token',
    'client_secret_basic',
    3600,
    2592000
);

-- Mobile App
INSERT INTO oauth2_clients VALUES (
    'mobile-app',
    '{bcrypt}$2a$10$...',
    'Mobile Application',
    'myapp://oauth2/callback',
    'read,write,openid',
    'authorization_code,refresh_token',
    'client_secret_post',
    3600,
    2592000
);

-- Third-party App (Postman for testing)
INSERT INTO oauth2_clients VALUES (
    'postman-client',
    '{bcrypt}$2a$10$...',
    'Postman Testing',
    'https://oauth.pstmn.io/v1/callback',
    'read',
    'authorization_code',
    'client_secret_basic',
    3600,
    86400
);
```

---

## 🔐 Security Best Practices

1. **Client Secret**:
   - ✅ Luôn mã hóa BCrypt
   - ✅ Không commit vào Git
   - ✅ Dùng environment variables trong production

2. **Redirect URI**:
   - ✅ Chỉ cho phép HTTPS trong production
   - ✅ Whitelist chính xác, không dùng wildcard
   - ✅ Validate chặt chẽ

3. **Scopes**:
   - ✅ Chỉ cấp quyền tối thiểu cần thiết
   - ✅ User phải approve scopes
   - ✅ Kiểm tra scopes trong API endpoints

4. **State Parameter**:
   - ✅ Luôn validate state (CSRF protection)
   - ✅ Generate random, unique cho mỗi request
   - ✅ Lưu trong sessionStorage, không localStorage

5. **Token Expiry**:
   - ✅ Access token: 1 hour
   - ✅ Refresh token: 30 days
   - ✅ Implement auto-refresh mechanism

---

## 📝 Complete Setup Checklist

- [ ] Tạo bảng `oauth2_clients` trong database
- [ ] Insert client record với BCrypt secret
- [ ] Tạo `OAuth2CallbackPage.jsx` component
- [ ] Thêm route `/oauth2/callback` trong React Router
- [ ] Thêm nút OAuth2 login trong `LoginPage.jsx`
- [ ] Cấu hình CORS trong backend
- [ ] Test OAuth2 flow end-to-end
- [ ] Kiểm tra token được lưu đúng
- [ ] Kiểm tra redirect sau login
- [ ] Test error cases (invalid code, state mismatch...)

---

Hoàn thành! Bây giờ bạn có thể đăng nhập bằng OAuth2! 🎉
