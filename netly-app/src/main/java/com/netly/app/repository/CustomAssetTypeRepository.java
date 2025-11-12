package com.netly.app.repository;

import com.netly.app.model.CustomAssetType;
import com.netly.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomAssetTypeRepository extends JpaRepository<CustomAssetType, Long> {

    @Query("SELECT cat FROM CustomAssetType cat WHERE cat.user = :user ORDER BY cat.displayName ASC")
    List<CustomAssetType> findByUserOrderedByDisplayName(@Param("user") User user);
    boolean existsByUserAndTypeName(User user, String typeName);
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.assetType.id = :assetTypeId")
    Long countAssetsByAssetTypeId(@Param("assetTypeId") Long assetTypeId);
}
