package com.netly.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioHistoryDTO {
    private List<String> dates;
    private List<BigDecimal> totalAssets;
    private List<BigDecimal> totalLiabilities;
    private List<BigDecimal> netWorth;
    private List<BigDecimal> totalGains;
}

