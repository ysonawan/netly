package com.netly.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_snapshots", schema = "netly_schema",
    indexes = {
        @Index(name = "idx_asset_snapshots_portfolio_snapshot_id", columnList = "portfolio_snapshot_id"),
        @Index(name = "idx_asset_snapshots_asset_id", columnList = "asset_id")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_snapshot_id", nullable = false)
    private PortfolioSnapshot portfolioSnapshot;

    @Column(name = "asset_id")
    private Long assetId;

    @Column(name = "asset_name", nullable = false)
    private String assetName;

    @Column(name = "asset_type_name", nullable = false)
    private String assetTypeName;

    @Column(name = "current_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal currentValue;

    @Column(name = "gain_loss", precision = 15, scale = 2)
    private BigDecimal gainLoss;

    @Column(name = "currency")
    private String currency;

    @Column(name = "value_in_inr", precision = 15, scale = 2, nullable = false)
    private BigDecimal valueInInr;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

