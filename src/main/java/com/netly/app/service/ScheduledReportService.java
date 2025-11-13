package com.netly.app.service;

import com.netly.app.model.User;
import com.netly.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledReportService {

    private final UserRepository userRepository;
    private final ReportingService reportingService;

    /**
     * Sends portfolio reports to all users on the 1st day of every month at 1:00 AM UTC
     * Cron expression: "0 0 1 1 * ?" means:
     * - 0 seconds
     * - 0 minutes
     * - 1 hours (1 AM UTC)
     * - 1st day of month
     * - every month
     * - any day of week
     */
    @Scheduled(cron = "0 0 1 1 * ?", zone = "UTC")
    public void sendMonthlyReportsToAllUsers() {
        log.info("Starting scheduled monthly portfolio report generation for all users...");

        try {
            // Get all users from database
            List<User> allUsers = userRepository.findAll();
            log.info("Found {} users to send reports to", allUsers.size());

            int successCount = 0;
            int failureCount = 0;

            // Send report to each user
            for (User user : allUsers) {
                try {
                    log.info("Sending portfolio report to user: {} ({})", user.getName(), user.getEmail());
                    reportingService.sendReportForUser(user);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to send report to user: {} ({})", user.getName(), user.getEmail(), e);
                    failureCount++;
                }
            }

            log.info("Scheduled monthly report generation completed. Success: {}, Failed: {}", successCount, failureCount);
        } catch (Exception e) {
            log.error("Error during scheduled monthly report generation", e);
        }
    }

}

