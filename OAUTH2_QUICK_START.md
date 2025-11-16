# 🚀 OAuth2 Quick Start Guide

## ✅ Tóm tắt: Bạn đã có SẴN tất cả!

Backend của bạn đã có **Oauth2ClientInitializer** - nó sẽ **TỰ ĐỘNG** tạo OAuth2 client khi app khởi động!

**Bạn KHÔNG CẦN chạy SQL thủ công!** 🎉

---

## 📋 Bước 1: Thêm Route trong React

**File**: `App.js` hoặc `Router.jsx`

```javascript
import OAuth2CallbackPage from './components/Auth/OAuth2CallbackPage';

// Thêm route này
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

## 📋 Bước 2: Khởi động Backend

```bash
cd PQ_FE-main
mvn spring-boot:run
```

**Khi backend start, bạn sẽ thấy log**:
```
✅ OAuth2 Client 'react-client' initialized:
   Client ID: react-client
   Client Secret: react-secret
   Redirect URI: http://localhost:3000/oauth2/callback
   Scopes: read, write, openid
```

**Hoặc nếu đã tồn tại**:
```
ℹ️  OAuth2 Client 'react-client' already exists
```

---

## 📋 Bước 3: Khởi động Frontend

```bash
cd a_ReactApp_WebsiteElectronics/WebsiteElectronics
npm start
```

---

## 📋 Bước 4: Test OAuth2 Login

1. Vào: http://localhost:3000/LoginPage

2. Click nút: **"Đăng nhập bằng OAuth2 (Third-party App)"** 🔐

3. Browser redirect đến:
   ```
   http://localhost:8080/oauth2/authorize?
     response_type=code&
     client_id=react-client&
     redirect_uri=http://localhost:3000/oauth2/callback&
     scope=read write openid&
     state=abc123
   ```

4. **Nếu chưa login** → Hiện form login
   - Nhập email/password
   - Submit

5. **Sau khi login** → Hiện Consent Screen
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

6. Click **Approve**

7. Redirect về:
   ```
   http://localhost:3000/oauth2/callback?
     code=abc123xyz&
     state=abc123
   ```

8. **OAuth2CallbackPage** xử lý:
   - ✅ Validate state
   - ✅ Exchange code for token
   - ✅ Save token to localStorage
   - ✅ Redirect to /home

9. **Done!** 🎉

---

## 🔍 Kiểm tra Client đã được tạo chưa

### Option 1: Check Database

```sql
SELECT * FROM oauth2_clients WHERE client_id = 'react-client';
```

**Kết quả mong đợi**:
```
client_id: react-client
client_secret: {bcrypt}$2a$10$...
client_name: React Web Application
redirect_uris: http://localhost:3000/oauth2/callback
scopes: read,write,openid
authorization_grant_types: authorization_code,refresh_token
access_token_validity_seconds: 3600
refresh_token_validity_seconds: 2592000
```

### Option 2: Check Backend Log

Khi backend start, xem console log:
```
✅ OAuth2 Client 'react-client' initialized:
   Client ID: react-client
   Client Secret: react-secret
   Redirect URI: http://localhost:3000/oauth2/callback
   Scopes: read, write, openid
```

---

## 🎯 Cách hoạt động

### Oauth2ClientInitializer.java

```java
@Service
public class Oauth2ClientInitializer {
    
    @PostConstruct  // ← Chạy TỰ ĐỘNG khi app start
    public void initializeDefaultClient() {
        // Kiểm tra client đã tồn tại chưa
        if (oauth2ClientsRepository.findByClientId("react-client").isEmpty()) {
            // Tạo client mới
            Oauth2Clients reactClient = Oauth2Clients.builder()
                .clientId("react-client")
                .clientSecret(passwordEncoder.encode("react-secret"))
                .clientName("React Web Application")
                .redirectUris("http://localhost:3000/oauth2/callback")
                .scopes("read,write,openid")
                .authorizationGrantTypes("authorization_code,refresh_token")
                .clientAuthenticationMethods("client_secret_basic,client_secret_post")
                .accessTokenValiditySeconds(3600)
                .refreshTokenValiditySeconds(2592000)
                .build();
            
            // Lưu vào database
            oauth2ClientsRepository.save(reactClient);
            
            System.out.println("✅ OAuth2 Client 'react-client' initialized");
        }
    }
}
```

**Lợi ích**:
- ✅ Tự động tạo client khi app start
- ✅ Không cần chạy SQL thủ công
- ✅ Chỉ tạo 1 lần (check exists trước)
- ✅ BCrypt được tạo tự động
- ✅ Perfect cho development!

---

## 🔧 Nếu muốn thay đổi Client Config

### Option 1: Xóa và tạo lại

```sql
-- Xóa client cũ
DELETE FROM oauth2_clients WHERE client_id = 'react-client';

-- Restart backend → Oauth2ClientInitializer sẽ tạo lại
```

### Option 2: Update trực tiếp

```sql
UPDATE oauth2_clients 
SET redirect_uris = 'http://localhost:3000/oauth2/callback,https://production.com/callback'
WHERE client_id = 'react-client';
```

### Option 3: Sửa code và restart

```java
// Oauth2ClientInitializer.java
.redirectUris("http://localhost:3000/oauth2/callback,http://localhost:3001/callback")
.scopes("read,write,openid,admin")
```

Sau đó:
1. Xóa client trong database
2. Restart backend
3. Client mới sẽ được tạo với config mới

---

## 🎓 So sánh 3 cách tạo OAuth2 Client

### 1. SQL Script (Manual)

```sql
INSERT INTO oauth2_clients VALUES (...);
```

**Ưu điểm**: Kiểm soát hoàn toàn
**Nhược điểm**: Phải chạy thủ công, dễ quên

### 2. Oauth2ClientInitializer (Auto) ⭐ **BẠN ĐANG DÙNG**

```java
@PostConstruct
public void initializeDefaultClient() { ... }
```

**Ưu điểm**: 
- ✅ Tự động
- ✅ Không cần chạy SQL
- ✅ Perfect cho development

**Nhược điểm**: Hardcode trong code

### 3. Oauth2ClientsService (Dynamic API)

```java
POST /api/admin/oauth2-clients
Body: { clientId, clientSecret, ... }
```

**Ưu điểm**: 
- ✅ Tạo client động
- ✅ Không cần restart
- ✅ Perfect cho production

**Nhược điểm**: Cần implement API

---

## 📊 Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│  Backend Start                                              │
└─────────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────────┐
│  @PostConstruct                                             │
│  Oauth2ClientInitializer.initializeDefaultClient()         │
└─────────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────────┐
│  Check: oauth2ClientsRepository.findByClientId()            │
│  Exists? → Skip                                             │
│  Not exists? → Create                                       │
└─────────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────────┐
│  Database: oauth2_clients table                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ client_id: react-client                               │  │
│  │ client_secret: {bcrypt}$2a$10$...                     │  │
│  │ redirect_uris: http://localhost:3000/oauth2/callback  │  │
│  │ scopes: read,write,openid                             │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────────┐
│  JpaRegisteredClientRepository                              │
│  Chuyển đổi: Oauth2Clients → RegisteredClient              │
└─────────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────────┐
│  AuthorizationServerConfig                                  │
│  OAuth2 Endpoints ready:                                    │
│  - /oauth2/authorize                                        │
│  - /oauth2/token                                            │
│  - /oauth2/revoke                                           │
└─────────────────────────────────────────────────────────────┘
```

---

## ✅ Checklist

- [x] Backend có `Oauth2ClientInitializer.java`
- [x] Frontend có `OAuth2CallbackPage.jsx`
- [x] Frontend có nút OAuth2 login trong `LoginPage.jsx`
- [ ] Thêm route `/oauth2/callback` trong React Router
- [ ] Start backend → Check log "✅ OAuth2 Client initialized"
- [ ] Start frontend
- [ ] Test OAuth2 login flow
- [ ] Verify token được lưu vào localStorage
- [ ] Verify redirect đến /home

---

## 🎉 Kết luận

**Bạn KHÔNG CẦN chạy SQL!**

Backend của bạn đã có `Oauth2ClientInitializer` - nó sẽ tự động tạo client `react-client` khi app khởi động.

**Chỉ cần**:
1. Thêm route `/oauth2/callback` trong React
2. Start backend
3. Start frontend
4. Test!

That's it! 🚀
