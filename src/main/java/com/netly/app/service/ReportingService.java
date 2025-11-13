package com.netly.app.service;

import com.netly.app.dto.AssetDTO;
import com.netly.app.dto.LiabilityDTO;
import com.netly.app.dto.PortfolioSummaryDTO;
import com.netly.app.dto.ResendEmailPayload;
import com.netly.app.model.User;
import com.netly.app.repository.UserRepository;
import com.netly.app.util.IndianNumberFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private final ResendEmailService resendEmailService;
    private final TemplateEngine templateEngine;
    private final AssetService assetService;
    private final LiabilityService liabilityService;
    private final UserRepository userRepository;

    public void sendReport() {
        // Get current user
        User currentUser = getCurrentUser();
        sendReportForUser(currentUser);
    }

    /**
     * Send portfolio report for a specific user (used by scheduler)
     * @param user The user to send the report to
     */
    public void sendReportForUser(User user) {
        // Fetch portfolio data for the user
        PortfolioSummaryDTO portfolioSummary = assetService.getPortfolioSummaryForUser(user.getId());
        List<AssetDTO> assets = assetService.getAllAssetsForUser(user.getId());
        List<LiabilityDTO> liabilities = liabilityService.getAllLiabilitiesForUser(user.getId());

        // Collect all email addresses (primary + secondary)
        List<String> allEmails = new java.util.ArrayList<>();
        allEmails.add(user.getEmail()); // Primary email

        // Add secondary emails if they exist
        if (user.getSecondaryEmails() != null && !user.getSecondaryEmails().trim().isEmpty()) {
            String[] secondaryEmails = user.getSecondaryEmails().split(",");
            for (String email : secondaryEmails) {
                String trimmedEmail = email.trim();
                if (!trimmedEmail.isEmpty()) {
                    allEmails.add(trimmedEmail);
                }
            }
        }

        // Prepare email
        String[] to = allEmails.toArray(new String[0]);
        ResendEmailPayload resendEmailPayload = new ResendEmailPayload();
        resendEmailPayload.setTo(to);
        resendEmailPayload.setSubject("📊 Your Netly Portfolio Report - " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        resendEmailPayload.setHtml(this.getEmailBody(user, portfolioSummary, assets, liabilities));

        // Send email
        resendEmailService.sendEmail(resendEmailPayload);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String getEmailBody(User user, PortfolioSummaryDTO portfolioSummary,
                                 List<AssetDTO> assets, List<LiabilityDTO> liabilities) {
        // Create context for the portfolio report template
        Context context = new Context();

        // Set user info
        context.setVariable("userName", user.getName());
        context.setVariable("reportDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

        // Set portfolio summary
        context.setVariable("portfolioSummary", portfolioSummary);

        // Format currency values using Indian number format
        context.setVariable("formattedNetWorth", IndianNumberFormatter.formatCurrency(portfolioSummary.getNetWorth()));
        context.setVariable("formattedTotalAssets", IndianNumberFormatter.formatCurrency(portfolioSummary.getTotalValue()));
        context.setVariable("formattedTotalLiabilities", IndianNumberFormatter.formatCurrency(portfolioSummary.getTotalLiabilities()));
        context.setVariable("formattedTotalGainLoss", IndianNumberFormatter.formatCurrency(portfolioSummary.getTotalGainLoss()));

        // Set assets and liabilities
        context.setVariable("assets", assets);
        context.setVariable("liabilities", liabilities);

        // Register formatter utility for use in template
        context.setVariable("formatter", IndianNumberFormatter.class);

        // Process the report template
        String contentHtml = templateEngine.process("email/portfolio-report", context);

        // Wrap in base layout
        Context baseContext = new Context();
        baseContext.setVariable("subject", "Your Netly Portfolio Report");
        baseContext.setVariable("contentHtml", contentHtml);

        return templateEngine.process("email/base-layout", baseContext);

    }

}

