package com.netly.app.repository;

import com.netly.app.model.LiabilitySnapshot;
import com.netly.app.model.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiabilitySnapshotRepository extends JpaRepository<LiabilitySnapshot, Long> {

    List<LiabilitySnapshot> findByPortfolioSnapshot(PortfolioSnapshot portfolioSnapshot);

    @Query("SELECT l FROM LiabilitySnapshot l WHERE l.portfolioSnapshot IN :snapshots ORDER BY l.portfolioSnapshot.snapshotDate ASC")
    List<LiabilitySnapshot> findByPortfolioSnapshots(@Param("snapshots") List<PortfolioSnapshot> snapshots);

    @Query("SELECT l FROM LiabilitySnapshot l WHERE l.portfolioSnapshot IN :snapshots AND l.liabilityId = :liabilityId ORDER BY l.portfolioSnapshot.snapshotDate ASC")
    List<LiabilitySnapshot> findByPortfolioSnapshotsAndLiabilityId(
            @Param("snapshots") List<PortfolioSnapshot> snapshots,
            @Param("liabilityId") Long liabilityId);

    @Query("SELECT l FROM LiabilitySnapshot l WHERE l.portfolioSnapshot IN :snapshots AND l.liabilityTypeName = :typeName ORDER BY l.portfolioSnapshot.snapshotDate ASC")
    List<LiabilitySnapshot> findByPortfolioSnapshotsAndLiabilityTypeName(
            @Param("snapshots") List<PortfolioSnapshot> snapshots,
            @Param("typeName") String typeName);
}

