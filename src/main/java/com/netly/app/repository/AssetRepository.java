package com.netly.app.repository;

import com.netly.app.model.Asset;
import com.netly.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findByUserOrderByUpdatedAtDesc(User user);
    Optional<Asset> findByIdAndUser(Long id, User user);

    @Query("SELECT COUNT(a) FROM Asset a WHERE a.user = :user AND a.currency = :currency")
    Long countByUserAndCurrency(@Param("user") User user, @Param("currency") String currency);
}
