package com.netly.app.service;

import com.netly.app.dto.CustomAssetTypeDTO;
import com.netly.app.dto.CustomLiabilityTypeDTO;
import com.netly.app.model.*;
import com.netly.app.repository.CustomAssetTypeRepository;
import com.netly.app.repository.CustomLiabilityTypeRepository;
import com.netly.app.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfigurationService {

    private final CustomAssetTypeRepository customAssetTypeRepository;
    private final CustomLiabilityTypeRepository customLiabilityTypeRepository;
    private final UserRepository userRepository;

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

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
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
    public CustomLiabilityTypeDTO saveCustomLiabilityType(CustomLiabilityTypeDTO dto) {
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
}

