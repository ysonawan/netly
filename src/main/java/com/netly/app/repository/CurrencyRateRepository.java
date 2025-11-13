package com.netly.app.repository;

import com.netly.app.model.CurrencyRate;
import com.netly.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {

    List<CurrencyRate> findByUser(User user);
    Optional<CurrencyRate> findByUserAndCurrencyCode(User user, String currencyCode);
    void deleteByUserAndCurrencyCode(User user, String currencyCode);
}

