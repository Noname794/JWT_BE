# 🔐 OAuth2 Flow - Giải thích chi tiết từng bước

## 🎯 Bước 1: User Click Login

### 📍 Vị trí: FRONTEND (React App)

**File**: `src/components/Auth/LoginPage.jsx` (hoặc bất kỳ component nào)

**Code**:
```javascript
const handleOAuth2Login = () => {
  // Tạo OAuth2 authorization URL
  const authUrl = 
    'http://localhost:8080/oauth2/authorize?' +
    'response_type=code&' +              // Yêu cầu authorization code
    'client_id=my-client-app&' +         // ID của client app
    'redirect_uri=http://localhost:3000/callback&' + // Nơi nhận code
    'scope=read write openid&' +         // Quyền yêu cầu
    'state=xyz123';                      // Random string để bảo mật
  
  // Redirect browser đến Authorization Server
  window.location.href = authUrl;
};
```

**Nhiệm vụ**:
- ✅ User nhấn button "Login with OAuth2"
- ✅ Frontend tạo authorization URL với các parameters
- ✅ Browser redirect đến Authorization Server

**Parameters giải thích**:
- `response_type=code`: Yêu cầu authorization code (không phải token trực tiếp)
- `client_id`: ID của app đang request (đã register trước)
- `redirect_uri`: URL để nhận authorization code
- `scope`: Quyền mà app muốn (read, write, openid...)
- `state`: Random string để prevent CSRF attack

---

## 🎯 Bước 2: Redirect to /oauth2/authorize

### 📍 Vị trí: BACKEND

**File 1**: `AuthorizationServerConfig.java`

**Code xử lý**:
```java
@Bean
@Order(1) // Ưu tiên cao nhất - chạy trước SecurityConfig
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) {
    
    OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
            new OAuth2AuthorizationServerConfigurer();
    
    http
        // Chỉ apply cho OAuth2 endpoints
        .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
        
        // Enable OAuth2 Authorization Server
        .with(authorizationServerConfigurer, (authorizationServer) ->
            authorizationServer.oidc(Customizer.withDefaults())
        )
        
        // Yêu cầu authentication cho tất cả requests
        .authorizeHttpRequests((authorize) ->
            authorize.anyRequest().authenticated()
        )
        
        // Nếu chưa login → redirect đến /login
        .exceptionHandling((exceptions) -> exceptions
            .defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint("/login"),
                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
            )
        );
    
    return http.build();
}
```

**File 2**: `AuthorizationServerSettings` (trong cùng file)

```java
@Bean
public AuthorizationServerSettings authorizationServerSettings() {
    return AuthorizationServerSettings.builder()
        .issuer("http://localhost:8080")
        .authorizationEndpoint("/oauth2/authorize")  // ← Endpoint này!
        .tokenEndpoint("/oauth2/token")
        .jwkSetEndpoint("/oauth2/jwks")
        .build();
}
```

**Nhiệm vụ**:
- ✅ Nhận request từ browser
- ✅ Parse parameters (client_id, redirect_uri, scope, state)
- ✅ Validate client_id có tồn tại không
- ✅ Validate redirect_uri có match với client config không
- ✅ Check user đã login chưa
- ✅ Nếu CHƯA → redirect đến `/login` (Bước 3)
- ✅ Nếu RỒI → hiện consent screen (Bước 4)

**Validation sử dụng**:
**File**: `JpaRegisteredClientRepository.java`

```java
@Override
public RegisteredClient findByClientId(String clientId) {
    // Tìm client trong database
    return oauth2ClientsRepository.findByClientId(clientId)
            .map(this::toRegisteredClient)
            .orElse(null);
}
```



---

## 🎯 Bước 3: Login Form (if not authenticated)

### 📍 Vị trí: BACKEND

**File 1**: `SecurityConfig.java`

**Code redirect đến login**:
```java
// AuthorizationServerConfig.java - Line 48-52
.exceptionHandling((exceptions) -> exceptions
    .defaultAuthenticationEntryPointFor(
        new LoginUrlAuthenticationEntryPoint("/login"), // ← Redirect đến đây!
        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
    )
)
```

**Nhiệm vụ**:
- ✅ User chưa authenticated
- ✅ Spring Security redirect browser đến `/login`
- ✅ Hiển thị login form (HTML form hoặc React component)

**Login Form HTML** (Spring Security default):
```html
<form action="/login" method="POST">
    <input type="text" name="username" placeholder="Email" />
    <input type="password" name="password" placeholder="Password" />
    <button type="submit">Login</button>
</form>
```

**File 2**: `CustomAuthenticationProvider.java`

**Code xử lý login**:
```java
@Override
public Authentication authenticate(Authentication authentication) 
        throws AuthenticationException {
    
    // Lấy email và password từ form
    final String email = authentication.getName();
    final String password = authentication.getCredentials().toString();

    // Tìm user trong database
    Optional<Customers> customers = customersRepository.findByEmail(email);

    // Kiểm tra user có tồn tại không
    if(customers.isEmpty()){
        throw new BadCredentialsException("Email not found");
    }

    Customers customers1 = customers.get();

    // Kiểm tra password có đúng không
    if(!passwordEncoder.matches(password, customers1.getPassword())){
        throw new BadCredentialsException("Password not matched");
    }

    // Tạo Authentication token với roles
    return authenticateAndGetToken(customers1);
}

private UsernamePasswordAuthenticationToken authenticateAndGetToken(
        Customers customers) {
    
    // Tạo list authorities (roles)
    final List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

    String role = customers.getRole();
    if(role == null || role.isEmpty()){
        role = "USER";
    }

    // Add role với prefix ROLE_
    grantedAuthorities.add(
        new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
    );

    // Tạo UserDetails object
    final UserDetails principal = new User(
        customers.getEmail(),
        customers.getPassword(),
        grantedAuthorities
    );

    // Return authenticated token
    return new UsernamePasswordAuthenticationToken(
        principal, 
        customers.getPassword(), 
        grantedAuthorities
    );
}
```

**Nhiệm vụ**:
- ✅ Nhận email + password từ form
- ✅ Tìm user trong database (`CustomersRepository`)
- ✅ So sánh password (BCrypt)
- ✅ Nếu đúng → tạo Authentication object với roles
- ✅ Nếu sai → throw BadCredentialsException
- ✅ Spring Security lưu authentication vào SecurityContext

**Database Query**:
```java
// CustomersRepository.java
Optional<Customers> findByEmail(String email);
```



---

## 🎯 Bước 4: Consent Screen (approve scopes)

### 📍 Vị trí: BACKEND (Spring Authorization Server tự động)

**File**: `JpaRegisteredClientRepository.java`

**Code cấu hình consent**:
```java
private RegisteredClient toRegisteredClient(Oauth2Clients client) {
    RegisteredClient.Builder builder = RegisteredClient.withId(client.getClientId())
        .clientId(client.getClientId())
        .clientSecret(client.getClientSecret())
        .clientName(client.getClientName());
    
    // ... other configs ...
    
    // Client settings - REQUIRE consent screen
    builder.clientSettings(ClientSettings.builder()
        .requireAuthorizationConsent(true)  // ← Bật consent screen!
        .build());
    
    return builder.build();
}
```

**Consent Screen hiển thị**:
```
┌─────────────────────────────────────┐
│  My Client Application              │
│  wants to access your account       │
│                                     │
│  ☑ Read your profile                │
│  ☑ Write data                       │
│  ☑ Access your email                │
│                                     │
│  [Approve]  [Deny]                  │
└─────────────────────────────────────┘
```

**Nhiệm vụ**:
- ✅ User đã login thành công
- ✅ Spring Authorization Server hiện consent screen
- ✅ Hiển thị scopes mà client app yêu cầu
- ✅ User approve hoặc deny
- ✅ Nếu approve → tạo authorization code
- ✅ Nếu deny → redirect với error

**Scopes được request**:
```java
// Từ URL parameter: scope=read write openid
// Được parse và hiển thị cho user
```



---

## 🎯 Bước 5: Generate Authorization Code

### 📍 Vị trí: BACKEND (Spring Authorization Server)

**File**: Spring Security tự động xử lý (internal)

**Flow xử lý**:
```java
// Sau khi user approve consent:

// 1. Tạo OAuth2Authorization object
OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
    .principalName(principal.getName())  // Email của user
    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
    .authorizedScopes(approvedScopes)    // Scopes user đã approve
    .build();

// 2. Generate random authorization code
String authorizationCode = generateRandomCode(); // Ví dụ: "abc123xyz"

// 3. Lưu vào database
OAuth2Authorization savedAuth = authorization
    .token(authorizationCode)
    .expiresAt(Instant.now().plusSeconds(300))  // Hết hạn sau 5 phút
    .build();

authorizationService.save(savedAuth);
```

**Database Table**: `oauth2_authorization`

| Column | Value |
|--------|-------|
| id | UUID |
| registered_client_id | "my-client-app" |
| principal_name | "user@example.com" |
| authorization_grant_type | "authorization_code" |
| authorized_scopes | "read,write,openid" |
| state | "xyz123" |
| authorization_code_value | "abc123xyz" (encrypted) |
| authorization_code_issued_at | 2025-11-14 10:00:00 |
| authorization_code_expires_at | 2025-11-14 10:05:00 |

**Nhiệm vụ**:
- ✅ User đã approve scopes
- ✅ Generate random authorization code
- ✅ Lưu code vào database với expiry time (5 phút)
- ✅ Link code với user + client + scopes
- ✅ Redirect browser về client app với code

**Redirect URL**:
```
http://localhost:3000/callback?
  code=abc123xyz&
  state=xyz123
```

---

## 🎯 Bước 6: Client Receives Authorization Code

### 📍 Vị trí: FRONTEND (React App)

**File**: `src/components/Auth/CallbackPage.jsx`

**Code xử lý**:
```javascript
import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

function CallbackPage() {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    // Parse URL parameters
    const params = new URLSearchParams(location.search);
    const code = params.get('code');
    const state = params.get('state');
    const error = params.get('error');

    // Kiểm tra có error không
    if (error) {
      console.error('OAuth2 Error:', error);
      navigate('/login?error=' + error);
      return;
    }

    // Kiểm tra state để prevent CSRF
    const savedState = sessionStorage.getItem('oauth2_state');
    if (state !== savedState) {
      console.error('State mismatch - possible CSRF attack!');
      navigate('/login?error=invalid_state');
      return;
    }

    // Có authorization code → exchange for token
    if (code) {
      exchangeCodeForToken(code);
    }
  }, [location, navigate]);

  return <div>Processing login...</div>;
}
```

**Nhiệm vụ**:
- ✅ Browser redirect về `/callback?code=abc123xyz&state=xyz123`
- ✅ React component parse URL parameters
- ✅ Validate state parameter (CSRF protection)
- ✅ Extract authorization code
- ✅ Chuẩn bị exchange code for access token

---

## 🎯 Bước 7: Exchange Code for Access Token

### 📍 Vị trí: FRONTEND → BACKEND

**File Frontend**: `src/services/authService.js`

**Code**:
```javascript
async function exchangeCodeForToken(authorizationCode) {
  try {
    // Gọi backend endpoint để exchange code
    const response = await fetch('http://localhost:8080/oauth2/token', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        // Basic Auth: base64(client_id:client_secret)
        'Authorization': 'Basic ' + btoa('my-client-app:secret123')
      },
      body: new URLSearchParams({
        grant_type: 'authorization_code',
        code: authorizationCode,
        redirect_uri: 'http://localhost:3000/callback',
        client_id: 'my-client-app'
      })
    });

    const data = await response.json();
    
    if (response.ok) {
      // Lưu tokens
      localStorage.setItem('access_token', data.access_token);
      localStorage.setItem('refresh_token', data.refresh_token);
      localStorage.setItem('token_expires_at', 
        Date.now() + (data.expires_in * 1000)
      );
      
      // Redirect đến home page
      window.location.href = '/';
    } else {
      console.error('Token exchange failed:', data);
    }
  } catch (error) {
    console.error('Error exchanging code:', error);
  }
}
```

**File Backend**: `AuthorizationServerConfig.java`

**Endpoint xử lý**: `/oauth2/token`

```java
@Bean
public AuthorizationServerSettings authorizationServerSettings() {
    return AuthorizationServerSettings.builder()
        .issuer("http://localhost:8080")
        .authorizationEndpoint("/oauth2/authorize")
        .tokenEndpoint("/oauth2/token")  // ← Endpoint này xử lý!
        .jwkSetEndpoint("/oauth2/jwks")
        .build();
}
```

**Flow xử lý trong Spring**:

```java
// 1. Validate client credentials
RegisteredClient client = clientRepository.findByClientId(clientId);
if (!passwordEncoder.matches(clientSecret, client.getClientSecret())) {
    throw new OAuth2AuthenticationException("Invalid client");
}

// 2. Tìm authorization code trong database
OAuth2Authorization authorization = 
    authorizationService.findByToken(code, OAuth2TokenType.AUTHORIZATION_CODE);

if (authorization == null) {
    throw new OAuth2AuthenticationException("Invalid code");
}

// 3. Kiểm tra code đã hết hạn chưa
if (authorization.getAuthorizationCode().isExpired()) {
    throw new OAuth2AuthenticationException("Code expired");
}

// 4. Kiểm tra code đã được sử dụng chưa
if (authorization.getAuthorizationCode().isInvalidated()) {
    throw new OAuth2AuthenticationException("Code already used");
}

// 5. Validate redirect_uri
if (!redirectUri.equals(authorization.getRedirectUri())) {
    throw new OAuth2AuthenticationException("Redirect URI mismatch");
}

// 6. Generate Access Token (JWT)
JwtClaimsSet accessTokenClaims = JwtClaimsSet.builder()
    .issuer("http://localhost:8080")
    .subject(authorization.getPrincipalName())  // user email
    .audience(List.of(clientId))
    .issuedAt(Instant.now())
    .expiresAt(Instant.now().plusSeconds(3600))  // 1 hour
    .claim("scope", authorization.getAuthorizedScopes())
    .claim("roles", getUserRoles(authorization.getPrincipalName()))
    .build();

String accessToken = jwtEncoder.encode(accessTokenClaims).getTokenValue();

// 7. Generate Refresh Token
String refreshToken = generateRandomToken();
OAuth2RefreshToken refreshTokenObj = new OAuth2RefreshToken(
    refreshToken,
    Instant.now(),
    Instant.now().plusDays(30)  // 30 days
);

// 8. Invalidate authorization code (chỉ dùng 1 lần)
authorization = OAuth2Authorization.from(authorization)
    .token(authorization.getAuthorizationCode(), 
           (metadata) -> metadata.put("invalidated", true))
    .accessToken(new OAuth2AccessToken(
        OAuth2AccessToken.TokenType.BEARER,
        accessToken,
        Instant.now(),
        Instant.now().plusSeconds(3600)
    ))
    .refreshToken(refreshTokenObj)
    .build();

authorizationService.save(authorization);

// 9. Return token response
return OAuth2AccessTokenResponse.builder()
    .accessToken(accessToken)
    .tokenType("Bearer")
    .expiresIn(3600)
    .refreshToken(refreshToken)
    .scopes(authorization.getAuthorizedScopes())
    .build();
```

**Response JSON**:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "refresh_abc123xyz",
  "scope": "read write openid"
}
```

**Nhiệm vụ**:
- ✅ Frontend gửi POST request với authorization code
- ✅ Backend validate client credentials
- ✅ Backend validate authorization code
- ✅ Backend generate JWT access token
- ✅ Backend generate refresh token
- ✅ Backend invalidate authorization code (dùng 1 lần)
- ✅ Return tokens cho frontend
- ✅ Frontend lưu tokens vào localStorage

---

## 🎯 Bước 8: Use Access Token to Call APIs

### 📍 Vị trí: FRONTEND → BACKEND APIs

**File Frontend**: `src/services/apiService.js`

**Code**:
```javascript
// Interceptor tự động thêm token vào mọi request
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api'
});

// Request interceptor - thêm token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor - xử lý token expired
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    // Nếu 401 và chưa retry
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      // Thử refresh token
      const newToken = await refreshAccessToken();
      if (newToken) {
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return apiClient(originalRequest);
      }
    }
    
    return Promise.reject(error);
  }
);

// Example API calls
export const getProducts = () => apiClient.get('/products');
export const getUserProfile = () => apiClient.get('/users/me');
export const createOrder = (data) => apiClient.post('/orders', data);
```

**File Backend**: `SecurityConfig.java`

**Code validate token**:
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // Enable OAuth2 Resource Server
        .oauth2ResourceServer((oauth2) -> oauth2
            .jwt(Customizer.withDefaults())  // Validate JWT tokens
        )
        
        // Configure authorization rules
        .authorizeHttpRequests((authorize) -> authorize
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/user/**").hasRole("USER")
            .anyRequest().authenticated()
        );
    
    return http.build();
}

@Bean
public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
    return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
}
```

**Flow validate token**:

```java
// 1. Extract token from Authorization header
String token = request.getHeader("Authorization").substring(7); // Remove "Bearer "

// 2. Decode và validate JWT
Jwt jwt = jwtDecoder.decode(token);

// 3. Kiểm tra signature (dùng public key)
// 4. Kiểm tra expiry time
if (jwt.getExpiresAt().isBefore(Instant.now())) {
    throw new JwtException("Token expired");
}

// 5. Kiểm tra issuer
if (!jwt.getIssuer().equals("http://localhost:8080")) {
    throw new JwtException("Invalid issuer");
}

// 6. Extract claims
String email = jwt.getSubject();
List<String> scopes = jwt.getClaimAsStringList("scope");
List<String> roles = jwt.getClaimAsStringList("roles");

// 7. Tạo Authentication object
Authentication auth = new JwtAuthenticationToken(jwt, authorities);
SecurityContextHolder.getContext().setAuthentication(auth);

// 8. Cho phép request tiếp tục
filterChain.doFilter(request, response);
```

**Example API Controller**:
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping
    public List<Product> getProducts(Authentication authentication) {
        // Token đã được validate bởi Spring Security
        String userEmail = authentication.getName();
        
        // Có thể access user info từ token
        Jwt jwt = (Jwt) authentication.getPrincipal();
        List<String> scopes = jwt.getClaimAsStringList("scope");
        
        return productService.getProducts();
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_write')")  // Cần scope 'write'
    public Product createProduct(@RequestBody Product product) {
        return productService.create(product);
    }
}
```

**Nhiệm vụ**:
- ✅ Frontend thêm `Authorization: Bearer <token>` vào header
- ✅ Backend nhận request
- ✅ Spring Security validate JWT token
- ✅ Check signature, expiry, issuer
- ✅ Extract user info và scopes từ token
- ✅ Check authorization (roles, scopes)
- ✅ Nếu OK → xử lý request
- ✅ Nếu invalid → return 401 Unauthorized

---

## 🎯 Bước 9: Refresh Access Token (when expired)

### 📍 Vị trí: FRONTEND → BACKEND

**File Frontend**: `src/services/authService.js`

**Code**:
```javascript
async function refreshAccessToken() {
  try {
    const refreshToken = localStorage.getItem('refresh_token');
    
    if (!refreshToken) {
      // Không có refresh token → redirect to login
      window.location.href = '/login';
      return null;
    }

    const response = await fetch('http://localhost:8080/oauth2/token', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': 'Basic ' + btoa('my-client-app:secret123')
      },
      body: new URLSearchParams({
        grant_type: 'refresh_token',  // ← Khác với authorization_code
        refresh_token: refreshToken,
        client_id: 'my-client-app'
      })
    });

    const data = await response.json();
    
    if (response.ok) {
      // Lưu access token mới
      localStorage.setItem('access_token', data.access_token);
      localStorage.setItem('token_expires_at', 
        Date.now() + (data.expires_in * 1000)
      );
      
      // Có thể có refresh token mới (rotation)
      if (data.refresh_token) {
        localStorage.setItem('refresh_token', data.refresh_token);
      }
      
      return data.access_token;
    } else {
      // Refresh token invalid → logout
      localStorage.clear();
      window.location.href = '/login';
      return null;
    }
  } catch (error) {
    console.error('Error refreshing token:', error);
    return null;
  }
}

// Auto refresh trước khi token hết hạn
function setupAutoRefresh() {
  setInterval(() => {
    const expiresAt = localStorage.getItem('token_expires_at');
    const now = Date.now();
    
    // Refresh 5 phút trước khi hết hạn
    if (expiresAt && (expiresAt - now) < 5 * 60 * 1000) {
      refreshAccessToken();
    }
  }, 60000); // Check mỗi phút
}
```

**File Backend**: Xử lý trong `/oauth2/token` endpoint

**Flow xử lý**:
```java
// 1. Validate client credentials (giống bước 7)

// 2. Tìm refresh token trong database
OAuth2Authorization authorization = 
    authorizationService.findByToken(refreshToken, OAuth2TokenType.REFRESH_TOKEN);

if (authorization == null) {
    throw new OAuth2AuthenticationException("Invalid refresh token");
}

// 3. Kiểm tra refresh token đã hết hạn chưa
if (authorization.getRefreshToken().isExpired()) {
    throw new OAuth2AuthenticationException("Refresh token expired");
}

// 4. Generate access token mới
JwtClaimsSet newAccessTokenClaims = JwtClaimsSet.builder()
    .issuer("http://localhost:8080")
    .subject(authorization.getPrincipalName())
    .audience(List.of(clientId))
    .issuedAt(Instant.now())
    .expiresAt(Instant.now().plusSeconds(3600))
    .claim("scope", authorization.getAuthorizedScopes())
    .claim("roles", getUserRoles(authorization.getPrincipalName()))
    .build();

String newAccessToken = jwtEncoder.encode(newAccessTokenClaims).getTokenValue();

// 5. (Optional) Refresh Token Rotation - tạo refresh token mới
String newRefreshToken = generateRandomToken();
OAuth2RefreshToken newRefreshTokenObj = new OAuth2RefreshToken(
    newRefreshToken,
    Instant.now(),
    Instant.now().plusDays(30)
);

// 6. Invalidate refresh token cũ (nếu dùng rotation)
authorization = OAuth2Authorization.from(authorization)
    .accessToken(new OAuth2AccessToken(
        OAuth2AccessToken.TokenType.BEARER,
        newAccessToken,
        Instant.now(),
        Instant.now().plusSeconds(3600)
    ))
    .refreshToken(newRefreshTokenObj)
    .build();

authorizationService.save(authorization);

// 7. Return new tokens
return OAuth2AccessTokenResponse.builder()
    .accessToken(newAccessToken)
    .tokenType("Bearer")
    .expiresIn(3600)
    .refreshToken(newRefreshToken)  // Token mới (nếu rotation)
    .scopes(authorization.getAuthorizedScopes())
    .build();
```

**Nhiệm vụ**:
- ✅ Access token hết hạn (401 error)
- ✅ Frontend gửi refresh token
- ✅ Backend validate refresh token
- ✅ Backend generate access token mới
- ✅ (Optional) Generate refresh token mới (rotation)
- ✅ Return tokens mới
- ✅ Frontend lưu và retry request

---

## 🎯 Bước 10: Logout

### 📍 Vị trí: FRONTEND + BACKEND

**File Frontend**: `src/components/Auth/LogoutButton.jsx`

**Code**:
```javascript
async function handleLogout() {
  try {
    const token = localStorage.getItem('access_token');
    
    // 1. Gọi backend để revoke tokens
    await fetch('http://localhost:8080/oauth2/revoke', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': 'Basic ' + btoa('my-client-app:secret123')
      },
      body: new URLSearchParams({
        token: token,
        token_type_hint: 'access_token'
      })
    });
    
    // 2. Clear local storage
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('token_expires_at');
    
    // 3. Redirect to login
    window.location.href = '/login';
    
  } catch (error) {
    console.error('Logout error:', error);
    // Vẫn clear local storage dù có lỗi
    localStorage.clear();
    window.location.href = '/login';
  }
}
```

**File Backend**: `AuthorizationServerConfig.java`

**Endpoint**: `/oauth2/revoke`

```java
@Bean
public AuthorizationServerSettings authorizationServerSettings() {
    return AuthorizationServerSettings.builder()
        .issuer("http://localhost:8080")
        .authorizationEndpoint("/oauth2/authorize")
        .tokenEndpoint("/oauth2/token")
        .tokenRevocationEndpoint("/oauth2/revoke")  // ← Endpoint này!
        .jwkSetEndpoint("/oauth2/jwks")
        .build();
}
```

**Flow xử lý**:
```java
// 1. Validate client credentials

// 2. Tìm token trong database
OAuth2Authorization authorization = 
    authorizationService.findByToken(token, null);  // Tìm cả access và refresh

if (authorization == null) {
    // Token không tồn tại → OK (idempotent)
    return ResponseEntity.ok().build();
}

// 3. Invalidate tất cả tokens của authorization này
authorization = OAuth2Authorization.from(authorization)
    .token(authorization.getAccessToken(), 
           (metadata) -> metadata.put("invalidated", true))
    .token(authorization.getRefreshToken(), 
           (metadata) -> metadata.put("invalidated", true))
    .build();

authorizationService.save(authorization);

// 4. (Optional) Xóa session
SecurityContextHolder.clearContext();

// 5. Return success
return ResponseEntity.ok().build();
```

**Nhiệm vụ**:
- ✅ User click logout
- ✅ Frontend gọi revoke endpoint
- ✅ Backend invalidate access token
- ✅ Backend invalidate refresh token
- ✅ Frontend clear localStorage
- ✅ Redirect to login page

---

## 📊 Complete Flow Diagram

```
┌─────────────┐                                    ┌──────────────────┐
│   Browser   │                                    │  Authorization   │
│  (Frontend) │                                    │     Server       │
└──────┬──────┘                                    └────────┬─────────┘
       │                                                    │
       │ 1. Click Login                                     │
       │────────────────────────────────────────────────────>
       │    GET /oauth2/authorize?                          │
       │    response_type=code&client_id=...                │
       │                                                    │
       │                                          2. Check authenticated?
       │                                                    │
       │ 3. Redirect to /login                              │
       │<────────────────────────────────────────────────────
       │                                                    │
       │ 4. POST /login (email + password)                  │
       │────────────────────────────────────────────────────>
       │                                                    │
       │                                          5. Validate credentials
       │                                             (CustomAuthenticationProvider)
       │                                                    │
       │ 6. Show Consent Screen                             │
       │<────────────────────────────────────────────────────
       │    "App wants to access: read, write"              │
       │                                                    │
       │ 7. User approves                                   │
       │────────────────────────────────────────────────────>
       │                                                    │
       │                                          8. Generate authorization code
       │                                             Save to database
       │                                                    │
       │ 9. Redirect to callback                            │
       │<────────────────────────────────────────────────────
       │    http://localhost:3000/callback?code=abc123      │
       │                                                    │
       │ 10. POST /oauth2/token                             │
       │────────────────────────────────────────────────────>
       │     grant_type=authorization_code&code=abc123      │
       │                                                    │
       │                                          11. Validate code
       │                                              Generate JWT access token
       │                                              Generate refresh token
       │                                              Invalidate code
       │                                                    │
       │ 12. Return tokens                                  │
       │<────────────────────────────────────────────────────
       │     { access_token, refresh_token, expires_in }    │
       │                                                    │
       │ 13. Save tokens to localStorage                    │
       │                                                    │
       │                                                    │
┌──────┴──────┐                                    ┌────────┴─────────┐
│   Browser   │                                    │   Resource       │
│  (Frontend) │                                    │   Server (API)   │
└──────┬──────┘                                    └────────┬─────────┘
       │                                                    │
       │ 14. GET /api/products                              │
       │────────────────────────────────────────────────────>
       │     Authorization: Bearer <access_token>           │
       │                                                    │
       │                                          15. Validate JWT
       │                                              Check signature
       │                                              Check expiry
       │                                              Extract user info
       │                                                    │
       │ 16. Return data                                    │
       │<────────────────────────────────────────────────────
       │     { products: [...] }                            │
       │                                                    │
       │                                                    │
       │ (Token expired after 1 hour)                       │
       │                                                    │
       │ 17. GET /api/orders                                │
       │────────────────────────────────────────────────────>
       │     Authorization: Bearer <expired_token>          │
       │                                                    │
       │ 18. 401 Unauthorized                               │
       │<────────────────────────────────────────────────────
       │                                                    │
       │ 19. POST /oauth2/token                             │
       │────────────────────────────────────────────────────>
       │     grant_type=refresh_token&                      │
       │     refresh_token=<refresh_token>                  │
       │                                                    │
       │                                          20. Validate refresh token
       │                                              Generate new access token
       │                                                    │
       │ 21. Return new access token                        │
       │<────────────────────────────────────────────────────
       │     { access_token, expires_in }                   │
       │                                                    │
       │ 22. Retry GET /api/orders                          │
       │────────────────────────────────────────────────────>
       │     Authorization: Bearer <new_access_token>       │
       │                                                    │
       │ 23. Return data                                    │
       │<────────────────────────────────────────────────────
       │                                                    │
```

---

## 🔑 Key Files Summary

| File | Nhiệm vụ |
|------|----------|
| **AuthorizationServerConfig.java** | Cấu hình OAuth2 Authorization Server, định nghĩa endpoints |
| **SecurityConfig.java** | Cấu hình security rules, login form |
| **CustomAuthenticationProvider.java** | Xử lý login (validate email/password) |
| **JpaRegisteredClientRepository.java** | Quản lý OAuth2 clients (client_id, secret, scopes) |
| **OAuth2LoginSuccessHandler.java** | Xử lý sau khi login thành công |
| **CallbackPage.jsx** | Nhận authorization code từ redirect |
| **authService.js** | Exchange code for token, refresh token |
| **apiService.js** | Gọi APIs với access token, auto refresh |

---

## 🎓 Tổng kết

**OAuth2 Authorization Code Flow** gồm 10 bước chính:

1. ✅ User click login → redirect to `/oauth2/authorize`
2. ✅ Check authenticated → nếu chưa thì show login form
3. ✅ User login → validate credentials
4. ✅ Show consent screen → user approve scopes
5. ✅ Generate authorization code → lưu database
6. ✅ Redirect về frontend với code
7. ✅ Exchange code for access token + refresh token
8. ✅ Use access token để call APIs
9. ✅ Refresh access token khi hết hạn
10. ✅ Logout → revoke tokens

**Security Features**:
- ✅ Authorization code chỉ dùng 1 lần
- ✅ Code hết hạn sau 5 phút
- ✅ Access token hết hạn sau 1 giờ
- ✅ Refresh token hết hạn sau 30 ngày
- ✅ JWT signature validation
- ✅ State parameter để prevent CSRF
- ✅ Client authentication (client_id + client_secret)
- ✅ Refresh token rotation (optional)

**Database Tables**:
- `oauth2_clients` - Lưu registered clients
- `oauth2_authorization` - Lưu authorization codes, tokens
- `customers` - Lưu user accounts

Đã hoàn thành! 🎉
