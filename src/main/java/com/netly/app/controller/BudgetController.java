package com.netly.app.controller;

import com.netly.app.dto.BudgetItemDTO;
import com.netly.app.dto.BudgetSummaryDTO;
import com.netly.app.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetItemDTO>> getAllBudgetItems() {
        return ResponseEntity.ok(budgetService.getAllBudgetItems());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<BudgetItemDTO>> getBudgetItemsByType(@PathVariable String type) {
        return ResponseEntity.ok(budgetService.getBudgetItemsByType(type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetItemDTO> getBudgetItemById(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetItemById(id));
    }

    @PostMapping
    public ResponseEntity<BudgetItemDTO> createBudgetItem(@RequestBody BudgetItemDTO budgetItemDTO) {
        BudgetItemDTO createdItem = budgetService.createBudgetItem(budgetItemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetItemDTO> updateBudgetItem(@PathVariable Long id, @RequestBody BudgetItemDTO budgetItemDTO) {
        return ResponseEntity.ok(budgetService.updateBudgetItem(id, budgetItemDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudgetItem(@PathVariable Long id) {
        budgetService.deleteBudgetItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<BudgetSummaryDTO> getBudgetSummary() {
        return ResponseEntity.ok(budgetService.getBudgetSummary());
    }
}

