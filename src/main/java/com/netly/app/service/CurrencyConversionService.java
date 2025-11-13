package com.netly.app.service;

import com.netly.app.model.CurrencyRate;
import com.netly.app.model.User;
import com.netly.app.repository.CurrencyRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrencyConversionService {

    private final CurrencyRateRepository currencyRateRepository;

    /**
     * Convert amount from source currency to INR
     * @param amount The amount to convert
     * @param fromCurrency The source currency code
     * @param user The user for whom to fetch currency rates
     * @return The amount converted to INR
     */
    @Transactional(readOnly = true)
    public BigDecimal convertToINR(BigDecimal amount, String fromCurrency, User user) {
        if (amount == null || fromCurrency == null) {
            return BigDecimal.ZERO;
        }

        // If already in INR, return as is
        if ("INR".equalsIgnoreCase(fromCurrency)) {
            return amount;
        }

        // Get user's currency rates
        Map<String, BigDecimal> currencyRates = getCurrencyRatesMap(user);

        // Get the rate for the source currency
        BigDecimal rateToINR = currencyRates.get(fromCurrency.toUpperCase());

        if (rateToINR == null) {
            // If rate not found, throw a more specific exception
            throw new IllegalArgumentException("Currency rate not found for: " + fromCurrency);
        }

        return amount.multiply(rateToINR);
    }

    /**
     * Get all currency rates for a user as a map
     * @param user The user
     * @return Map of currency code to INR rate
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getCurrencyRatesMap(User user) {
        return currencyRateRepository.findByUser(user)
                .stream()
                .filter(CurrencyRate::getIsActive)
                .collect(Collectors.toMap(
                        rate -> rate.getCurrencyCode().toUpperCase(),
                        CurrencyRate::getRateToInr
                ));
    }
}
