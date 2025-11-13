package com.netly.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummaryDTO {
    private BigDecimal totalValue;
    private BigDecimal totalGainLoss;
    private BigDecimal totalGainLossPercentage;
    private int totalAssets;
    private AssetTypeBreakdown breakdown;

    // Liabilities
    private BigDecimal totalLiabilities;
    private int totalLiabilityCount;
    private LiabilityTypeBreakdown liabilityBreakdown;

    // Net Worth
    private BigDecimal netWorth;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetTypeBreakdown {
        private Map<String, BigDecimal> typeBreakdown;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LiabilityTypeBreakdown {
        private Map<String, BigDecimal> typeBreakdown;
    }
}
