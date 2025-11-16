# 🔐 OAuth2 vs Traditional Authentication - So sánh chi tiết

## 🤔 Câu hỏi: Khác gì nhau?

Bạn đúng là cả 2 đều có **nhập username/password**, nhưng **mục đích** và **cách sử dụng** hoàn toàn khác!

---

## 📊 So sánh tổng quan

| Tiêu chí | Traditional Auth (`/api/auth/login`) | OAuth2 (`/oauth2/authorize`) |
|----------|--------------------------------------|------------------------------|
| **Mục đích** | Đăng nhập vào **chính app này** | Cho phép **app khác** truy cập thay mặt user |
| **User nhập password cho ai?** | Cho **app của bạn** | Cho **app của bạn** (Authorization Server) |
| **Token dùng ở đâu?** | Chỉ trong **app của bạn** | Dùng cho **app bên thứ 3** |
| **Scope/Permission** | Không có (full access) | Có (giới hạn quyền: read, write...) |
| **Consent Screen** | Không có | Có (user phải approve) |
| **Use case** | User dùng trực tiếp app | App A muốn truy cập data từ App B |

---

## 🎯 Scenario 1: Traditional Authentication

### Use Case
**User muốn đăng nhập vào website của BẠN để mua hàng**

### Flow
```
User → Frontend (React) → Backend (/api/auth/login) → Return JWT
                                                      ↓
                                            User dùng JWT để call API
```

### Code Example
```javascript
// Frontend
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  body: JSON.stringify({
    email: 'user@example.com',
    password: 'password123'
  })
});

const data = await response.json();
// { accessToken: "jwt_token_here", email: "user@example.com" }

// Lưu token
localStorage.setItem('token', data.accessToken);

// Dùng token để call API
fetch('http://localhost:8080/api/products', {
  headers: {
    'Authorization': 'Bearer ' + data.accessToken
  }
});
```

### Đặc điểm
- ✅ User nhập email/password **trực tiếp vào app của bạn**
- ✅ Token chỉ dùng trong **app của bạn**
- ✅ User có **full access** (không giới hạn quyền)
- ✅ Không có consent screen
- ✅ **Đơn giản, phù hợp cho app của chính bạn**

---

## 🎯 Scenario 2: OAuth2 Authorization

### Use Case
**App bên thứ 3 muốn truy cập data của user từ website của BẠN**

**Ví dụ thực tế:**
- **Canva** muốn upload ảnh lên **Google Drive** của user
- **Spotify** muốn post bài hát lên **Facebook** của user
- **Mobile App** muốn truy cập **API của website** bạn

### Flow
```
User → Third-party App → Redirect to YOUR Authorization Server
                                    ↓
                         User login (nếu chưa login)
                                    ↓
                         Consent Screen: "Canva muốn:
                                          ☑ Đọc files
                                          ☑ Upload files
                                          Approve?"
                                    ↓
                         Generate Authorization Code
                                    ↓
                         Redirect back to Canva với code
                                    ↓
                         Canva exchange code → Access Token
                                    ↓
                         Canva dùng token để call YOUR API
```

### Code Example

**Third-party App (Canva)**:
```javascript
// Canva redirect user đến YOUR Authorization Server
window.location.href = 
  'http://your-website.com/oauth2/authorize?' +
  'response_type=code&' +
  'client_id=canva-app&' +           // ID của Canva
  'redirect_uri=https://canva.com/callback&' +
  'scope=read_files upload_files';   // Quyền Canva muốn
```

**YOUR Authorization Server**:
```java
// User chưa login → redirect to /login
// User nhập email/password vào FORM CỦA BẠN (không phải Canva!)
// → CustomAuthenticationProvider xử lý

// Sau khi login → hiện Consent Screen:
// "Canva muốn truy cập:
//  ☑ Đọc files của bạn
//  ☑ Upload files
//  Bạn có đồng ý không?"

// User approve → generate code
String code = "abc123xyz";

// Redirect về Canva
return "redirect:https://canva.com/callback?code=abc123xyz";
```

**Canva nhận code và exchange**:
```javascript
// Canva gọi YOUR token endpoint
const response = await fetch('http://your-website.com/oauth2/token', {
  method: 'POST',
  headers: {
    'Authorization': 'Basic ' + btoa('canva-app:canva-secret')
  },
  body: new URLSearchParams({
    grant_type: 'authorization_code',
    code: 'abc123xyz',
    redirect_uri: 'https://canva.com/callback'
  })
});

const data = await response.json();
// { access_token: "jwt_token", scope: "read_files upload_files" }

// Canva dùng token để call YOUR API
fetch('http://your-website.com/api/files', {
  headers: {
    'Authorization': 'Bearer ' + data.access_token
  }
});
```

### Đặc điểm
- ✅ User nhập password vào **website của BẠN** (không phải Canva!)
- ✅ Token được cấp cho **Canva** để dùng
- ✅ Token có **giới hạn quyền** (chỉ read_files, upload_files)
- ✅ Có **consent screen** (user phải approve)
- ✅ **An toàn hơn** - Canva không bao giờ thấy password của user

---

## 🔍 Điểm khác biệt quan trọng nhất

### 1. **Ai nhận được password?**

**Traditional Auth:**
```
User → Nhập password vào app của BẠN → App của BẠN xác thực
```

**OAuth2:**
```
User → Nhập password vào app của BẠN → App của BẠN xác thực
                                     ↓
                            Nhưng token được cấp cho APP KHÁC (Canva)
```

### 2. **Token có quyền gì?**

**Traditional Auth:**
```java
// Token có FULL ACCESS
@GetMapping("/api/products")
public List<Product> getProducts(Authentication auth) {
    // User có thể làm MỌI THỨ
    return productService.getAll();
}
```

**OAuth2:**
```java
// Token chỉ có quyền được approve
@GetMapping("/api/files")
@PreAuthorize("hasAuthority('SCOPE_read_files')")  // ← Kiểm tra scope!
public List<File> getFiles(Authentication auth) {
    // Chỉ được đọc files, KHÔNG được xóa, sửa
    return fileService.getAll();
}

@DeleteMapping("/api/files/{id}")
@PreAuthorize("hasAuthority('SCOPE_delete_files')")  // ← Canva KHÔNG có scope này!
public void deleteFile(@PathVariable Long id) {
    // Canva KHÔNG thể gọi endpoint này
    fileService.delete(id);
}
```

### 3. **Consent Screen**

**Traditional Auth:**
- ❌ Không có
- User login → có full access ngay

**OAuth2:**
- ✅ Có consent screen
- User phải **chủ động approve** quyền cho app bên thứ 3

```
┌─────────────────────────────────────┐
│  Canva muốn truy cập tài khoản      │
│  của bạn trên YourWebsite.com       │
│                                     │
│  Canva sẽ có thể:                   │
│  ☑ Đọc danh sách files của bạn      │
│  ☑ Upload files mới                 │
│                                     │
│  Canva KHÔNG thể:                   │
│  ☐ Xóa files của bạn                │
│  ☐ Thay đổi thông tin tài khoản     │
│                                     │
│  [Approve]  [Deny]                  │
└─────────────────────────────────────┘
```

---

## 🎬 Ví dụ thực tế dễ hiểu

### Scenario A: Traditional Auth

**Bạn muốn mua hàng trên Shopee**

1. Bạn vào Shopee.vn
2. Nhập email/password **của Shopee**
3. Login thành công
4. Bạn có thể: xem sản phẩm, mua hàng, xem đơn hàng, sửa profile...
5. **Full access** vào tài khoản Shopee của bạn

→ Đây là **Traditional Authentication** (`/api/auth/login`)

---

### Scenario B: OAuth2

**Bạn muốn dùng Canva để tạo design và lưu vào Google Drive**

1. Bạn vào Canva.com
2. Canva hỏi: "Bạn muốn lưu vào Google Drive?"
3. Canva redirect bạn đến **accounts.google.com** (không phải Canva!)
4. Bạn nhập email/password **của Google** (Canva KHÔNG thấy password!)
5. Google hiện consent screen:
   ```
   Canva muốn:
   ☑ Xem files trên Drive
   ☑ Upload files mới
   Approve?
   ```
6. Bạn approve
7. Google redirect về Canva với **authorization code**
8. Canva exchange code → **access token**
9. Canva dùng token để upload file lên **Google Drive của bạn**

**Lợi ích:**
- ✅ Canva **KHÔNG BAO GIỜ** thấy password Google của bạn
- ✅ Canva chỉ có quyền **upload files**, không thể **xóa files**
- ✅ Bạn có thể **revoke** quyền của Canva bất cứ lúc nào
- ✅ Token hết hạn sau 1 giờ (an toàn hơn)

→ Đây là **OAuth2 Authorization**

---

## 🏗️ Kiến trúc hệ thống

### Traditional Auth - Single App

```
┌─────────────────────────────────────────┐
│         Your Website (Monolith)         │
│                                         │
│  ┌──────────┐         ┌──────────┐     │
│  │ Frontend │────────▶│ Backend  │     │
│  │  React   │         │  Spring  │     │
│  └──────────┘         └──────────┘     │
│                            │            │
│                       ┌────▼────┐       │
│                       │Database │       │
│                       └─────────┘       │
└─────────────────────────────────────────┘

User login → JWT token → Dùng trong app này
```

### OAuth2 - Multiple Apps

```
┌──────────────┐                    ┌─────────────────────┐
│ Third-party  │                    │   Your Website      │
│   App        │                    │ (Authorization      │
│  (Canva)     │                    │  Server + API)      │
│              │                    │                     │
│  User click  │  1. Redirect       │  ┌──────────────┐   │
│  "Connect    │───────────────────▶│  │ /oauth2/     │   │
│   Drive"     │                    │  │  authorize   │   │
│              │                    │  └──────────────┘   │
│              │                    │         │           │
│              │                    │  User login +       │
│              │                    │  approve scopes     │
│              │                    │         │           │
│              │  2. Redirect back  │  ┌──────▼───────┐   │
│              │◀───────────────────│  │ Return code  │   │
│              │    with code       │  └──────────────┘   │
│              │                    │                     │
│  ┌────────┐  │  3. Exchange code  │  ┌──────────────┐   │
│  │Exchange│──┼───────────────────▶│  │ /oauth2/token│   │
│  │ code   │  │                    │  └──────────────┘   │
│  └────────┘  │                    │         │           │
│      │       │  4. Return token   │  ┌──────▼───────┐   │
│      │       │◀───────────────────│  │ JWT + scopes │   │
│      │       │                    │  └──────────────┘   │
│  ┌───▼────┐  │                    │                     │
│  │ Call   │  │  5. Call API       │  ┌──────────────┐   │
│  │ API    │──┼───────────────────▶│  │ /api/files   │   │
│  │with    │  │  with token        │  │ (check scope)│   │
│  │token   │  │                    │  └──────────────┘   │
│  └────────┘  │                    │                     │
└──────────────┘                    └─────────────────────┘
```

---

## 💡 Khi nào dùng cái nào?

### Dùng Traditional Auth (`/api/auth/login`) khi:

✅ User đăng nhập **trực tiếp vào app của bạn**
✅ Không có app bên thứ 3
✅ User cần **full access** vào tài khoản
✅ Đơn giản, nhanh gọn

**Ví dụ:**
- Website bán hàng của bạn
- Admin dashboard
- Internal tools
- Mobile app của chính bạn

### Dùng OAuth2 (`/oauth2/authorize`) khi:

✅ Có **app bên thứ 3** muốn truy cập API của bạn
✅ Cần **giới hạn quyền** (scopes)
✅ Muốn user **approve** trước khi cho phép
✅ Muốn **revoke** quyền dễ dàng

**Ví dụ:**
- Mobile app của đối tác muốn truy cập API của bạn
- Third-party integration (Zapier, IFTTT...)
- Public API cho developers
- Microservices architecture

---

## 🔐 Security Comparison

### Traditional Auth

```java
// AuthController.java
@PostMapping("/api/auth/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    // Validate email/password
    Authentication auth = authenticationManager.authenticate(...);
    
    // Generate JWT
    String token = jwtService.generateToken(userDetails);
    
    // Return token
    return ResponseEntity.ok(new AuthResponse(token));
}

// Token có FULL ACCESS
// Không có scope, không có consent
// User có thể làm MỌI THỨ với token này
```

### OAuth2

```java
// AuthorizationServerConfig.java
// Tự động xử lý bởi Spring Authorization Server

// 1. User login (giống traditional)
// 2. Hiện consent screen (KHÁC BIỆT!)
// 3. Generate authorization code (KHÁC BIỆT!)
// 4. Exchange code for token (KHÁC BIỆT!)
// 5. Token có SCOPES (KHÁC BIỆT!)

// Token chỉ có quyền được approve
@GetMapping("/api/files")
@PreAuthorize("hasAuthority('SCOPE_read_files')")  // ← Kiểm tra scope!
public List<File> getFiles() {
    return fileService.getAll();
}
```

---

## 🎯 Tóm tắt

| | Traditional Auth | OAuth2 |
|---|---|---|
| **User nhập password cho** | App của bạn | App của bạn (nhưng token cho app khác) |
| **Token dùng ở đâu** | Trong app của bạn | App bên thứ 3 |
| **Quyền hạn** | Full access | Giới hạn theo scopes |
| **Consent** | Không | Có |
| **Use case** | User dùng trực tiếp | App khác truy cập thay mặt user |
| **Ví dụ** | Login Shopee | Canva upload lên Google Drive |

---

## 🚀 Trong project của bạn

### Hiện tại bạn có CẢ HAI:

**1. Traditional Auth** - Cho user đăng nhập trực tiếp
```
File: AuthController.java
Endpoint: /api/auth/login
Use case: User vào website của bạn để mua hàng
```

**2. OAuth2** - Cho app bên thứ 3
```
File: AuthorizationServerConfig.java
Endpoint: /oauth2/authorize
Use case: Mobile app của đối tác muốn truy cập API của bạn
```

### Bạn nên dùng cái nào?

**Nếu chỉ có website của bạn:**
→ Dùng **Traditional Auth** (`/api/auth/login`) là đủ!

**Nếu có mobile app, third-party integrations:**
→ Cần **OAuth2** (`/oauth2/authorize`)

**Best practice:**
→ Giữ CẢ HAI! Dùng Traditional cho website, OAuth2 cho third-party apps.

---

Hy vọng giờ bạn đã hiểu rõ sự khác biệt! 🎉
