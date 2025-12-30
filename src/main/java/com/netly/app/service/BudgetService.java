package com.netly.app.service;

import com.netly.app.dto.BudgetItemDTO;
import com.netly.app.dto.BudgetSummaryDTO;
import com.netly.app.model.BudgetItem;
import com.netly.app.model.User;
import com.netly.app.repository.BudgetItemRepository;
import com.netly.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetItemRepository budgetItemRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<BudgetItemDTO> getAllBudgetItems() {
        User user = getCurrentUser();
        List<BudgetItem> items = budgetItemRepository.findByUserOrderByDisplayOrderAsc(user);
        return items.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BudgetItemDTO> getBudgetItemsByType(String type) {
        User user = getCurrentUser();
        BudgetItem.BudgetItemType itemType = BudgetItem.BudgetItemType.valueOf(type);
        List<BudgetItem> items = budgetItemRepository.findByUserAndItemTypeOrderByDisplayOrderAsc(user, itemType);
        return items.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BudgetItemDTO getBudgetItemById(Long id) {
        User user = getCurrentUser();
        BudgetItem item = budgetItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget item not found"));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        return convertToDTO(item);
    }

    @Transactional
    public BudgetItemDTO createBudgetItem(BudgetItemDTO budgetItemDTO) {
        User user = getCurrentUser();

        BudgetItem item = new BudgetItem();
        item.setUser(user);
        item.setItemType(BudgetItem.BudgetItemType.valueOf(budgetItemDTO.getItemType()));
        item.setItemName(budgetItemDTO.getItemName());
        item.setAmount(budgetItemDTO.getAmount());
        item.setIsInvestment(budgetItemDTO.getIsInvestment() != null ? budgetItemDTO.getIsInvestment() : false);
        item.setDescription(budgetItemDTO.getDescription());
        item.setDisplayOrder(budgetItemDTO.getDisplayOrder());

        BudgetItem savedItem = budgetItemRepository.save(item);
        return convertToDTO(savedItem);
    }

    @Transactional
    public BudgetItemDTO updateBudgetItem(Long id, BudgetItemDTO budgetItemDTO) {
        User user = getCurrentUser();
        BudgetItem item = budgetItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget item not found"));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        item.setItemType(BudgetItem.BudgetItemType.valueOf(budgetItemDTO.getItemType()));
        item.setItemName(budgetItemDTO.getItemName());
        item.setAmount(budgetItemDTO.getAmount());
        item.setIsInvestment(budgetItemDTO.getIsInvestment() != null ? budgetItemDTO.getIsInvestment() : false);
        item.setDescription(budgetItemDTO.getDescription());
        item.setDisplayOrder(budgetItemDTO.getDisplayOrder());

        BudgetItem updatedItem = budgetItemRepository.save(item);
        return convertToDTO(updatedItem);
    }

    @Transactional
    public void deleteBudgetItem(Long id) {
        User user = getCurrentUser();
        BudgetItem item = budgetItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget item not found"));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        budgetItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public BudgetSummaryDTO getBudgetSummary() {
        User user = getCurrentUser();
        List<BudgetItem> items = budgetItemRepository.findByUserOrderByDisplayOrderAsc(user);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalInvestments = BigDecimal.ZERO;
        BigDecimal totalNonInvestmentExpenses = BigDecimal.ZERO;

        for (BudgetItem item : items) {
            if (item.getItemType() == BudgetItem.BudgetItemType.INCOME) {
                totalIncome = totalIncome.add(item.getAmount());
            } else if (item.getItemType() == BudgetItem.BudgetItemType.EXPENSE) {
                totalExpenses = totalExpenses.add(item.getAmount());
                if (item.getIsInvestment()) {
                    totalInvestments = totalInvestments.add(item.getAmount());
                } else {
                    totalNonInvestmentExpenses = totalNonInvestmentExpenses.add(item.getAmount());
                }
            }
        }

        BigDecimal totalSurplus = totalIncome.subtract(totalExpenses);

        BudgetSummaryDTO summary = new BudgetSummaryDTO();
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpenses(totalExpenses);
        summary.setTotalInvestments(totalInvestments);
        summary.setTotalNonInvestmentExpenses(totalNonInvestmentExpenses);
        summary.setTotalSurplus(totalSurplus);

        // Calculate percentages
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            summary.setInvestmentPercentage(
                totalInvestments.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
            );
            summary.setNonInvestmentExpensePercentage(
                totalNonInvestmentExpenses.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
            );
            summary.setSurplusOrDeficitRate(
                totalSurplus.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
            );
        } else {
            summary.setInvestmentPercentage(BigDecimal.ZERO);
            summary.setNonInvestmentExpensePercentage(BigDecimal.ZERO);
            summary.setSurplusOrDeficitRate(BigDecimal.ZERO);
        }

        return summary;
    }

    private BudgetItemDTO convertToDTO(BudgetItem item) {
        BudgetItemDTO dto = new BudgetItemDTO();
        dto.setId(item.getId());
        dto.setItemType(item.getItemType().name());
        dto.setItemName(item.getItemName());
        dto.setAmount(item.getAmount());
        dto.setIsInvestment(item.getIsInvestment());
        dto.setDescription(item.getDescription());
        dto.setDisplayOrder(item.getDisplayOrder());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }
}

