package com.netly.app.repository;

import com.netly.app.model.PortfolioSnapshot;
import com.netly.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long> {

    List<PortfolioSnapshot> findByUserOrderBySnapshotDateDesc(User user);

    List<PortfolioSnapshot> findByUserAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            User user, LocalDate startDate, LocalDate endDate);

    Optional<PortfolioSnapshot> findByUserAndSnapshotDate(User user, LocalDate snapshotDate);

    @Query("SELECT ps FROM PortfolioSnapshot ps WHERE ps.user = :user " +
           "AND ps.snapshotDate >= :startDate ORDER BY ps.snapshotDate ASC")
    List<PortfolioSnapshot> findRecentSnapshots(@Param("user") User user, @Param("startDate") LocalDate startDate);

    long countByUser(User user);
}

