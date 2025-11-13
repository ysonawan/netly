package com.netly.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "liabilities", schema = "netly_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Liability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_liability_type_id", nullable = false)
    private CustomLiabilityType liabilityType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal currentBalance;

    @Column(precision = 15, scale = 2)
    private BigDecimal originalAmount;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(precision = 15, scale = 2)
    private BigDecimal monthlyPayment;

    private String lender;

    private String description;

    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (currency == null) {
            currency = "INR";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public BigDecimal getPaidAmount() {
        if (originalAmount != null && currentBalance != null) {
            return originalAmount.subtract(currentBalance);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getRepaymentPercentage() {
        if (originalAmount != null && originalAmount.compareTo(BigDecimal.ZERO) > 0) {
            return getPaidAmount().divide(originalAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }
}
