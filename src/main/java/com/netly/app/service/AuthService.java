package com.netly.app.service;

import com.netly.app.dto.AuthResponse;
import com.netly.app.dto.LoginRequest;
import com.netly.app.dto.SignupRequest;
import com.netly.app.model.User;
import com.netly.app.repository.UserRepository;
import com.netly.app.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final OtpService otpService;

    public AuthResponse signup(SignupRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        // Generate token
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token, savedUser.getId(), savedUser.getName(), savedUser.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        // Check if password is provided (password-based login)
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            // Authenticate user with password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } else {
            // For OTP login, just verify user exists
            // OTP verification happens in separate endpoint
            throw new RuntimeException("Password is required for login");
        }

        // Load user details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail());
    }

    public void requestOtp(String email) {
        // Verify user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate OTP
        String otp = otpService.generateOtp();

        // Store OTP in Redis
        otpService.storeOtp(email, otp);

        // Send OTP via email
        otpService.sendOtpEmail(email, user.getName(), otp);
    }

    public AuthResponse loginWithOtp(String email, String otp) {
        // Verify OTP
        if (!otpService.verifyOtp(email, otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // Load user details
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail());
    }
}

