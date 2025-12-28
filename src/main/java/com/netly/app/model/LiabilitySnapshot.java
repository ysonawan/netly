package com.netly.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "liability_snapshots", schema = "netly_schema",
    indexes = {
        @Index(name = "idx_liability_snapshots_portfolio_snapshot_id", columnList = "portfolio_snapshot_id"),
        @Index(name = "idx_liability_snapshots_liability_id", columnList = "liability_id")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiabilitySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_snapshot_id", nullable = false)
    private PortfolioSnapshot portfolioSnapshot;

    @Column(name = "liability_id")
    private Long liabilityId;

    @Column(name = "liability_name", nullable = false)
    private String liabilityName;

    @Column(name = "liability_type_name", nullable = false)
    private String liabilityTypeName;

    @Column(name = "current_balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal currentBalance;

    @Column(name = "currency")
    private String currency;

    @Column(name = "balance_in_inr", precision = 15, scale = 2, nullable = false)
    private BigDecimal balanceInInr;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

