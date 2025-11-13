package com.netly.app.controller;

import com.netly.app.dto.LiabilityDTO;
import com.netly.app.service.LiabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/liabilities")
@RequiredArgsConstructor
public class LiabilityController {

    private final LiabilityService liabilityService;

    @GetMapping
    public ResponseEntity<List<LiabilityDTO>> getAllLiabilities() {
        return ResponseEntity.ok(liabilityService.getAllLiabilities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LiabilityDTO> getLiabilityById(@PathVariable Long id) {
        return ResponseEntity.ok(liabilityService.getLiabilityById(id));
    }

    @PostMapping
    public ResponseEntity<LiabilityDTO> createLiability(@RequestBody LiabilityDTO liabilityDTO) {
        LiabilityDTO createdLiability = liabilityService.createLiability(liabilityDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLiability);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LiabilityDTO> updateLiability(@PathVariable Long id, @RequestBody LiabilityDTO liabilityDTO) {
        return ResponseEntity.ok(liabilityService.updateLiability(id, liabilityDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLiability(@PathVariable Long id) {
        liabilityService.deleteLiability(id);
        return ResponseEntity.noContent().build();
    }
}

