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
@Table(name = "assets", schema = "netly_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_asset_type_id", nullable = false)
    private CustomAssetType assetType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal currentValue;

    @Column(precision = 15, scale = 2)
    private BigDecimal purchasePrice;

    private LocalDate purchaseDate;

    @Column(precision = 10, scale = 2)
    private BigDecimal quantity;

    private String description;

    private String location; // For real estate

    private String currency;

    private Boolean illiquid;

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

    public BigDecimal getGainLoss() {
        if (purchasePrice != null && quantity != null) {
            BigDecimal totalPurchase = purchasePrice.multiply(quantity);
            return currentValue.subtract(totalPurchase);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getGainLossPercentage() {
        if (purchasePrice != null && quantity != null && purchasePrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalPurchase = purchasePrice.multiply(quantity);
            return getGainLoss().divide(totalPurchase, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }
}
