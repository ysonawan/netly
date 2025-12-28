package com.netly.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiabilityDTO {
    private Long id;
    private String name;
    private Long customLiabilityTypeId;
    private String liabilityTypeName;
    private String liabilityTypeDisplayName;
    private BigDecimal currentBalance;
    private BigDecimal originalAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal interestRate;
    private BigDecimal monthlyPayment;
    private String lender;
    private String description;
    private String currency;
    private BigDecimal paidAmount;
    private BigDecimal repaymentPercentage;
    private LocalDateTime updatedAt;
}
