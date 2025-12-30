package com.netly.app.service;

import com.netly.app.dto.AssetDTO;
import com.netly.app.dto.BudgetItemDTO;
import com.netly.app.dto.BudgetSummaryDTO;
import com.netly.app.dto.LiabilityDTO;
import com.netly.app.dto.PortfolioSummaryDTO;
import com.netly.app.dto.ResendEmailPayload;
import com.netly.app.model.BudgetItem;
import com.netly.app.model.User;
import com.netly.app.repository.BudgetItemRepository;
import com.netly.app.repository.UserRepository;
import com.netly.app.util.IndianNumberFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private final ResendEmailService resendEmailService;
    private final TemplateEngine templateEngine;
    private final AssetService assetService;
    private final LiabilityService liabilityService;
    private final BudgetItemRepository budgetItemRepository;
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

    /**
     * Send budget report for the current authenticated user
     */
    public void sendBudgetReport() {
        User currentUser = getCurrentUser();
        sendBudgetReportForUser(currentUser);
    }

    /**
     * Send budget report for a specific user (used by scheduler)
     * @param user The user to send the report to
     */
    public void sendBudgetReportForUser(User user) {
        // Fetch budget data for the user
        BudgetSummaryDTO budgetSummary = calculateBudgetSummaryForUser(user.getId());
        List<BudgetItemDTO> incomeItems = getBudgetItemsForUser(user.getId(), BudgetItem.BudgetItemType.INCOME);
        List<BudgetItemDTO> expenseItems = getBudgetItemsForUser(user.getId(), BudgetItem.BudgetItemType.EXPENSE);

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
        resendEmailPayload.setSubject("💰 Your Netly Budget Report - " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        resendEmailPayload.setHtml(this.getBudgetEmailBody(user, budgetSummary, incomeItems, expenseItems));

        // Send email
        resendEmailService.sendEmail(resendEmailPayload);
    }

    private BudgetSummaryDTO calculateBudgetSummaryForUser(Long userId) {
        List<BudgetItem> items = budgetItemRepository.findByUserIdOrderByDisplayOrderAsc(userId);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalInvestments = BigDecimal.ZERO;
        BigDecimal totalNonInvestmentExpenses = BigDecimal.ZERO;

        for (BudgetItem item : items) {
            if (item.getItemType() == BudgetItem.BudgetItemType.INCOME) {
                totalIncome = totalIncome.add(item.getAmount());
            } else if (item.getItemType() == BudgetItem.BudgetItemType.EXPENSE) {
                totalExpenses = totalExpenses.add(item.getAmount());
                if (item.getIsInvestment()) {
                    totalInvestments = totalInvestments.add(item.getAmount());
                } else {
                    totalNonInvestmentExpenses = totalNonInvestmentExpenses.add(item.getAmount());
                }
            }
        }

        BigDecimal totalSurplus = totalIncome.subtract(totalExpenses);

        BudgetSummaryDTO summary = new BudgetSummaryDTO();
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpenses(totalExpenses);
        summary.setTotalInvestments(totalInvestments);
        summary.setTotalNonInvestmentExpenses(totalNonInvestmentExpenses);
        summary.setTotalSurplus(totalSurplus);

        // Calculate percentages
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            summary.setInvestmentPercentage(
                totalInvestments.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
            );
            summary.setNonInvestmentExpensePercentage(
                totalNonInvestmentExpenses.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
            );
            summary.setSurplusOrDeficitRate(
                totalSurplus.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
            );
        } else {
            summary.setInvestmentPercentage(BigDecimal.ZERO);
            summary.setNonInvestmentExpensePercentage(BigDecimal.ZERO);
            summary.setSurplusOrDeficitRate(BigDecimal.ZERO);
        }

        return summary;
    }

    private List<BudgetItemDTO> getBudgetItemsForUser(Long userId, BudgetItem.BudgetItemType itemType) {
        List<BudgetItem> items = budgetItemRepository.findByUserIdAndItemTypeOrderByDisplayOrderAsc(userId, itemType);
        return items.stream()
                .map(this::convertBudgetItemToDTO)
                .collect(Collectors.toList());
    }

    private BudgetItemDTO convertBudgetItemToDTO(BudgetItem item) {
        BudgetItemDTO dto = new BudgetItemDTO();
        dto.setId(item.getId());
        dto.setItemType(item.getItemType().name());
        dto.setItemName(item.getItemName());
        dto.setAmount(item.getAmount());
        dto.setIsInvestment(item.getIsInvestment());
        dto.setDescription(item.getDescription());
        dto.setDisplayOrder(item.getDisplayOrder());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }

    private String getBudgetEmailBody(User user, BudgetSummaryDTO budgetSummary,
                                      List<BudgetItemDTO> incomeItems, List<BudgetItemDTO> expenseItems) {
        // Create context for the budget report template
        Context context = new Context();

        // Set user info
        context.setVariable("userName", user.getName());
        context.setVariable("reportDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

        // Set budget summary
        context.setVariable("budgetSummary", budgetSummary);

        // Format currency values using Indian number format
        context.setVariable("formattedTotalIncome", IndianNumberFormatter.formatCurrency(budgetSummary.getTotalIncome()));
        context.setVariable("formattedTotalExpenses", IndianNumberFormatter.formatCurrency(budgetSummary.getTotalExpenses()));
        context.setVariable("formattedTotalSurplus", IndianNumberFormatter.formatCurrency(budgetSummary.getTotalSurplus()));
        context.setVariable("formattedTotalInvestments", IndianNumberFormatter.formatCurrency(budgetSummary.getTotalInvestments()));

        // Calculate percentage bar widths (capped at 100)
        context.setVariable("surplusRateWidth", calculateBarWidth(budgetSummary.getSurplusOrDeficitRate()));
        context.setVariable("investmentRateWidth", calculateBarWidth(budgetSummary.getInvestmentPercentage()));
        context.setVariable("expenseRateWidth", calculateBarWidth(budgetSummary.getNonInvestmentExpensePercentage()));

        // Set income and expense items
        context.setVariable("incomeItems", incomeItems);
        context.setVariable("expenseItems", expenseItems);

        // Register formatter utility for use in template
        context.setVariable("formatter", IndianNumberFormatter.class);

        // Process the budget report template
        String contentHtml = templateEngine.process("email/budget-report", context);

        // Wrap in base layout
        Context baseContext = new Context();
        baseContext.setVariable("subject", "Your Netly Budget Report");
        baseContext.setVariable("contentHtml", contentHtml);

        return templateEngine.process("email/base-layout", baseContext);
    }

    private String calculateBarWidth(BigDecimal percentage) {
        if (percentage == null) {
            return "0";
        }
        if (percentage.compareTo(BigDecimal.ZERO) < 0) {
            return "0";
        }
        if (percentage.compareTo(new BigDecimal("100")) > 0) {
            return "100";
        }
        return percentage.setScale(2, RoundingMode.HALF_UP).toString();
    }

}
