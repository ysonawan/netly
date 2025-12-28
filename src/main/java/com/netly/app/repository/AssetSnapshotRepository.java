package com.netly.app.repository;

import com.netly.app.model.AssetSnapshot;
import com.netly.app.model.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetSnapshotRepository extends JpaRepository<AssetSnapshot, Long> {

    List<AssetSnapshot> findByPortfolioSnapshot(PortfolioSnapshot portfolioSnapshot);

    @Query("SELECT a FROM AssetSnapshot a WHERE a.portfolioSnapshot IN :snapshots ORDER BY a.portfolioSnapshot.snapshotDate ASC")
    List<AssetSnapshot> findByPortfolioSnapshots(@Param("snapshots") List<PortfolioSnapshot> snapshots);

    @Query("SELECT a FROM AssetSnapshot a WHERE a.portfolioSnapshot IN :snapshots AND a.assetId = :assetId ORDER BY a.portfolioSnapshot.snapshotDate ASC")
    List<AssetSnapshot> findByPortfolioSnapshotsAndAssetId(
            @Param("snapshots") List<PortfolioSnapshot> snapshots,
            @Param("assetId") Long assetId);

    @Query("SELECT a FROM AssetSnapshot a WHERE a.portfolioSnapshot IN :snapshots AND a.assetTypeName = :typeName ORDER BY a.portfolioSnapshot.snapshotDate ASC")
    List<AssetSnapshot> findByPortfolioSnapshotsAndAssetTypeName(
            @Param("snapshots") List<PortfolioSnapshot> snapshots,
            @Param("typeName") String typeName);
}

