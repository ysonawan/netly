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
     * Sends portfolio reports to all users on the scheduled cron expression.
     * */
    @Scheduled(cron = "${report.cron.expression}", zone = "UTC")
    public void sendPortfolioReportsToAllUsers() {
        log.info("Starting scheduled portfolio report generation for all users...");

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

            log.info("Scheduled report generation completed. Success: {}, Failed: {}", successCount, failureCount);
        } catch (Exception e) {
            log.error("Error during scheduled report generation", e);
        }
    }

}

