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
public class AssetDTO {
    private Long id;
    private String name;
    private Long customAssetTypeId;
    private String assetTypeName;
    private String assetTypeDisplayName;
    private BigDecimal currentValue;
    private BigDecimal purchasePrice;
    private LocalDate purchaseDate;
    private BigDecimal quantity;
    private String description;
    private String location;
    private String currency;
    private Boolean illiquid;
    private BigDecimal gainLoss;
    private BigDecimal gainLossPercentage;
    private LocalDateTime updatedAt;
}
