package com.netly.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetSnapshotDTO {
    private Long id;
    private Long assetId;
    private String assetName;
    private String assetTypeName;
    private BigDecimal currentValue;
    private BigDecimal gainLoss;
    private String currency;
    private BigDecimal valueInInr;
}

