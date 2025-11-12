package com.netly.app.service;

import com.netly.app.dto.CurrencyRateDTO;
import com.netly.app.dto.CustomAssetTypeDTO;
import com.netly.app.dto.CustomLiabilityTypeDTO;
import com.netly.app.model.*;
import com.netly.app.repository.AssetRepository;
import com.netly.app.repository.CurrencyRateRepository;
import com.netly.app.repository.CustomAssetTypeRepository;
import com.netly.app.repository.CustomLiabilityTypeRepository;
import com.netly.app.repository.LiabilityRepository;
import com.netly.app.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfigurationService {

    private final CurrencyRateRepository currencyRateRepository;
    private final CustomAssetTypeRepository customAssetTypeRepository;
    private final CustomLiabilityTypeRepository customLiabilityTypeRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final LiabilityRepository liabilityRepository;

    // Default display names for asset types
    private static final Map<AssetType, String> DEFAULT_ASSET_NAMES = Map.of(
            AssetType.EQUITY, "Equity",
            AssetType.CASH, "Cash",
            AssetType.REAL_ESTATE, "Real Estate",
            AssetType.GOLD, "Gold",
            AssetType.DEBT, "Debt",
            AssetType.MUTUAL_FUND, "Mutual Fund",
            AssetType.CRYPTOCURRENCY, "Cryptocurrency",
            AssetType.BONDS, "Bonds",
            AssetType.OTHER, "Other"
    );

    // Default display names for liability types
    private static final Map<LiabilityType, String> DEFAULT_LIABILITY_NAMES = Map.of(
            LiabilityType.HOME_LOAN, "Home Loan",
            LiabilityType.CAR_LOAN, "Car Loan",
            LiabilityType.PERSONAL_LOAN, "Personal Loan",
            LiabilityType.CREDIT_CARD, "Credit Card",
            LiabilityType.EDUCATION_LOAN, "Education Loan",
            LiabilityType.BUSINESS_LOAN, "Business Loan",
            LiabilityType.OTHER, "Other"
    );

    // Default currency rates (1 unit of currency = X INR)
    private static final Map<String, CurrencyData> DEFAULT_CURRENCIES = Map.of(
            "INR", new CurrencyData("Indian Rupee", new BigDecimal("1.000000")),
            "USD", new CurrencyData("US Dollar", new BigDecimal("83.000000")),
            "EUR", new CurrencyData("Euro", new BigDecimal("90.000000")),
            "GBP", new CurrencyData("British Pound", new BigDecimal("105.000000")),
            "AED", new CurrencyData("UAE Dirham", new BigDecimal("22.600000")),
            "SGD", new CurrencyData("Singapore Dollar", new BigDecimal("62.000000"))
    );

    private static class CurrencyData {
        String name;
        BigDecimal rate;

        CurrencyData(String name, BigDecimal rate) {
            this.name = name;
            this.rate = rate;
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Currency Rate methods
    public List<CurrencyRateDTO> getAllCurrencyRates(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ensureDefaultCurrencyRates(user);

        Map<String, CurrencyRate> userRates = currencyRateRepository.findByUser(user)
                .stream()
                .collect(Collectors.toMap(CurrencyRate::getCurrencyCode, rate -> rate));

        List<CurrencyRateDTO> result = new ArrayList<>();

        // Add user's custom currencies
        for (CurrencyRate rate : userRates.values()) {
            result.add(convertToDTO(rate));
        }

        return result.stream()
                .sorted(Comparator.comparing(CurrencyRateDTO::getCurrencyCode))
                .collect(Collectors.toList());
    }

    @Transactional
    public CurrencyRateDTO saveCurrencyRate(String email, CurrencyRateDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if currency code or name already exists for this user (excluding current rate if updating)
        List<CurrencyRate> existingRates = currencyRateRepository.findByUser(user);
        for (CurrencyRate rate : existingRates) {
            boolean isSameId = dto.getId() != null && rate.getId().equals(dto.getId());
            if (!isSameId) {
                if (rate.getCurrencyCode().equalsIgnoreCase(dto.getCurrencyCode())) {
                    throw new RuntimeException("Currency code already exists");
                }
                if (rate.getCurrencyName() != null && dto.getCurrencyName() != null && rate.getCurrencyName().equalsIgnoreCase(dto.getCurrencyName())) {
                    throw new RuntimeException("Currency name already exists");
                }
            }
        }

        CurrencyRate rate = currencyRateRepository
                .findByUserAndCurrencyCode(user, dto.getCurrencyCode())
                .orElse(new CurrencyRate());

        rate.setUser(user);
        rate.setCurrencyCode(dto.getCurrencyCode().toUpperCase());
        rate.setCurrencyName(dto.getCurrencyName());
        rate.setRateToInr(dto.getRateToInr());
        rate.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        rate = currencyRateRepository.save(rate);

        return convertToDTO(rate);
    }

    @Transactional
    public void deleteCurrencyRate(String email, String currencyCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Don't allow deleting INR
        if ("INR".equals(currencyCode)) {
            throw new RuntimeException("Cannot delete INR currency");
        }

        // Check if this currency is referenced by any assets
        Long assetCount = assetRepository.countByUserAndCurrency(user, currencyCode);
        if (assetCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete currency because it is being used by " + assetCount + " asset(s). " +
                            "Please reassign or delete those assets first."
            );
        }

        // Check if this currency is referenced by any liabilities
        Long liabilityCount = liabilityRepository.countByUserAndCurrency(user, currencyCode);
        if (liabilityCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete currency because it is being used by " + liabilityCount + " liability(ies). " +
                            "Please reassign or delete those liabilities first."
            );
        }
        currencyRateRepository.deleteByUserAndCurrencyCode(user, currencyCode);
    }

    private CurrencyRateDTO convertToDTO(CurrencyRate rate) {
        CurrencyRateDTO dto = new CurrencyRateDTO();
        dto.setId(rate.getId());
        dto.setCurrencyCode(rate.getCurrencyCode());
        dto.setCurrencyName(rate.getCurrencyName());
        dto.setRateToInr(rate.getRateToInr());
        dto.setIsActive(rate.getIsActive());
        return dto;
    }

    // Custom Asset Type methods
    public List<CustomAssetTypeDTO> getAllCustomAssetTypes() {
        User user = this.getCurrentUser();
        ensureDefaultCustomAssetTypes(user);
        return customAssetTypeRepository.findByUserOrderedByDisplayName(user).stream()
                .map(this::convertToCustomAssetDTO)
                .sorted(Comparator.comparing(CustomAssetTypeDTO::getDisplayName))
                .collect(Collectors.toList());
    }

    private void ensureDefaultCustomAssetTypes(User user) {
        List<CustomAssetType> existingTypes = customAssetTypeRepository.findByUserOrderedByDisplayName(user);
        if(existingTypes.isEmpty()) {
            for (Map.Entry<AssetType, String> entry : DEFAULT_ASSET_NAMES.entrySet()) {
                String typeName = entry.getKey().name();
                CustomAssetType newType = new CustomAssetType();
                newType.setUser(user);
                newType.setTypeName(typeName);
                newType.setDisplayName(entry.getValue());
                newType.setDescription("Default " + entry.getValue() + " type");
                newType.setIsActive(true);
                customAssetTypeRepository.save(newType);
            }
        }
    }

    @Transactional
    public CustomAssetTypeDTO saveCustomAssetType(CustomAssetTypeDTO dto) {
        User user = this.getCurrentUser();

        // Generate type name from display name if not provided
        String typeName = dto.getTypeName();
        if (typeName == null || typeName.isEmpty()) {
            typeName = dto.getDisplayName().toUpperCase().replaceAll("\\s+", "_");
        }

        // Check if type name already exists for this user
        if (dto.getId() == null && customAssetTypeRepository.existsByUserAndTypeName(user, typeName)) {
            throw new RuntimeException("Custom asset type with this name already exists");
        }

        // Check if display name already exists for this user (excluding current type if updating)
        List<CustomAssetType> existingTypes = customAssetTypeRepository.findByUserOrderedByDisplayName(user);
        if(dto.getId() == null && existingTypes.size() >= 20) {
            throw new RuntimeException("Cannot add more than 20 custom asset types");
        }
        for (CustomAssetType type : existingTypes) {
            if (type.getDisplayName().equalsIgnoreCase(dto.getDisplayName()) && (dto.getId() == null || !type.getId().equals(dto.getId()))) {
                throw new RuntimeException("Custom asset type with this display name already exists");
            }
        }

        CustomAssetType customType = dto.getId() != null
                ? customAssetTypeRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Custom asset type not found"))
                : new CustomAssetType();

        customType.setUser(user);
        customType.setTypeName(typeName);
        customType.setDisplayName(dto.getDisplayName());
        customType.setDescription(dto.getDescription());
        customType.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        customType = customAssetTypeRepository.save(customType);

        return convertToCustomAssetDTO(customType);
    }

    @Transactional
    public void deleteCustomAssetType(Long id) {
        User user = this.getCurrentUser();

        CustomAssetType customType = customAssetTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Custom asset type not found"));

        if (!customType.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this custom asset type");
        }

        // Check if this asset type is referenced by any assets
        Long assetCount = customAssetTypeRepository.countAssetsByAssetTypeId(id);
        if (assetCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete asset type because it is being used by " + assetCount + " asset(s). " +
                            "Please reassign or delete those assets first."
            );
        }
        customAssetTypeRepository.delete(customType);
    }

    private CustomAssetTypeDTO convertToCustomAssetDTO(CustomAssetType customType) {
        CustomAssetTypeDTO dto = new CustomAssetTypeDTO();
        dto.setId(customType.getId());
        dto.setTypeName(customType.getTypeName());
        dto.setDisplayName(customType.getDisplayName());
        dto.setDescription(customType.getDescription());
        dto.setIsActive(customType.getIsActive());
        return dto;
    }

    // Custom Liability Type methods
    public List<CustomLiabilityTypeDTO> getAllCustomLiabilityTypes() {
        User user = this.getCurrentUser();
        ensureDefaultCustomLiabilityTypes(user);
        return customLiabilityTypeRepository.findByUserOrderedByDisplayName(user).stream()
                .map(this::convertToCustomLiabilityDTO)
                .sorted(Comparator.comparing(CustomLiabilityTypeDTO::getDisplayName))
                .collect(Collectors.toList());
    }

    private void ensureDefaultCustomLiabilityTypes(User user) {
        List<CustomLiabilityType> existingTypes = customLiabilityTypeRepository.findByUserOrderedByDisplayName(user);
        if(existingTypes.isEmpty()) {
            for (Map.Entry<LiabilityType, String> entry : DEFAULT_LIABILITY_NAMES.entrySet()) {
                String typeName = entry.getKey().name();
                CustomLiabilityType newType = new CustomLiabilityType();
                newType.setUser(user);
                newType.setTypeName(typeName);
                newType.setDisplayName(entry.getValue());
                newType.setDescription("Default " + entry.getValue() + " type");
                newType.setIsActive(true);
                customLiabilityTypeRepository.save(newType);
            }
        }
    }

    @Transactional
    public CustomLiabilityTypeDTO saveCustomLiabilityType( CustomLiabilityTypeDTO dto) {
        User user = this.getCurrentUser();

        // Generate type name from display name if not provided
        String typeName = dto.getTypeName();
        if (typeName == null || typeName.isEmpty()) {
            typeName = dto.getDisplayName().toUpperCase().replaceAll("\\s+", "_");
        }

        // Check if type name already exists for this user
        if (dto.getId() == null && customLiabilityTypeRepository.existsByUserAndTypeName(user, typeName)) {
            throw new RuntimeException("Custom liability type with this name already exists");
        }

        // Check if display name already exists for this user (excluding current type if updating)
        List<CustomLiabilityType> existingTypes = customLiabilityTypeRepository.findByUserOrderedByDisplayName(user);
        if(dto.getId() == null && existingTypes.size() > 20) {
            throw new RuntimeException("Cannot add more than 20 custom liability types");
        }
        for (CustomLiabilityType type : existingTypes) {
            if (type.getDisplayName().equalsIgnoreCase(dto.getDisplayName()) && (dto.getId() == null || !type.getId().equals(dto.getId()))) {
                throw new RuntimeException("Custom liability type with this display name already exists");
            }
        }

        CustomLiabilityType customType = dto.getId() != null
                ? customLiabilityTypeRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Custom liability type not found"))
                : new CustomLiabilityType();

        customType.setUser(user);
        customType.setTypeName(typeName);
        customType.setDisplayName(dto.getDisplayName());
        customType.setDescription(dto.getDescription());
        customType.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        customType = customLiabilityTypeRepository.save(customType);

        return convertToCustomLiabilityDTO(customType);
    }

    @Transactional
    public void deleteCustomLiabilityType(Long id) {
        User user = this.getCurrentUser();
        CustomLiabilityType customType = customLiabilityTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Custom liability type not found"));

        if (!customType.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this custom liability type");
        }
        // Check if this liability type is referenced by any liabilities
        Long liabilityCount = customLiabilityTypeRepository.countLiabilitiesByLiabilityTypeId(id);
        if (liabilityCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete liability type because it is being used by " + liabilityCount + " liability(ies). " +
                            "Please reassign or delete those liabilities first."
            );
        }

        customLiabilityTypeRepository.delete(customType);
    }

    private CustomLiabilityTypeDTO convertToCustomLiabilityDTO(CustomLiabilityType customType) {
        CustomLiabilityTypeDTO dto = new CustomLiabilityTypeDTO();
        dto.setId(customType.getId());
        dto.setTypeName(customType.getTypeName());
        dto.setDisplayName(customType.getDisplayName());
        dto.setDescription(customType.getDescription());
        dto.setIsActive(customType.getIsActive());
        return dto;
    }

    private void ensureDefaultCurrencyRates(User user) {
        List<CurrencyRate> existingRates = currencyRateRepository.findByUser(user);
        if(existingRates.isEmpty()) {
            for (Map.Entry<String, CurrencyData> entry : DEFAULT_CURRENCIES.entrySet()) {
                String code = entry.getKey();
                CurrencyData data = entry.getValue();
                CurrencyRate newRate = new CurrencyRate();
                newRate.setUser(user);
                newRate.setCurrencyCode(code);
                newRate.setCurrencyName(data.name);
                newRate.setRateToInr(data.rate);
                newRate.setIsActive(true);
                currencyRateRepository.save(newRate);
            }
        }
    }
}
