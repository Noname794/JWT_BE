# ✅ TÓM TẮT NHỮNG GÌ ĐÃ SỬA

## 🎯 MỤC TIÊU
Implement Google Login với JWT Authentication theo flow:
```
Frontend → Google OAuth → ID Token → Backend Verify → JWT Token → Authenticated API Calls
```

---

## 📝 CÁC FILE ĐÃ SỬA/TẠO

### ✅ 1. **pom.xml**
**Thêm dependency:**
```xml
<dependency>
    <groupId>com.google.api-client</groupId>
    <artifactId>google-api-client</artifactId>
    <version>2.2.0</version>
</dependency>
```
**Mục đích:** Verify Google ID Token từ frontend

---

### ✅ 2. **application.properties**
**Đã có sẵn:**
```properties
# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=...
spring.security.oauth2.client.registration.google.client-secret=...

# JWT
jwt.secret=mySecretKeyForJWTTokenGenerationAndValidation12345678901234567890
jwt.expiration=86400000
```

---

### ✅ 3. **GoogleTokenRequest.java** (DTO mới)
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleTokenRequest {
    private String idToken;
}
```
**Mục đích:** Nhận Google ID Token từ frontend

---

### ✅ 4. **LoginResponse.java** (DTO mới)
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String message;
    private String email;
    private Integer customerId;
    private List<String> roles;
    private String accessToken;
    private String tokenType = "Bearer";
}
```
**Mục đích:** Response chuẩn cho login (bao gồm JWT token)

---

### ✅ 5. **AuthService.java** (Interface)
**Thêm methods:**
```java
LoginResponse verifyGg(String token);
LoginResponse refreshToken(String token);
boolean validateToken(String token);
```

---

### ✅ 6. **AuthServiceImpl.java** (Implementation) - FILE QUAN TRỌNG NHẤT

#### **Đã sửa:**

**a. Thêm imports:**
```java
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
```

**b. Thêm logger:**
```java
private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
```

**c. Implement `verifyGg()` - VERIFY GOOGLE ID TOKEN:**
```java
@Override
public LoginResponse verifyGg(String token) {
    try {
        // 1. Verify Google ID Token với Google API
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
        
        GoogleIdToken idToken = verifier.verify(token);
        
        if (idToken == null) {
            throw new RuntimeException("Invalid Google ID Token");
        }

        // 2. Extract email, name từ token
        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String providerId = payload.getSubject();
        String provider = "GOOGLE";
        
        logger.info("Google token verified - Email: {}", email);

        // 3. Tìm hoặc tạo customer trong DB
        Customers customer = findOrCreateCustomer(email, name, provider, providerId);

        // 4. Tạo JWT token
        UserDetails userDetails = User.builder()
                .username(customer.getEmail())
                .password("")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + customer.getRole().toUpperCase())))
                .build();

        String jwtToken = jwtService.generateToken(userDetails);

        // 5. Return JWT + user info
        return LoginResponse.builder()
                .message("Google login successful")
                .email(customer.getEmail())
                .customerId(customer.getId())
                .roles(List.of("ROLE_" + customer.getRole().toUpperCase()))
                .accessToken(jwtToken)
                .tokenType("Bearer")
                .build();

    } catch (Exception e) {
        logger.error("Error verifying Google token", e);
        throw new RuntimeException("Failed to verify Google token: " + e.getMessage());
    }
}
```

**d. Implement `refreshToken()`:**
```java
@Override
public LoginResponse refreshToken(String token) {
    try {
        String email = jwtService.extractUsername(token);
        Customers customer = customersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        UserDetails userDetails = User.builder()
                .username(customer.getEmail())
                .password("")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + customer.getRole().toUpperCase())))
                .build();

        if (jwtService.validateToken(token, userDetails)) {
            String newToken = jwtService.generateToken(userDetails);

            return LoginResponse.builder()
                    .message("Token refreshed")
                    .email(customer.getEmail())
                    .customerId(customer.getId())
                    .roles(List.of("ROLE_" + customer.getRole().toUpperCase()))
                    .accessToken(newToken)
                    .tokenType("Bearer")
                    .build();
        } else {
            throw new RuntimeException("Invalid token");
        }
    } catch (Exception e) {
        throw new RuntimeException("Failed to refresh token: " + e.getMessage());
    }
}
```

**e. Implement `validateToken()`:**
```java
@Override
public boolean validateToken(String token) {
    try {
        String email = jwtService.extractUsername(token);
        Customers customer = customersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        UserDetails userDetails = User.builder()
                .username(customer.getEmail())
                .password("")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + customer.getRole().toUpperCase())))
                .build();

        return jwtService.validateToken(token, userDetails);
    } catch (Exception e) {
        return false;
    }
}
```

**f. Thêm method `findOrCreateCustomer()` - TÌM HOẶC TẠO USER:**
```java
private Customers findOrCreateCustomer(String email, String name, String provider, String providerId) {
    Optional<Customers> existingCustomer = customersRepository.findByEmail(email);

    if (existingCustomer.isPresent()) {
        Customers customer = existingCustomer.get();
        
        // Update OAuth info nếu chưa có
        if (customer.getOauthProvider() == null || customer.getOauthProvider().equals("LOCAL")) {
            customer.setOauthProvider(provider);
            customer.setOauthProviderId(providerId);
            customersRepository.save(customer);
            logger.info("Updated existing customer with OAuth provider: {}", provider);
        }
        
        return customer;
    }

    // Tạo customer mới
    Customers newCustomer = new Customers();
    newCustomer.setEmail(email);

    String[] nameParts = name != null ? name.split(" ", 2) : new String[]{"", ""};
    newCustomer.setFirstName(nameParts.length > 0 ? nameParts[0] : "");
    newCustomer.setLastName(nameParts.length > 1 ? nameParts[1] : "");

    newCustomer.setPassword(""); // OAuth users không cần password
    newCustomer.setRole("USER"); // Mặc định role USER
    newCustomer.setOauthProvider(provider);
    newCustomer.setOauthProviderId(providerId);

    Customers savedCustomer = customersRepository.save(newCustomer);
    logger.info("Created new customer from OAuth - Provider: {}, Email: {}", provider, email);

    return savedCustomer;
}
```

---

### ✅ 7. **AuthController.java**

**Đã thêm endpoints:**

```java
@Autowired
private AuthService authService;

// Verify Google ID Token
@PostMapping("/google/verify")
public ResponseEntity<LoginResponse> verifyGg(@RequestBody GoogleTokenRequest googleTokenRequest) {
    LoginResponse loginResponse = authService.verifyGg(googleTokenRequest.getIdToken());
    return ResponseEntity.ok(loginResponse);
}

// Refresh JWT Token
@PostMapping("/refresh")
public ResponseEntity<LoginResponse> refreshToken(@RequestHeader("Authorization") String token) {
    String jwt = token.substring(7); // Remove "Bearer "
    LoginResponse response = authService.refreshToken(jwt);
    return ResponseEntity.ok(response);
}

// Validate JWT Token
@GetMapping("/validate")
public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String token) {
    String jwt = token.substring(7);
    boolean isValid = authService.validateToken(jwt);
    return ResponseEntity.ok(isValid);
}
```

**Đã xóa import lỗi:**
```java
// Xóa: import org.springframework.security.core.parameters.P;
```

---

### ✅ 8. **CustomersRepository.java**

**Đã xóa method không hợp lệ:**
```java
// Xóa: Customers findOrCreateCustomer(String email, String name, String provider, String providerId);
```
**Lý do:** JpaRepository interface không thể có method implementation. Logic này đã được chuyển vào `AuthServiceImpl.findOrCreateCustomer()`

---

## 🔄 FLOW HOẠT ĐỘNG

### **Flow 1: Google Login qua Backend Redirect (Flow cũ - VẪN HOẠT ĐỘNG)**
```
1. User click link: http://localhost:8080/oauth2/authorization/google
2. Google OAuth popup
3. User login Google
4. Google redirect về: /login/oauth2/code/google
5. OAuth2LoginSuccessHandler xử lý
6. Tạo JWT token
7. Redirect về frontend: http://localhost:5173/oauth2/redirect?token=...
```

### **Flow 2: Google Login qua Frontend API (Flow mới - ĐÃ IMPLEMENT)**
```
1. Frontend: User click "Login with Google"
2. Frontend: Google OAuth popup (sử dụng @react-oauth/google)
3. Frontend: Nhận Google ID Token
4. Frontend: POST /api/auth/google/verify { idToken: "..." }
5. Backend: Verify ID Token với Google API
6. Backend: Extract email từ token
7. Backend: Check email trong DB
   - Có: Lấy user info + role
   - Không: Tạo user mới với role USER
8. Backend: Generate JWT token
9. Backend: Return { accessToken, email, customerId, roles }
10. Frontend: Lưu JWT vào localStorage
11. Frontend: Call API với header: Authorization: Bearer <JWT>
12. Backend: JwtAuthenticationFilter verify JWT
13. Backend: Cấp quyền theo role
```

---

## 🔐 API ENDPOINTS

### **POST /api/auth/google/verify**
Verify Google ID Token và trả về JWT

**Request:**
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE4MmU0..."
}
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

### **POST /api/auth/refresh**
Refresh JWT token

**Headers:**
```
Authorization: Bearer <old_jwt_token>
```

**Response:**
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

### **GET /api/auth/validate**
Validate JWT token

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response:**
```json
true
```

---

## ⚠️ LƯU Ý QUAN TRỌNG

### 1. **Không conflict với OAuth2 flow cũ**
- Flow cũ (OAuth2LoginSuccessHandler) vẫn hoạt động bình thường
- Flow mới (API /google/verify) là option bổ sung
- Cả 2 flow đều generate JWT token giống nhau
- Bạn có thể dùng 1 trong 2 hoặc cả 2

### 2. **Database**
- Table `customers` cần có columns:
  - `oauth_provider` (VARCHAR)
  - `oauth_provider_id` (VARCHAR)
- Nếu chưa có, cần chạy migration:
```sql
ALTER TABLE customers 
ADD COLUMN oauth_provider VARCHAR(50) DEFAULT 'LOCAL',
ADD COLUMN oauth_provider_id VARCHAR(255);
```

### 3. **Java Version Issue**
- Project yêu cầu Java 17 (trong pom.xml: `<java.version>17</java.version>`)
- Hiện tại đang dùng Java 8
- **Cần cài Java 17 để build được project**
- Download: https://adoptium.net/temurin/releases/?version=17

### 4. **Security**
- Google Client ID đã được config trong application.properties
- JWT secret key nên thay đổi trong production
- JWT expiration: 24 hours (86400000 ms)

---

## 🚀 CÁCH SỬ DỤNG

### **Backend:**
1. Cài Java 17
2. Build project: `mvn clean install`
3. Run: `mvn spring-boot:run`
4. Server chạy tại: http://localhost:8080

### **Frontend:**
Xem file `GOOGLE_LOGIN_GUIDE.md` để biết cách implement frontend

---

## ✅ CHECKLIST

- [x] Thêm Google API Client dependency
- [x] Tạo GoogleTokenRequest DTO
- [x] Tạo LoginResponse DTO
- [x] Implement verifyGg() method
- [x] Implement refreshToken() method
- [x] Implement validateToken() method
- [x] Implement findOrCreateCustomer() method
- [x] Thêm API endpoints trong AuthController
- [x] Xóa import lỗi
- [x] Xóa method không hợp lệ trong Repository
- [x] Kiểm tra conflict với OAuth2 flow cũ
- [ ] Cài Java 17 (cần làm)
- [ ] Test API endpoints (sau khi fix Java version)
- [ ] Implement Frontend (xem GOOGLE_LOGIN_GUIDE.md)

---

## 🐛 LỖI ĐÃ SỬA

### **1. Collections.singletonList() thiếu parameter**
❌ Trước: `.setAudience(Collections.singletonList())`
✅ Sau: `.setAudience(Collections.singletonList(clientId))`

### **2. payload.get("name") không cast**
❌ Trước: `String name = payload.get("name");`
✅ Sau: `String name = (String) payload.get("name");`

### **3. Thiếu import List**
❌ Trước: Không có import
✅ Sau: `import java.util.List;`

### **4. Method findOrCreateCustomer() không tồn tại**
❌ Trước: Gọi method không có
✅ Sau: Implement method private trong AuthServiceImpl

### **5. verifyGg() không return đúng**
❌ Trước: `return null;`
✅ Sau: Return LoginResponse với đầy đủ thông tin

### **6. refreshToken() và validateToken() chưa implement**
❌ Trước: `return null;` và `return false;`
✅ Sau: Implement đầy đủ logic

### **7. CustomersRepository có method không hợp lệ**
❌ Trước: `Customers findOrCreateCustomer(...);` trong interface
✅ Sau: Xóa method này, chuyển logic vào Service

### **8. Import lỗi trong AuthController**
❌ Trước: `import org.springframework.security.core.parameters.P;`
✅ Sau: Xóa import không sử dụng

---

## 📚 TÀI LIỆU THAM KHẢO

- **GOOGLE_LOGIN_GUIDE.md**: Hướng dẫn chi tiết implement frontend
- **Google OAuth2 Docs**: https://developers.google.com/identity/protocols/oauth2
- **JWT.io**: https://jwt.io/
- **Spring Security OAuth2**: https://spring.io/guides/tutorials/spring-boot-oauth2/

---

## 💡 KẾT LUẬN

✅ **Backend đã hoàn thiện:**
- Google ID Token verification
- JWT generation & validation
- User authentication & authorization
- Role-based access control
- Auto create user nếu chưa tồn tại

⚠️ **Cần làm tiếp:**
1. Cài Java 17
2. Build & test backend
3. Implement frontend theo hướng dẫn
4. Test end-to-end flow

🎯 **Flow hoàn chỉnh:** Google Login → ID Token → Backend Verify → JWT Token → Authenticated API Calls
