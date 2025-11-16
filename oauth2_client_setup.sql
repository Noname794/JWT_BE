-- ============================================
-- OAuth2 Client Setup Script
-- ============================================

-- 1. Tạo bảng oauth2_clients (nếu chưa có)
CREATE TABLE IF NOT EXISTS oauth2_clients (
    client_id VARCHAR(255) PRIMARY KEY,
    client_secret VARCHAR(255) NOT NULL,
    client_name VARCHAR(255) NOT NULL,
    redirect_uris TEXT,
    scopes TEXT,
    authorization_grant_types TEXT,
    client_authentication_methods TEXT,
    access_token_validity_seconds INT DEFAULT 3600,
    refresh_token_validity_seconds INT DEFAULT 86400,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Insert React Web Client
-- Note: Thay thế {bcrypt}$2a$10$... bằng BCrypt hash thực tế của 'react-secret'
-- Tạo hash tại: https://bcrypt-generator.com/ (rounds: 10)
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
    '{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- BCrypt của 'react-secret'
    'React Web Application',
    'http://localhost:3000/oauth2/callback',
    'read,write,openid',
    'authorization_code,refresh_token',
    'client_secret_basic,client_secret_post',
    3600,      -- Access token: 1 hour
    2592000    -- Refresh token: 30 days
)
ON DUPLICATE KEY UPDATE
    client_secret = VALUES(client_secret),
    client_name = VALUES(client_name),
    redirect_uris = VALUES(redirect_uris),
    scopes = VALUES(scopes),
    authorization_grant_types = VALUES(authorization_grant_types),
    client_authentication_methods = VALUES(client_authentication_methods),
    access_token_validity_seconds = VALUES(access_token_validity_seconds),
    refresh_token_validity_seconds = VALUES(refresh_token_validity_seconds),
    updated_at = CURRENT_TIMESTAMP;

-- 3. (Optional) Insert Mobile App Client
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
    'mobile-app',
    '{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- BCrypt của 'mobile-secret'
    'Mobile Application',
    'myapp://oauth2/callback',
    'read,write,openid',
    'authorization_code,refresh_token',
    'client_secret_post',
    3600,
    2592000
)
ON DUPLICATE KEY UPDATE
    client_secret = VALUES(client_secret),
    client_name = VALUES(client_name),
    redirect_uris = VALUES(redirect_uris),
    scopes = VALUES(scopes),
    authorization_grant_types = VALUES(authorization_grant_types),
    client_authentication_methods = VALUES(client_authentication_methods),
    access_token_validity_seconds = VALUES(access_token_validity_seconds),
    refresh_token_validity_seconds = VALUES(refresh_token_validity_seconds),
    updated_at = CURRENT_TIMESTAMP;

-- 4. (Optional) Insert Postman Testing Client
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
    'postman-client',
    '{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- BCrypt của 'postman-secret'
    'Postman Testing Client',
    'https://oauth.pstmn.io/v1/callback',
    'read,write',
    'authorization_code,refresh_token',
    'client_secret_basic',
    3600,
    86400
)
ON DUPLICATE KEY UPDATE
    client_secret = VALUES(client_secret),
    client_name = VALUES(client_name),
    redirect_uris = VALUES(redirect_uris),
    scopes = VALUES(scopes),
    authorization_grant_types = VALUES(authorization_grant_types),
    client_authentication_methods = VALUES(client_authentication_methods),
    access_token_validity_seconds = VALUES(access_token_validity_seconds),
    refresh_token_validity_seconds = VALUES(refresh_token_validity_seconds),
    updated_at = CURRENT_TIMESTAMP;

-- 5. Verify inserted clients
SELECT 
    client_id,
    client_name,
    redirect_uris,
    scopes,
    authorization_grant_types,
    access_token_validity_seconds,
    refresh_token_validity_seconds,
    created_at
FROM oauth2_clients
ORDER BY created_at DESC;

-- ============================================
-- Notes:
-- ============================================
-- 1. BCrypt Hash của 'react-secret':
--    $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
--    (Tạo tại: https://bcrypt-generator.com/)
--
-- 2. Redirect URIs:
--    - Development: http://localhost:3000/oauth2/callback
--    - Production: https://yourdomain.com/oauth2/callback
--
-- 3. Scopes:
--    - read: Đọc dữ liệu
--    - write: Ghi dữ liệu
--    - openid: OpenID Connect
--
-- 4. Grant Types:
--    - authorization_code: OAuth2 Authorization Code Flow
--    - refresh_token: Refresh access token
--
-- 5. Client Authentication Methods:
--    - client_secret_basic: Basic Auth (Authorization header)
--    - client_secret_post: POST body parameters
-- ============================================
