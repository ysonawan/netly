package com.netly.app.repository;

import com.netly.app.model.CustomLiabilityType;
import com.netly.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomLiabilityTypeRepository extends JpaRepository<CustomLiabilityType, Long> {

    @Query("SELECT clt FROM CustomLiabilityType clt WHERE clt.user = :user ORDER BY clt.displayName ASC")
    List<CustomLiabilityType> findByUserOrderedByDisplayName(@Param("user") User user);
    boolean existsByUserAndTypeName(User user, String typeName);
    @Query("SELECT COUNT(l) FROM Liability l WHERE l.liabilityType.id = :liabilityTypeId")
    Long countLiabilitiesByLiabilityTypeId(@Param("liabilityTypeId") Long liabilityTypeId);
}
