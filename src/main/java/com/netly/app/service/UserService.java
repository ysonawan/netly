package com.netly.app.service;

import com.netly.app.dto.UpdateSecondaryEmailsRequest;
import com.netly.app.dto.UserProfileDTO;
import com.netly.app.model.User;
import com.netly.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return convertToDTO(user);
    }

    @Transactional
    public UserProfileDTO updateSecondaryEmails(Long userId, UpdateSecondaryEmailsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate that secondary emails don't exceed 10
        if (request.getSecondaryEmails() != null && request.getSecondaryEmails().size() > 10) {
            throw new RuntimeException("Maximum 10 secondary emails allowed");
        }

        // Validate that secondary emails don't include the primary email
        if (request.getSecondaryEmails() != null) {
            for (String email : request.getSecondaryEmails()) {
                if (email != null && email.trim().equalsIgnoreCase(user.getEmail())) {
                    throw new RuntimeException("Secondary email cannot be the same as primary email");
                }
            }
        }

        // Convert list to comma-separated string
        String secondaryEmailsStr = null;
        if (request.getSecondaryEmails() != null && !request.getSecondaryEmails().isEmpty()) {
            // Filter out empty strings and trim
            List<String> validEmails = request.getSecondaryEmails().stream()
                    .filter(email -> email != null && !email.trim().isEmpty())
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());

            if (!validEmails.isEmpty()) {
                secondaryEmailsStr = String.join(",", validEmails);
            }
        }

        user.setSecondaryEmails(secondaryEmailsStr);
        User savedUser = userRepository.save(user);

        return convertToDTO(savedUser);
    }

    private UserProfileDTO convertToDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());

        // Convert comma-separated string to list
        List<String> secondaryEmails = new ArrayList<>();
        if (user.getSecondaryEmails() != null && !user.getSecondaryEmails().trim().isEmpty()) {
            secondaryEmails = Arrays.stream(user.getSecondaryEmails().split(","))
                    .map(String::trim)
                    .filter(email -> !email.isEmpty())
                    .collect(Collectors.toList());
        }
        dto.setSecondaryEmails(secondaryEmails);

        return dto;
    }
}

