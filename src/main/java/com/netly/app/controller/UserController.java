package com.netly.app.controller;

import com.netly.app.dto.UpdateBasicInfoRequest;
import com.netly.app.dto.UpdateSecondaryEmailsRequest;
import com.netly.app.dto.UserProfileDTO;
import com.netly.app.service.ReportingService;
import com.netly.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

