package com.netly.app.repository;

import com.netly.app.model.CustomLiabilityType;
import com.netly.app.model.Liability;
import com.netly.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiabilityRepository extends JpaRepository<Liability, Long> {

    List<Liability> findByUser(User user);
    List<Liability> findByUserAndLiabilityType(User user, CustomLiabilityType liabilityType);
    Optional<Liability> findByIdAndUser(Long id, User user);

    @Query("SELECT COUNT(l) FROM Liability l WHERE l.user = :user AND l.currency = :currency")
    Long countByUserAndCurrency(@Param("user") User user, @Param("currency") String currency);
}
