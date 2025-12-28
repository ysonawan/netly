package com.netly.app.service;

import com.netly.app.model.User;
import com.netly.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklySnapshotScheduler {

    private final PortfolioSnapshotService portfolioSnapshotService;
    private final UserRepository userRepository;

    /**
     * Run every Monday at 12:00 PM UTC
     * Cron format: second, minute, hour, day of month, month, day of week
     * "0 0 12 ? * MON" means: at 12:00:00 on Monday
     */
    @Scheduled(cron = "0 0 12 ? * MON", zone = "UTC")
    public void createWeeklySnapshots() {
        log.info("Starting weekly portfolio snapshot creation...");
        LocalDate snapshotDate = LocalDate.now();

        List<User> allUsers = userRepository.findAll();
        int successCount = 0;
        int errorCount = 0;

        for (User user : allUsers) {
            try {
                portfolioSnapshotService.createSnapshotForUser(user.getId(), snapshotDate);
                successCount++;
                log.info("Created snapshot for user: {} ({})", user.getName(), user.getEmail());
            } catch (Exception e) {
                errorCount++;
                log.error("Failed to create snapshot for user: {} ({}). Error: {}",
                        user.getName(), user.getEmail(), e.getMessage());
            }
        }

        log.info("Weekly snapshot creation completed. Success: {}, Errors: {}, Total: {}",
                successCount, errorCount, allUsers.size());
    }
}

