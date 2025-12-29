package com.netly.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetSummaryDTO {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal totalInvestments;
    private BigDecimal totalNonInvestmentExpenses;
    private BigDecimal totalSurplus;
    private BigDecimal investmentPercentage;
    private BigDecimal nonInvestmentExpensePercentage;
    private BigDecimal savingsRate;
}

