package com.netly.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetItemDTO {
    private Long id;
    private String itemType; // INCOME or EXPENSE
    private String itemName;
    private BigDecimal amount;
    private Boolean isInvestment;
    private String description;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

