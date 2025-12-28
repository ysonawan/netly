package com.netly.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiabilitySnapshotDTO {
    private Long id;
    private Long liabilityId;
    private String liabilityName;
    private String liabilityTypeName;
    private BigDecimal currentBalance;
    private String currency;
    private BigDecimal balanceInInr;
}

