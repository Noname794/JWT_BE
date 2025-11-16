# 🔐 OAuth2 Authorization Server - Hướng dẫn chi tiết

## 📋 Tổng quan

Project của bạn đã implement một **OAuth2 Authorization Server** hoàn chỉnh sử dụng **Spring Authorization Server** (oauth2-authorization-server).

### Mục đích:
- Cung cấp OAuth2/OpenID Connect authentication
- Quản lý OAuth2 clients (applications)
- Issue và validate JWT tokens
- Hỗ trợ multiple grant types

## 📁 Cấu trúc Files

### 1. Config Files

#### `AuthorizationServerConfig.java` ⭐⭐⭐⭐⭐
**Vai trò**: File cấu hình chính của Authorization Server

**Các Bean quan trọng**:

```java
@Bean
@Order(1) // Ưu tiên cao nhất
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
```
- Cấu hình security cho OAuth2 endpoints
- Enable OIDC (OpenID Connect)
- Redirect về /login nếu chưa authenticate

```java
@Bean
public JWKSource<SecurityContext> jwkSource()
```
- Tạo RSA key pair (public/private keys)
- Dùng để sign và verify JWT tokens
- Key size: 2048 bits

```java
@Bean
public AuthorizationServerSettings authorizationServerSettings()
```
- Định nghĩa các OAuth2 endpoints:
  - `/oauth2/authorize` - Request authorization code
  - `/oauth2/token` - Exchange code for token
  - `/oauth2/jwks` - Public keys
  - `/oauth2/revoke` - Revoke tokens
  - `/oauth2/introspect` - Validate tokens
  - `/userinfo` - User information (OIDC)



### 2. Service Files

#### `JpaRegisteredClientRepository.java` ⭐⭐⭐⭐⭐
**Vai trò**: Quản lý OAuth2 clients trong database

**Implements**: `RegisteredClientRepository` (Spring Security interface)

**Chức năng chính**:
1. **save(RegisteredClient)** - Lưu client vào DB
2. **findById(String)** - Tìm client theo ID
3. **findByClientId(String)** - Tìm client theo client_id

**Mapping**:
- `toEntity()`: RegisteredClient → Oauth2Clients (Entity)
- `toRegisteredClient()`: Oauth2Clients → RegisteredClient

**Lưu ý**: 
- Lưu trữ persistent trong MySQL thay vì in-memory
- Convert giữa Spring Security format và Entity format

#### `Oauth2ClientInitializer.java` ⭐⭐⭐
**Vai trò**: Khởi tạo default OAuth2 client khi app start

**Annotation**: `@PostConstruct` - Chạy sau khi bean được tạo

**Default Client**:
```
Client ID: my-client-app
Client Secret: my-client-secret (encrypted)
Redirect URIs: http://localhost:3000/callback
Scopes: read, write, openid, profile, email
Grant Types: authorization_code, refresh_token, client_credentials
```



### 3. Entity & Repository

#### `Oauth2Clients.java` (Entity)
**Table**: `oauth2_clients`

**Columns**:
- `client_id` (PK) - Unique identifier
- `client_secret` - Encrypted secret
- `client_name` - Display name
- `redirect_uris` - Comma-separated URIs
- `scopes` - Comma-separated scopes
- `authorization_grant_types` - Supported grant types
- `client_authentication_methods` - Auth methods
- `access_token_validity_seconds` - Token TTL
- `refresh_token_validity_seconds` - Refresh token TTL
- `created_at`, `updated_at` - Timestamps

#### `Oauth2ClientsRepository.java`
```java
Optional<Oauth2Clients> findByClientId(String clientId);
```

### 4. Controller

#### `OAuth2TestController.java` ⭐⭐⭐
**Base Path**: `/api/oauth2`

**Endpoints**:
1. `GET /test-token` - Test JWT token
2. `GET /userinfo` - Get user info (OIDC)
3. `GET /health` - Health check

#### `Oauth2ClientsController.java`
**Base Path**: `/api/oauth2/clients`

**CRUD Operations**:
- `GET /` - List all clients
- `GET /{clientId}` - Get client by ID
- `POST /` - Create new client
- `PUT /{clientId}` - Update client
- `DELETE /{clientId}` - Delete client



## 🔄 Luồng hoạt động OAuth2 Authorization Code Flow

### Bước 1: Client Request Authorization
```
User → Client App → Authorization Server
```

**Request**:
```
GET http://localhost:8080/oauth2/authorize?
    response_type=code&
    client_id=my-client-app&
    redirect_uri=http://localhost:3000/callback&
    scope=read write openid&
    state=xyz123
```

**Xử lý**:
1. `AuthorizationServerConfig` nhận request
2. Check user đã login chưa
3. Nếu chưa → redirect `/login`
4. Nếu rồi → hiện consent screen

### Bước 2: User Login & Consent
```
User → Login Form → Authorization Server
```

**Xử lý**:
1. User nhập email/password
2. `CustomAuthenticationProvider` validate
3. User approve scopes (consent screen)
4. Authorization Server tạo authorization code

### Bước 3: Redirect với Authorization Code
```
Authorization Server → Client App
```

**Response**:
```
HTTP/1.1 302 Found
Location: http://localhost:3000/callback?
    code=AUTHORIZATION_CODE&
    state=xyz123
```

### Bước 4: Exchange Code for Token
```
Client App → Authorization Server
```

**Request**:
```
POST http://localhost:8080/oauth2/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic base64(client_id:client_secret)

grant_type=authorization_code&
code=AUTHORIZATION_CODE&
redirect_uri=http://localhost:3000/callback
```

**Xử lý**:
1. Validate authorization code
2. Validate client credentials
3. Generate JWT access token
4. Generate refresh token



**Response**:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "scope": "read write openid"
}
```

### Bước 5: Use Access Token
```
Client App → Resource Server (API)
```

**Request**:
```
GET http://localhost:8080/api/products
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Xử lý**:
1. `JwtAuthenticationFilter` extract token
2. `JwtDecoder` verify signature
3. Check expiration
4. Extract user info & roles
5. Allow/Deny request

### Bước 6: Refresh Token (Optional)
```
Client App → Authorization Server
```

**Request**:
```
POST http://localhost:8080/oauth2/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic base64(client_id:client_secret)

grant_type=refresh_token&
refresh_token=REFRESH_TOKEN
```

**Response**: New access token + refresh token



## 🎯 Các Grant Types được hỗ trợ

### 1. Authorization Code Flow ⭐⭐⭐⭐⭐
**Use case**: Web applications, Mobile apps

**Flow**:
```
User → Client → Auth Server (login) → Client (code) → Auth Server (token)
```

**Security**: Cao nhất (code + client secret)

### 2. Refresh Token ⭐⭐⭐⭐
**Use case**: Renew access token without re-login

**Flow**:
```
Client → Auth Server (refresh_token) → New access token
```

### 3. Client Credentials ⭐⭐⭐
**Use case**: Server-to-server communication

**Flow**:
```
Service A → Auth Server (client_id + secret) → Access token
```

**No user involved**: Machine-to-machine

## 🔑 JWT Token Structure

### Access Token
```json
{
  "sub": "user@example.com",
  "iss": "http://localhost:8080",
  "aud": "my-client-app",
  "exp": 1699999999,
  "iat": 1699996399,
  "scope": ["read", "write"],
  "roles": ["USER", "ADMIN"]
}
```

### Signed with RSA-256
- Private key: Sign token
- Public key: Verify token (available at `/oauth2/jwks`)



## 🧪 Testing OAuth2 Server

### 1. Test với Postman

#### Step 1: Get Authorization Code
```
GET http://localhost:8080/oauth2/authorize?
    response_type=code&
    client_id=my-client-app&
    redirect_uri=http://localhost:3000/callback&
    scope=read write openid&
    state=xyz123
```

→ Login in browser → Copy authorization code from redirect URL

#### Step 2: Exchange Code for Token
```
POST http://localhost:8080/oauth2/token
Authorization: Basic bXktY2xpZW50LWFwcDpteS1jbGllbnQtc2VjcmV0
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&
code=YOUR_CODE&
redirect_uri=http://localhost:3000/callback
```

#### Step 3: Test Access Token
```
GET http://localhost:8080/api/oauth2/test-token
Authorization: Bearer YOUR_ACCESS_TOKEN
```

### 2. Test với cURL

```bash
# Get token using client credentials
curl -X POST http://localhost:8080/oauth2/token \
  -u my-client-app:my-client-secret \
  -d "grant_type=client_credentials&scope=read write"

# Use token
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_TOKEN"
```



## 📊 Database Schema

### Table: oauth2_clients
```sql
CREATE TABLE oauth2_clients (
    client_id VARCHAR(100) PRIMARY KEY,
    client_secret VARCHAR(255) NOT NULL,
    client_name VARCHAR(255) NOT NULL,
    redirect_uris VARCHAR(1000),
    scopes VARCHAR(500),
    authorization_grant_types VARCHAR(500),
    client_authentication_methods VARCHAR(500),
    access_token_validity_seconds INT,
    refresh_token_validity_seconds INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Sample Data
```sql
INSERT INTO oauth2_clients VALUES (
    'my-client-app',
    '{bcrypt}$2a$10$...', -- encrypted
    'My Client Application',
    'http://localhost:3000/callback',
    'read,write,openid,profile,email',
    'authorization_code,refresh_token,client_credentials',
    'client_secret_basic,client_secret_post',
    3600,
    86400,
    NOW(),
    NOW()
);
```

## 🔒 Security Best Practices

### 1. Client Secret
- ✅ Always encrypt (BCrypt)
- ✅ Never expose in frontend
- ✅ Rotate regularly

### 2. Redirect URIs
- ✅ Whitelist exact URIs
- ❌ No wildcards in production
- ✅ Use HTTPS in production

### 3. Tokens
- ✅ Short-lived access tokens (1 hour)
- ✅ Longer refresh tokens (24 hours)
- ✅ Implement token revocation
- ✅ Use HTTPS only

### 4. Scopes
- ✅ Principle of least privilege
- ✅ User consent required
- ✅ Validate scopes on each request



## 🎨 Sequence Diagram

```
┌──────┐         ┌────────┐         ┌──────────────┐         ┌──────────┐
│ User │         │ Client │         │ Auth Server  │         │   API    │
└──┬───┘         └───┬────┘         └──────┬───────┘         └────┬─────┘
   │                 │                     │                      │
   │  1. Click Login │                     │                      │
   ├────────────────>│                     │                      │
   │                 │                     │                      │
   │                 │ 2. Redirect to Auth │                      │
   │                 ├────────────────────>│                      │
   │                 │   /oauth2/authorize │                      │
   │                 │                     │                      │
   │                 │  3. Show Login Form │                      │
   │                 │<────────────────────┤                      │
   │                 │                     │                      │
   │  4. Enter Credentials                 │                      │
   ├──────────────────────────────────────>│                      │
   │                 │                     │                      │
   │                 │  5. Show Consent    │                      │
   │<──────────────────────────────────────┤                      │
   │                 │                     │                      │
   │  6. Approve     │                     │                      │
   ├──────────────────────────────────────>│                      │
   │                 │                     │                      │
   │                 │ 7. Redirect + Code  │                      │
   │                 │<────────────────────┤                      │
   │                 │                     │                      │
   │                 │ 8. Exchange Code    │                      │
   │                 ├────────────────────>│                      │
   │                 │   /oauth2/token     │                      │
   │                 │                     │                      │
   │                 │ 9. Access Token     │                      │
   │                 │<────────────────────┤                      │
   │                 │                     │                      │
   │                 │ 10. API Request + Token                    │
   │                 ├───────────────────────────────────────────>│
   │                 │                     │                      │
   │                 │                     │ 11. Verify Token     │
   │                 │                     │<─────────────────────┤
   │                 │                     │                      │
   │                 │                     │ 12. Token Valid      │
   │                 │                     ├─────────────────────>│
   │                 │                     │                      │
   │                 │ 13. API Response                           │
   │                 │<───────────────────────────────────────────┤
   │                 │                     │                      │
```



## 🚀 Production Deployment

### 1. Environment Variables
```properties
# application-prod.properties
spring.security.oauth2.authorizationserver.issuer=https://auth.yourdomain.com
spring.security.oauth2.authorizationserver.jwk-set-uri=https://auth.yourdomain.com/oauth2/jwks
```

### 2. HTTPS Required
```java
@Bean
public AuthorizationServerSettings authorizationServerSettings() {
    return AuthorizationServerSettings.builder()
            .issuer("https://auth.yourdomain.com") // HTTPS!
            .build();
}
```

### 3. Database Migration
- Use Flyway/Liquibase
- Backup before migration
- Test rollback procedure

### 4. Monitoring
- Log all token requests
- Monitor failed authentications
- Alert on suspicious activity
- Track token usage

## 📚 Key Concepts

### OAuth2 vs OpenID Connect (OIDC)
- **OAuth2**: Authorization framework (access delegation)
- **OIDC**: Authentication layer on top of OAuth2
- **OIDC adds**: ID Token, UserInfo endpoint, standard claims

### Scopes
- `read`: Read access
- `write`: Write access
- `openid`: Enable OIDC
- `profile`: Access to profile info
- `email`: Access to email

### Token Types
- **Access Token**: Short-lived, for API access
- **Refresh Token**: Long-lived, to get new access token
- **ID Token**: User identity info (OIDC only)



## 🔧 Troubleshooting

### Issue 1: "Invalid client"
**Cause**: Client not found in database
**Solution**: 
```sql
SELECT * FROM oauth2_clients WHERE client_id = 'my-client-app';
```
Check if client exists and credentials match.

### Issue 2: "Invalid redirect_uri"
**Cause**: Redirect URI not whitelisted
**Solution**: Add URI to client's redirect_uris:
```sql
UPDATE oauth2_clients 
SET redirect_uris = 'http://localhost:3000/callback,http://localhost:3000/authorized'
WHERE client_id = 'my-client-app';
```

### Issue 3: "Token expired"
**Cause**: Access token TTL exceeded
**Solution**: Use refresh token to get new access token

### Issue 4: "Invalid signature"
**Cause**: JWT signed with different key
**Solution**: 
- Check JWK endpoint: `GET /oauth2/jwks`
- Verify issuer matches
- Restart server if keys rotated

## 📖 References

### Official Documentation
- Spring Authorization Server: https://spring.io/projects/spring-authorization-server
- OAuth 2.0 RFC: https://tools.ietf.org/html/rfc6749
- OpenID Connect: https://openid.net/connect/

### Useful Tools
- JWT Debugger: https://jwt.io
- OAuth2 Playground: https://www.oauth.com/playground/
- Postman OAuth2 Helper

---

**Version**: 1.0.0  
**Last Updated**: 2025-11-08  
**Author**: Backend Team
