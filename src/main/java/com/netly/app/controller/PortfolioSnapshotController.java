package com.netly.app.controller;

import com.netly.app.dto.PortfolioHistoryDTO;
import com.netly.app.dto.PortfolioSnapshotDTO;
import com.netly.app.service.PortfolioSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/portfolio-snapshots")
@RequiredArgsConstructor
public class PortfolioSnapshotController {

    private final PortfolioSnapshotService portfolioSnapshotService;

    /**
     * Create a snapshot manually (for current user)
     */
    @PostMapping
    public ResponseEntity<PortfolioSnapshotDTO> createSnapshot() {
        try {
            PortfolioSnapshotDTO snapshot = portfolioSnapshotService.createSnapshot();
            return ResponseEntity.ok(snapshot);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all snapshots for current user
     */
    @GetMapping
    public ResponseEntity<List<PortfolioSnapshotDTO>> getAllSnapshots() {
        List<PortfolioSnapshotDTO> snapshots = portfolioSnapshotService.getAllSnapshots();
        return ResponseEntity.ok(snapshots);
    }

    /**
     * Get snapshots within date range
     */
    @GetMapping("/range")
    public ResponseEntity<List<PortfolioSnapshotDTO>> getSnapshotsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<PortfolioSnapshotDTO> snapshots = portfolioSnapshotService.getSnapshotsByDateRange(start, end);
        return ResponseEntity.ok(snapshots);
    }

    /**
     * Get portfolio history for charting (overview)
     */
    @GetMapping("/history")
    public ResponseEntity<PortfolioHistoryDTO> getPortfolioHistory(
            @RequestParam(required = false, defaultValue = "12") Integer weeks) {
        PortfolioHistoryDTO history = portfolioSnapshotService.getPortfolioHistory(weeks);
        return ResponseEntity.ok(history);
    }

    /**
     * Get history for a specific asset
     */
    @GetMapping("/history/asset/{assetId}")
    public ResponseEntity<PortfolioHistoryDTO> getAssetHistory(
            @PathVariable Long assetId,
            @RequestParam(required = false, defaultValue = "12") Integer weeks) {
        PortfolioHistoryDTO history = portfolioSnapshotService.getAssetHistory(assetId, weeks);
        return ResponseEntity.ok(history);
    }

    /**
     * Get history for a specific asset type
     */
    @GetMapping("/history/asset-type/{assetTypeName}")
    public ResponseEntity<PortfolioHistoryDTO> getAssetTypeHistory(
            @PathVariable String assetTypeName,
            @RequestParam(required = false, defaultValue = "12") Integer weeks) {
        PortfolioHistoryDTO history = portfolioSnapshotService.getAssetTypeHistory(assetTypeName, weeks);
        return ResponseEntity.ok(history);
    }

    /**
     * Get history for a specific liability
     */
    @GetMapping("/history/liability/{liabilityId}")
    public ResponseEntity<PortfolioHistoryDTO> getLiabilityHistory(
            @PathVariable Long liabilityId,
            @RequestParam(required = false, defaultValue = "12") Integer weeks) {
        PortfolioHistoryDTO history = portfolioSnapshotService.getLiabilityHistory(liabilityId, weeks);
        return ResponseEntity.ok(history);
    }

    /**
     * Get history for a specific liability type
     */
    @GetMapping("/history/liability-type/{liabilityTypeName}")
    public ResponseEntity<PortfolioHistoryDTO> getLiabilityTypeHistory(
            @PathVariable String liabilityTypeName,
            @RequestParam(required = false, defaultValue = "12") Integer weeks) {
        PortfolioHistoryDTO history = portfolioSnapshotService.getLiabilityTypeHistory(liabilityTypeName, weeks);
        return ResponseEntity.ok(history);
    }
}

