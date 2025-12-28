package com.netly.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_snapshots", schema = "netly_schema",
    indexes = {
        @Index(name = "idx_portfolio_snapshots_user_id", columnList = "user_id"),
        @Index(name = "idx_portfolio_snapshots_snapshot_date", columnList = "snapshot_date"),
        @Index(name = "idx_portfolio_snapshots_user_date", columnList = "user_id, snapshot_date")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    // Overall portfolio metrics
    @Column(name = "total_assets", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAssets;

    @Column(name = "total_liabilities", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalLiabilities;

    @Column(name = "net_worth", precision = 15, scale = 2, nullable = false)
    private BigDecimal netWorth;

    @Column(name = "total_gains", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalGains;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

