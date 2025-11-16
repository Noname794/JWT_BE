package com.websiteElectronics.websiteElectronics.Controllers;

import com.websiteElectronics.websiteElectronics.Dtos.*;
import com.websiteElectronics.websiteElectronics.Entities.Customers;
import com.websiteElectronics.websiteElectronics.Repositories.CustomersRepository;
import com.websiteElectronics.websiteElectronics.Services.AuthService;
import com.websiteElectronics.websiteElectronics.Services.CustomersService;
import com.websiteElectronics.websiteElectronics.Services.EmailVerificationService;
import com.websiteElectronics.websiteElectronics.Services.Impl.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//
//@CrossOrigin("*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomersRepository customersRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomersService customersService;

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String accessToken = jwtService.generateToken(userDetails);

            String email = userDetails.getUsername();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            CustomersDto customer = customersService.getCustomerByEmail(email);
            Integer customerId = customer.getId();

            AuthResponse response = new AuthResponse(
                    "successful",
                    email,
                    roles,
                    accessToken,
                    customerId
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email hoặc mật khẩu không chính xác");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @PostMapping("/google/verify")
    public ResponseEntity<LoginResponse> verifyGg(@RequestBody GoogleTokenRequest googleTokenRequest) {
        LoginResponse loginResponse = authService.verifyGg(googleTokenRequest.getIdToken());
        return ResponseEntity.ok(loginResponse);
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {

            if (customersRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Email đã được sử dụng"));
            }

            emailVerificationService.sendOtpForRegistration(registerRequest);

            return ResponseEntity.ok(Map.of(
                    "message", "Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra email và xác thực.",
                    "email", registerRequest.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi gửi email: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest) {
        boolean isVerified = emailVerificationService.verifyOtpAndRegister(
                verifyOtpRequest.getEmail(),
                verifyOtpRequest.getOtp()
        );

        if (isVerified) {
            return ResponseEntity.ok(Map.of(
                    "message", "Xác thực thành công! Tài khoản của bạn đã được tạo."
            ));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Mã OTP không hợp lệ hoặc đã hết hạn"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        LoginResponse response = authService.refreshToken(jwt);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        boolean isValid = authService.validateToken(jwt);
        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            return ResponseEntity.ok(new AuthResponse(
                    "Thông tin user hiện tại",
                    userDetails.getUsername(),
                    userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList()),
                    null,
                    null
            ));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
    }
    
    // ==================== FORGOT PASSWORD ====================
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            emailVerificationService.sendOtpForPasswordReset(request.getEmail());
            
            return ResponseEntity.ok(Map.of(
                    "message", "Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra email.",
                    "email", request.getEmail()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Email không tồn tại trong hệ thống"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi gửi email: " + e.getMessage()));
        }
    }
    
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<?> verifyResetOtp(@Valid @RequestBody VerifyResetOtpRequest request) {
        boolean isValid = emailVerificationService.verifyResetOtp(
                request.getEmail(),
                request.getOtp()
        );
        
        if (isValid) {
            return ResponseEntity.ok(Map.of(
                    "message", "Mã OTP hợp lệ. Bạn có thể đặt lại mật khẩu.",
                    "valid", true
            ));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "message", "Mã OTP không hợp lệ hoặc đã hết hạn",
                            "valid", false
                    ));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        boolean isReset = emailVerificationService.resetPassword(
                request.getEmail(),
                request.getOtp(),
                request.getNewPassword()
        );
        
        if (isReset) {
            return ResponseEntity.ok(Map.of(
                    "message", "Mật khẩu đã được đặt lại thành công. Bạn có thể đăng nhập với mật khẩu mới."
            ));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Không thể đặt lại mật khẩu. Mã OTP không hợp lệ hoặc đã hết hạn."));
        }
    }
}
