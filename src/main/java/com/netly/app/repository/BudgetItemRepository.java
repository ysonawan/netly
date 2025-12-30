package com.netly.app.repository;

import com.netly.app.model.BudgetItem;
import com.netly.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BudgetItemRepository extends JpaRepository<BudgetItem, Long> {
    List<BudgetItem> findByUserOrderByDisplayOrderAsc(User user);
    List<BudgetItem> findByUserAndItemTypeOrderByDisplayOrderAsc(User user, BudgetItem.BudgetItemType itemType);
    List<BudgetItem> findByUserIdOrderByDisplayOrderAsc(Long userId);
    List<BudgetItem> findByUserIdAndItemTypeOrderByDisplayOrderAsc(Long userId, BudgetItem.BudgetItemType itemType);
}

