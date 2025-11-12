package com.netly.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRateDTO {
    private Long id;
    private String currencyCode;
    private String currencyName;
    private BigDecimal rateToInr;
    private Boolean isActive;
}

