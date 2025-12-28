package com.netly.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSnapshotDTO {
    private Long id;
    private LocalDate snapshotDate;
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal netWorth;
    private BigDecimal totalGains;
    private List<AssetSnapshotDTO> assetSnapshots;
    private List<LiabilitySnapshotDTO> liabilitySnapshots;
}

