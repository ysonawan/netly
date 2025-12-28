package com.netly.app.service;

import com.netly.app.dto.*;
import com.netly.app.model.*;
import com.netly.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioSnapshotService {

    private final PortfolioSnapshotRepository portfolioSnapshotRepository;
    private final AssetSnapshotRepository assetSnapshotRepository;
    private final LiabilitySnapshotRepository liabilitySnapshotRepository;
    private final AssetRepository assetRepository;
    private final LiabilityRepository liabilityRepository;
    private final UserRepository userRepository;
    private final CurrencyConversionService currencyConversionService;
    private final AssetService assetService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Create a snapshot for a specific user (used by scheduler)
     */
    @Transactional
    public PortfolioSnapshot createSnapshotForUser(Long userId, LocalDate snapshotDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if snapshot already exists for this date
        portfolioSnapshotRepository.findByUserAndSnapshotDate(user, snapshotDate)
                .ifPresent(existing -> {
                    log.info("Snapshot already exists for user {} on date {}", userId, snapshotDate);
                    throw new RuntimeException("Snapshot already exists for this date");
                });

        // Get portfolio summary
        PortfolioSummaryDTO summary = assetService.getPortfolioSummaryForUser(userId);

        // Create portfolio snapshot
        PortfolioSnapshot portfolioSnapshot = new PortfolioSnapshot();
        portfolioSnapshot.setUser(user);
        portfolioSnapshot.setSnapshotDate(snapshotDate);
        portfolioSnapshot.setTotalAssets(summary.getTotalValue());
        portfolioSnapshot.setTotalLiabilities(summary.getTotalLiabilities());
        portfolioSnapshot.setNetWorth(summary.getNetWorth());
        portfolioSnapshot.setTotalGains(summary.getTotalGainLoss());

        portfolioSnapshot = portfolioSnapshotRepository.save(portfolioSnapshot);

        // Create asset snapshots
        List<Asset> assets = assetRepository.findByUserOrderByUpdatedAtDesc(user);
        for (Asset asset : assets) {
            AssetSnapshot assetSnapshot = new AssetSnapshot();
            assetSnapshot.setPortfolioSnapshot(portfolioSnapshot);
            assetSnapshot.setAssetId(asset.getId());
            assetSnapshot.setAssetName(asset.getName());
            assetSnapshot.setAssetTypeName(asset.getAssetType().getDisplayName());
            assetSnapshot.setCurrentValue(asset.getCurrentValue());
            assetSnapshot.setGainLoss(asset.getGainLoss());
            assetSnapshot.setCurrency(asset.getCurrency());
            assetSnapshot.setValueInInr(currencyConversionService.convertToINR(
                    asset.getCurrentValue(), asset.getCurrency(), user));
            assetSnapshotRepository.save(assetSnapshot);
        }

        // Create liability snapshots
        List<Liability> liabilities = liabilityRepository.findByUserOrderByUpdatedAtDesc(user);
        for (Liability liability : liabilities) {
            LiabilitySnapshot liabilitySnapshot = new LiabilitySnapshot();
            liabilitySnapshot.setPortfolioSnapshot(portfolioSnapshot);
            liabilitySnapshot.setLiabilityId(liability.getId());
            liabilitySnapshot.setLiabilityName(liability.getName());
            liabilitySnapshot.setLiabilityTypeName(liability.getLiabilityType().getDisplayName());
            liabilitySnapshot.setCurrentBalance(liability.getCurrentBalance());
            liabilitySnapshot.setCurrency(liability.getCurrency());
            liabilitySnapshot.setBalanceInInr(currencyConversionService.convertToINR(
                    liability.getCurrentBalance(), liability.getCurrency(), user));
            liabilitySnapshotRepository.save(liabilitySnapshot);
        }

        log.info("Created portfolio snapshot for user {} on date {} with {} assets and {} liabilities",
                userId, snapshotDate, assets.size(), liabilities.size());

        return portfolioSnapshot;
    }

    /**
     * Create a snapshot for current user
     */
    @Transactional
    public PortfolioSnapshotDTO createSnapshot() {
        User currentUser = getCurrentUser();
        LocalDate today = LocalDate.now();

        PortfolioSnapshot snapshot = createSnapshotForUser(currentUser.getId(), today);
        return convertToDTO(snapshot);
    }

    /**
     * Get all snapshots for current user
     */
    @Transactional(readOnly = true)
    public List<PortfolioSnapshotDTO> getAllSnapshots() {
        User currentUser = getCurrentUser();
        List<PortfolioSnapshot> snapshots = portfolioSnapshotRepository.findByUserOrderBySnapshotDateDesc(currentUser);
        return snapshots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get snapshots within a date range
     */
    @Transactional(readOnly = true)
    public List<PortfolioSnapshotDTO> getSnapshotsByDateRange(LocalDate startDate, LocalDate endDate) {
        User currentUser = getCurrentUser();
        List<PortfolioSnapshot> snapshots = portfolioSnapshotRepository
                .findByUserAndSnapshotDateBetweenOrderBySnapshotDateAsc(currentUser, startDate, endDate);
        return snapshots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get recent snapshots (for charting)
     */
    @Transactional(readOnly = true)
    public PortfolioHistoryDTO getPortfolioHistory(Integer weeks) {
        User currentUser = getCurrentUser();
        LocalDate startDate = LocalDate.now().minusWeeks(weeks != null ? weeks : 12);

        List<PortfolioSnapshot> snapshots = portfolioSnapshotRepository
                .findRecentSnapshots(currentUser, startDate);

        PortfolioHistoryDTO history = new PortfolioHistoryDTO();
        history.setDates(snapshots.stream()
                .map(s -> s.getSnapshotDate().toString())
                .collect(Collectors.toList()));
        history.setTotalAssets(snapshots.stream()
                .map(PortfolioSnapshot::getTotalAssets)
                .collect(Collectors.toList()));
        history.setTotalLiabilities(snapshots.stream()
                .map(PortfolioSnapshot::getTotalLiabilities)
                .collect(Collectors.toList()));
        history.setNetWorth(snapshots.stream()
                .map(PortfolioSnapshot::getNetWorth)
                .collect(Collectors.toList()));
        history.setTotalGains(snapshots.stream()
                .map(PortfolioSnapshot::getTotalGains)
                .collect(Collectors.toList()));

        return history;
    }

    /**
     * Get history for a specific asset
     */
    @Transactional(readOnly = true)
    public PortfolioHistoryDTO getAssetHistory(Long assetId, Integer weeks) {
        User currentUser = getCurrentUser();
        LocalDate startDate = LocalDate.now().minusWeeks(weeks != null ? weeks : 12);

        List<PortfolioSnapshot> portfolioSnapshots = portfolioSnapshotRepository
                .findRecentSnapshots(currentUser, startDate);

        List<AssetSnapshot> assetSnapshots = assetSnapshotRepository
                .findByPortfolioSnapshotsAndAssetId(portfolioSnapshots, assetId);

        return buildAssetHistory(portfolioSnapshots, assetSnapshots);
    }

    /**
     * Get history for a specific asset type
     */
    @Transactional(readOnly = true)
    public PortfolioHistoryDTO getAssetTypeHistory(String assetTypeName, Integer weeks) {
        User currentUser = getCurrentUser();
        LocalDate startDate = LocalDate.now().minusWeeks(weeks != null ? weeks : 12);

        List<PortfolioSnapshot> portfolioSnapshots = portfolioSnapshotRepository
                .findRecentSnapshots(currentUser, startDate);

        List<AssetSnapshot> assetSnapshots = assetSnapshotRepository
                .findByPortfolioSnapshotsAndAssetTypeName(portfolioSnapshots, assetTypeName);

        return buildAssetHistory(portfolioSnapshots, assetSnapshots);
    }

    /**
     * Get history for a specific liability
     */
    @Transactional(readOnly = true)
    public PortfolioHistoryDTO getLiabilityHistory(Long liabilityId, Integer weeks) {
        User currentUser = getCurrentUser();
        LocalDate startDate = LocalDate.now().minusWeeks(weeks != null ? weeks : 12);

        List<PortfolioSnapshot> portfolioSnapshots = portfolioSnapshotRepository
                .findRecentSnapshots(currentUser, startDate);

        List<LiabilitySnapshot> liabilitySnapshots = liabilitySnapshotRepository
                .findByPortfolioSnapshotsAndLiabilityId(portfolioSnapshots, liabilityId);

        return buildLiabilityHistory(portfolioSnapshots, liabilitySnapshots);
    }

    /**
     * Get history for a specific liability type
     */
    @Transactional(readOnly = true)
    public PortfolioHistoryDTO getLiabilityTypeHistory(String liabilityTypeName, Integer weeks) {
        User currentUser = getCurrentUser();
        LocalDate startDate = LocalDate.now().minusWeeks(weeks != null ? weeks : 12);

        List<PortfolioSnapshot> portfolioSnapshots = portfolioSnapshotRepository
                .findRecentSnapshots(currentUser, startDate);

        List<LiabilitySnapshot> liabilitySnapshots = liabilitySnapshotRepository
                .findByPortfolioSnapshotsAndLiabilityTypeName(portfolioSnapshots, liabilityTypeName);

        return buildLiabilityHistory(portfolioSnapshots, liabilitySnapshots);
    }

    private PortfolioHistoryDTO buildAssetHistory(
            List<PortfolioSnapshot> portfolioSnapshots,
            List<AssetSnapshot> assetSnapshots) {

        Map<LocalDate, List<AssetSnapshot>> snapshotsByDate = assetSnapshots.stream()
                .collect(Collectors.groupingBy(a -> a.getPortfolioSnapshot().getSnapshotDate()));

        PortfolioHistoryDTO history = new PortfolioHistoryDTO();
        List<String> dates = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        List<BigDecimal> gains = new ArrayList<>();

        for (PortfolioSnapshot ps : portfolioSnapshots) {
            dates.add(ps.getSnapshotDate().toString());

            List<AssetSnapshot> snapshotsForDate = snapshotsByDate.getOrDefault(
                    ps.getSnapshotDate(), new ArrayList<>());

            BigDecimal totalValue = snapshotsForDate.stream()
                    .map(AssetSnapshot::getValueInInr)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            values.add(totalValue);

            BigDecimal totalGain = snapshotsForDate.stream()
                    .map(a -> a.getGainLoss() != null ?
                        currencyConversionService.convertToINR(a.getGainLoss(), a.getCurrency(),
                            portfolioSnapshots.get(0).getUser()) : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            gains.add(totalGain);
        }

        history.setDates(dates);
        history.setTotalAssets(values);
        history.setTotalGains(gains);
        history.setTotalLiabilities(new ArrayList<>());
        history.setNetWorth(new ArrayList<>());

        return history;
    }

    private PortfolioHistoryDTO buildLiabilityHistory(
            List<PortfolioSnapshot> portfolioSnapshots,
            List<LiabilitySnapshot> liabilitySnapshots) {

        Map<LocalDate, List<LiabilitySnapshot>> snapshotsByDate = liabilitySnapshots.stream()
                .collect(Collectors.groupingBy(l -> l.getPortfolioSnapshot().getSnapshotDate()));

        PortfolioHistoryDTO history = new PortfolioHistoryDTO();
        List<String> dates = new ArrayList<>();
        List<BigDecimal> balances = new ArrayList<>();

        for (PortfolioSnapshot ps : portfolioSnapshots) {
            dates.add(ps.getSnapshotDate().toString());

            List<LiabilitySnapshot> snapshotsForDate = snapshotsByDate.getOrDefault(
                    ps.getSnapshotDate(), new ArrayList<>());

            BigDecimal totalBalance = snapshotsForDate.stream()
                    .map(LiabilitySnapshot::getBalanceInInr)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            balances.add(totalBalance);
        }

        history.setDates(dates);
        history.setTotalLiabilities(balances);
        history.setTotalAssets(new ArrayList<>());
        history.setTotalGains(new ArrayList<>());
        history.setNetWorth(new ArrayList<>());

        return history;
    }

    private PortfolioSnapshotDTO convertToDTO(PortfolioSnapshot snapshot) {
        PortfolioSnapshotDTO dto = new PortfolioSnapshotDTO();
        dto.setId(snapshot.getId());
        dto.setSnapshotDate(snapshot.getSnapshotDate());
        dto.setTotalAssets(snapshot.getTotalAssets());
        dto.setTotalLiabilities(snapshot.getTotalLiabilities());
        dto.setNetWorth(snapshot.getNetWorth());
        dto.setTotalGains(snapshot.getTotalGains());

        // Load asset snapshots
        List<AssetSnapshot> assetSnapshots = assetSnapshotRepository.findByPortfolioSnapshot(snapshot);
        dto.setAssetSnapshots(assetSnapshots.stream()
                .map(this::convertAssetSnapshotToDTO)
                .collect(Collectors.toList()));

        // Load liability snapshots
        List<LiabilitySnapshot> liabilitySnapshots = liabilitySnapshotRepository.findByPortfolioSnapshot(snapshot);
        dto.setLiabilitySnapshots(liabilitySnapshots.stream()
                .map(this::convertLiabilitySnapshotToDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    private AssetSnapshotDTO convertAssetSnapshotToDTO(AssetSnapshot snapshot) {
        AssetSnapshotDTO dto = new AssetSnapshotDTO();
        dto.setId(snapshot.getId());
        dto.setAssetId(snapshot.getAssetId());
        dto.setAssetName(snapshot.getAssetName());
        dto.setAssetTypeName(snapshot.getAssetTypeName());
        dto.setCurrentValue(snapshot.getCurrentValue());
        dto.setGainLoss(snapshot.getGainLoss());
        dto.setCurrency(snapshot.getCurrency());
        dto.setValueInInr(snapshot.getValueInInr());
        return dto;
    }

    private LiabilitySnapshotDTO convertLiabilitySnapshotToDTO(LiabilitySnapshot snapshot) {
        LiabilitySnapshotDTO dto = new LiabilitySnapshotDTO();
        dto.setId(snapshot.getId());
        dto.setLiabilityId(snapshot.getLiabilityId());
        dto.setLiabilityName(snapshot.getLiabilityName());
        dto.setLiabilityTypeName(snapshot.getLiabilityTypeName());
        dto.setCurrentBalance(snapshot.getCurrentBalance());
        dto.setCurrency(snapshot.getCurrency());
        dto.setBalanceInInr(snapshot.getBalanceInInr());
        return dto;
    }
}

