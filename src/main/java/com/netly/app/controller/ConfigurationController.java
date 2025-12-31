package com.netly.app.controller;

import com.netly.app.dto.CustomAssetTypeDTO;
import com.netly.app.dto.CustomLiabilityTypeDTO;
import com.netly.app.service.ConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configuration")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ConfigurationController {

    private final ConfigurationService configurationService;

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }


    // Custom Asset Type Endpoints
    @GetMapping("/custom-asset-types")
    public ResponseEntity<List<CustomAssetTypeDTO>> getAllCustomAssetTypes() {
        return ResponseEntity.ok(configurationService.getAllCustomAssetTypes());
    }

    @PostMapping("/custom-asset-types")
    public ResponseEntity<CustomAssetTypeDTO> saveCustomAssetType(@RequestBody CustomAssetTypeDTO dto) {
        return ResponseEntity.ok(configurationService.saveCustomAssetType(dto));
    }

    @DeleteMapping("/custom-asset-types/{id}")
    public ResponseEntity<Void> deleteCustomAssetType(@PathVariable Long id) {
        configurationService.deleteCustomAssetType(id);
        return ResponseEntity.ok().build();
    }

    // Custom Liability Type Endpoints
    @GetMapping("/custom-liability-types")
    public ResponseEntity<List<CustomLiabilityTypeDTO>> getAllCustomLiabilityTypes() {
        return ResponseEntity.ok(configurationService.getAllCustomLiabilityTypes());
    }

    @PostMapping("/custom-liability-types")
    public ResponseEntity<CustomLiabilityTypeDTO> saveCustomLiabilityType(@RequestBody CustomLiabilityTypeDTO dto) {
        return ResponseEntity.ok(configurationService.saveCustomLiabilityType(dto));
    }

    @DeleteMapping("/custom-liability-types/{id}")
    public ResponseEntity<Void> deleteCustomLiabilityType(@PathVariable Long id) {
        configurationService.deleteCustomLiabilityType(id);
        return ResponseEntity.ok().build();
    }
}
