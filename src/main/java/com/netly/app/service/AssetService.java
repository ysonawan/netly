package com.netly.app.service;

import com.netly.app.dto.AssetDTO;
import com.netly.app.dto.PortfolioSummaryDTO;
import com.netly.app.model.Asset;
import com.netly.app.model.CustomAssetType;
import com.netly.app.model.Liability;
import com.netly.app.model.User;
import com.netly.app.repository.AssetRepository;
import com.netly.app.repository.CustomAssetTypeRepository;
import com.netly.app.repository.LiabilityRepository;
import com.netly.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final LiabilityRepository liabilityRepository;
    private final UserRepository userRepository;
    private final CustomAssetTypeRepository customAssetTypeRepository;
    private final CurrencyConversionService currencyConversionService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<AssetDTO> getAllAssets() {
        User currentUser = getCurrentUser();
        return assetRepository.findByUserOrderByUpdatedAtDesc(currentUser).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all assets for a specific user (used by scheduler)
     */
    @Transactional(readOnly = true)
    public List<AssetDTO> getAllAssetsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return assetRepository.findByUserOrderByUpdatedAtDesc(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AssetDTO getAssetById(Long id) {
        User currentUser = getCurrentUser();
        Asset asset = assetRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Asset not found with id: " + id));
        return convertToDTO(asset);
    }

    @Transactional
    public AssetDTO createAsset(AssetDTO assetDTO) {
        User currentUser = getCurrentUser();
        CustomAssetType assetType = customAssetTypeRepository.findById(assetDTO.getCustomAssetTypeId())
                .orElseThrow(() -> new RuntimeException("Asset type not found with id: " + assetDTO.getCustomAssetTypeId()));

        if (!assetType.getUser().equals(currentUser)) {
            throw new RuntimeException("Asset type does not belong to current user");
        }

        Asset asset = convertToEntity(assetDTO, assetType);
        asset.setUser(currentUser);
        Asset savedAsset = assetRepository.save(asset);
        return convertToDTO(savedAsset);
    }

    @Transactional
    public AssetDTO updateAsset(Long id, AssetDTO assetDTO) {
        User currentUser = getCurrentUser();
        Asset existingAsset = assetRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Asset not found with id: " + id));

        CustomAssetType assetType = customAssetTypeRepository.findById(assetDTO.getCustomAssetTypeId())
                .orElseThrow(() -> new RuntimeException("Asset type not found with id: " + assetDTO.getCustomAssetTypeId()));

        if (!assetType.getUser().equals(currentUser)) {
            throw new RuntimeException("Asset type does not belong to current user");
        }

        existingAsset.setName(assetDTO.getName());
        existingAsset.setAssetType(assetType);
        existingAsset.setCurrentValue(assetDTO.getCurrentValue());
        existingAsset.setPurchasePrice(assetDTO.getPurchasePrice());
        existingAsset.setPurchaseDate(assetDTO.getPurchaseDate());
        existingAsset.setQuantity(assetDTO.getQuantity());
        existingAsset.setDescription(assetDTO.getDescription());
        existingAsset.setLocation(assetDTO.getLocation());
        existingAsset.setCurrency(assetDTO.getCurrency());
        existingAsset.setIlliquid(assetDTO.getIlliquid());

        Asset updatedAsset = assetRepository.save(existingAsset);
        return convertToDTO(updatedAsset);
    }

    @Transactional
    public void deleteAsset(Long id) {
        User currentUser = getCurrentUser();
        Asset asset = assetRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Asset not found with id: " + id));
        assetRepository.delete(asset);
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryDTO getPortfolioSummary() {
        User currentUser = getCurrentUser();
        return getPortfolioSummaryForUser(currentUser.getId());
    }

    /**
     * Get portfolio summary for a specific user (used by scheduler)
     */
    @Transactional(readOnly = true)
    public PortfolioSummaryDTO getPortfolioSummaryForUser(Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Asset> assets = assetRepository.findByUserOrderByUpdatedAtDesc(currentUser);

        // Convert all asset values to INR before calculations
        BigDecimal totalValue = assets.stream()
                .map(asset -> currencyConversionService.convertToINR(
                        asset.getCurrentValue(),
                        asset.getCurrency(),
                        currentUser))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGainLoss = assets.stream()
                .map(asset -> {
                    BigDecimal gainLoss = asset.getGainLoss();
                    return currencyConversionService.convertToINR(
                            gainLoss,
                            asset.getCurrency(),
                            currentUser);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPurchaseValue = assets.stream()
                .filter(a -> a.getPurchasePrice() != null && a.getQuantity() != null)
                .map(a -> {
                    BigDecimal purchaseValue = a.getPurchasePrice().multiply(a.getQuantity());
                    return currencyConversionService.convertToINR(
                            purchaseValue,
                            a.getCurrency(),
                            currentUser);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGainLossPercentage = BigDecimal.ZERO;
        if (totalPurchaseValue.compareTo(BigDecimal.ZERO) > 0) {
            totalGainLossPercentage = totalGainLoss
                    .divide(totalPurchaseValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        PortfolioSummaryDTO.AssetTypeBreakdown breakdown = calculateBreakdown(assets, currentUser);

        List<Liability> liabilities = liabilityRepository.findByUserOrderByUpdatedAtDesc(currentUser);
        BigDecimal totalLiabilities = liabilities.stream()
                .map(liability -> currencyConversionService.convertToINR(
                        liability.getCurrentBalance(),
                        liability.getCurrency(),
                        currentUser))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PortfolioSummaryDTO.LiabilityTypeBreakdown liabilityBreakdown = calculateLiabilityBreakdown(liabilities, currentUser);

        BigDecimal netWorth = totalValue.subtract(totalLiabilities);

        PortfolioSummaryDTO summary = new PortfolioSummaryDTO();
        summary.setTotalValue(totalValue);
        summary.setTotalGainLoss(totalGainLoss);
        summary.setTotalGainLossPercentage(totalGainLossPercentage);
        summary.setTotalAssets(assets.size());
        summary.setBreakdown(breakdown);
        summary.setTotalLiabilities(totalLiabilities);
        summary.setTotalLiabilityCount((int) liabilities.stream().filter(l -> l.getCurrentBalance() != null && l.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0).count());
        summary.setLiabilityBreakdown(liabilityBreakdown);
        summary.setNetWorth(netWorth);

        return summary;
    }

    private PortfolioSummaryDTO.AssetTypeBreakdown calculateBreakdown(List<Asset> assets, User currentUser) {
        PortfolioSummaryDTO.AssetTypeBreakdown breakdown = new PortfolioSummaryDTO.AssetTypeBreakdown();

        // Dynamic breakdown based on actual custom asset types - convert to INR
        Map<String, BigDecimal> typeBreakdown = assets.stream()
                .collect(Collectors.groupingBy(
                        asset -> asset.getAssetType().getDisplayName(),
                        Collectors.mapping(asset -> currencyConversionService.convertToINR(
                                asset.getCurrentValue(),
                                asset.getCurrency(),
                                currentUser),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
        breakdown.setTypeBreakdown(typeBreakdown);
        return breakdown;
    }

    private PortfolioSummaryDTO.LiabilityTypeBreakdown calculateLiabilityBreakdown(List<Liability> liabilities, User currentUser) {
        PortfolioSummaryDTO.LiabilityTypeBreakdown breakdown = new PortfolioSummaryDTO.LiabilityTypeBreakdown();

        // Dynamic breakdown based on actual custom liability types - convert to INR
        Map<String, BigDecimal> typeBreakdown = liabilities.stream()
                .collect(Collectors.groupingBy(
                        liability -> liability.getLiabilityType().getDisplayName(),
                        Collectors.mapping(liability -> currencyConversionService.convertToINR(
                                liability.getCurrentBalance(),
                                liability.getCurrency(),
                                currentUser),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        breakdown.setTypeBreakdown(typeBreakdown);
        return breakdown;
    }

    private AssetDTO convertToDTO(Asset asset) {
        AssetDTO dto = new AssetDTO();
        dto.setId(asset.getId());
        dto.setName(asset.getName());
        dto.setCustomAssetTypeId(asset.getAssetType().getId());
        dto.setAssetTypeName(asset.getAssetType().getTypeName());
        dto.setAssetTypeDisplayName(asset.getAssetType().getDisplayName());
        dto.setCurrentValue(asset.getCurrentValue());
        dto.setPurchasePrice(asset.getPurchasePrice());
        dto.setPurchaseDate(asset.getPurchaseDate());
        dto.setQuantity(asset.getQuantity());
        dto.setDescription(asset.getDescription());
        dto.setLocation(asset.getLocation());
        dto.setCurrency(asset.getCurrency());
        dto.setIlliquid(asset.getIlliquid());
        dto.setGainLoss(asset.getGainLoss());
        dto.setGainLossPercentage(asset.getGainLossPercentage());
        dto.setUpdatedAt(asset.getUpdatedAt());
        return dto;
    }

    private Asset convertToEntity(AssetDTO dto, CustomAssetType assetType) {
        Asset asset = new Asset();
        asset.setName(dto.getName());
        asset.setAssetType(assetType);
        asset.setCurrentValue(dto.getCurrentValue());
        asset.setPurchasePrice(dto.getPurchasePrice());
        asset.setPurchaseDate(dto.getPurchaseDate());
        asset.setQuantity(dto.getQuantity());
        asset.setDescription(dto.getDescription());
        asset.setLocation(dto.getLocation());
        asset.setCurrency(dto.getCurrency());
        asset.setIlliquid(dto.getIlliquid());
        return asset;
    }
}
