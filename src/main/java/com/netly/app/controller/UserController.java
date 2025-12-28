package com.netly.app.controller;

import com.netly.app.dto.RequestOtpForEmailChangeRequest;
import com.netly.app.dto.UpdateBasicInfoRequest;
import com.netly.app.dto.UpdateBasicInfoWithOtpRequest;
import com.netly.app.dto.UpdateSecondaryEmailsRequest;
import com.netly.app.dto.UpdateSecondaryEmailsWithOtpRequest;
import com.netly.app.dto.UserProfileDTO;
import com.netly.app.service.ReportingService;
import com.netly.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ReportingService reportingService;
    private final com.netly.app.repository.UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        com.netly.app.model.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileDTO profile = userService.getUserProfile(user.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile/secondary-emails")
    public ResponseEntity<UserProfileDTO> updateSecondaryEmails(
            @Valid @RequestBody UpdateSecondaryEmailsRequest request,
            Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        com.netly.app.model.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileDTO updatedProfile = userService.updateSecondaryEmails(user.getId(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/profile/basic")
    public ResponseEntity<UserProfileDTO> updateBasicInfo(
            @RequestBody UpdateBasicInfoRequest request,
            Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        com.netly.app.model.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserProfileDTO updatedProfile = userService.updateBasicInfo(user.getId(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/profile/request-otp-for-primary-email")
    public ResponseEntity<String> requestOtpForPrimaryEmailChange(
            @Valid @RequestBody RequestOtpForEmailChangeRequest request,
            Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        com.netly.app.model.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userService.requestOtpForEmailChange(user.getId(), request);
        return ResponseEntity.ok("OTP has been sent to your new email address. Please verify it to update your primary email.");
    }

    @PostMapping("/profile/request-otp-for-secondary-emails")
    public ResponseEntity<String> requestOtpForSecondaryEmailChange(
            @RequestBody Map<String, List<String>> request,
            Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        com.netly.app.model.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> secondaryEmails = request.getOrDefault("secondaryEmails", new ArrayList<>());
        userService.requestOtpForSecondaryEmailChange(user.getId(), secondaryEmails);
        return ResponseEntity.ok("OTP has been sent to your newly added email address for verification.");
    }

    @PutMapping("/profile/basic-with-otp")
    public ResponseEntity<UserProfileDTO> updateBasicInfoWithOtp(
            @Valid @RequestBody UpdateBasicInfoWithOtpRequest request,
            Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        com.netly.app.model.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserProfileDTO updatedProfile = userService.updateBasicInfoWithOtp(user.getId(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/profile/secondary-emails-with-otp")
    public ResponseEntity<UserProfileDTO> updateSecondaryEmailsWithOtp(
            @Valid @RequestBody UpdateSecondaryEmailsWithOtpRequest request,
            Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        com.netly.app.model.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileDTO updatedProfile = userService.updateSecondaryEmailsWithOtp(user.getId(), request);
        return ResponseEntity.ok(updatedProfile);
    }


    @PostMapping("/profile/send-report")
    public ResponseEntity<String> sendPortfolioReport(Authentication authentication) {
        try {
            reportingService.sendReport();
            return ResponseEntity.ok("Portfolio report queued successfully. It will be sent to all your email addresses.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send portfolio report");
        }
    }
}

