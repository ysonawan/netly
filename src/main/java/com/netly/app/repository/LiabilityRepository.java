package com.netly.app.repository;

import com.netly.app.model.Liability;
import com.netly.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiabilityRepository extends JpaRepository<Liability, Long> {

    List<Liability> findByUserOrderByUpdatedAtDesc(User user);
    Optional<Liability> findByIdAndUser(Long id, User user);
}
