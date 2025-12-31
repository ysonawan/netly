package com.netly.app.service;

import com.netly.app.dto.LiabilityDTO;
import com.netly.app.model.CustomLiabilityType;
import com.netly.app.model.Liability;
import com.netly.app.model.User;
import com.netly.app.repository.CustomLiabilityTypeRepository;
import com.netly.app.repository.LiabilityRepository;
import com.netly.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LiabilityService {

    private final LiabilityRepository liabilityRepository;
    private final UserRepository userRepository;
    private final CustomLiabilityTypeRepository customLiabilityTypeRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<LiabilityDTO> getAllLiabilities() {
        User currentUser = getCurrentUser();
        return getAllLiabilitiesForUser(currentUser.getId());
    }

    /**
     * Get all liabilities for a specific user (used by scheduler)
     */
    @Transactional(readOnly = true)
    public List<LiabilityDTO> getAllLiabilitiesForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return liabilityRepository.findByUserOrderByUpdatedAtDesc(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LiabilityDTO getLiabilityById(Long id) {
        User currentUser = getCurrentUser();
        Liability liability = liabilityRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Liability not found with id: " + id));
        return convertToDTO(liability);
    }

    @Transactional
    public LiabilityDTO createLiability(LiabilityDTO liabilityDTO) {
        User currentUser = getCurrentUser();
        CustomLiabilityType liabilityType = customLiabilityTypeRepository.findById(liabilityDTO.getCustomLiabilityTypeId())
                .orElseThrow(() -> new RuntimeException("Liability type not found with id: " + liabilityDTO.getCustomLiabilityTypeId()));

        if (!liabilityType.getUser().equals(currentUser)) {
            throw new RuntimeException("Liability type does not belong to current user");
        }

        Liability liability = convertToEntity(liabilityDTO, liabilityType);
        liability.setUser(currentUser);
        Liability savedLiability = liabilityRepository.save(liability);
        return convertToDTO(savedLiability);
    }

    @Transactional
    public LiabilityDTO updateLiability(Long id, LiabilityDTO liabilityDTO) {
        User currentUser = getCurrentUser();
        Liability existingLiability = liabilityRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Liability not found with id: " + id));

        CustomLiabilityType liabilityType = customLiabilityTypeRepository.findById(liabilityDTO.getCustomLiabilityTypeId())
                .orElseThrow(() -> new RuntimeException("Liability type not found with id: " + liabilityDTO.getCustomLiabilityTypeId()));

        if (!liabilityType.getUser().equals(currentUser)) {
            throw new RuntimeException("Liability type does not belong to current user");
        }

        existingLiability.setName(liabilityDTO.getName());
        existingLiability.setLiabilityType(liabilityType);
        existingLiability.setCurrentBalance(liabilityDTO.getCurrentBalance());
        existingLiability.setOriginalAmount(liabilityDTO.getOriginalAmount());
        existingLiability.setStartDate(liabilityDTO.getStartDate());
        existingLiability.setEndDate(liabilityDTO.getEndDate());
        existingLiability.setInterestRate(liabilityDTO.getInterestRate());
        existingLiability.setMonthlyPayment(liabilityDTO.getMonthlyPayment());
        existingLiability.setLender(liabilityDTO.getLender());
        existingLiability.setDescription(liabilityDTO.getDescription());

        Liability updatedLiability = liabilityRepository.save(existingLiability);
        return convertToDTO(updatedLiability);
    }

    @Transactional
    public void deleteLiability(Long id) {
        User currentUser = getCurrentUser();
        Liability liability = liabilityRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Liability not found with id: " + id));
        liabilityRepository.delete(liability);
    }

    private LiabilityDTO convertToDTO(Liability liability) {
        LiabilityDTO dto = new LiabilityDTO();
        dto.setId(liability.getId());
        dto.setName(liability.getName());
        dto.setCustomLiabilityTypeId(liability.getLiabilityType().getId());
        dto.setLiabilityTypeName(liability.getLiabilityType().getTypeName());
        dto.setLiabilityTypeDisplayName(liability.getLiabilityType().getDisplayName());
        dto.setCurrentBalance(liability.getCurrentBalance());
        dto.setOriginalAmount(liability.getOriginalAmount());
        dto.setStartDate(liability.getStartDate());
        dto.setEndDate(liability.getEndDate());
        dto.setInterestRate(liability.getInterestRate());
        dto.setMonthlyPayment(liability.getMonthlyPayment());
        dto.setLender(liability.getLender());
        dto.setDescription(liability.getDescription());
        dto.setPaidAmount(liability.getPaidAmount());
        dto.setRepaymentPercentage(liability.getRepaymentPercentage());
        dto.setUpdatedAt(liability.getUpdatedAt());
        return dto;
    }

    private Liability convertToEntity(LiabilityDTO dto, CustomLiabilityType liabilityType) {
        Liability liability = new Liability();
        liability.setName(dto.getName());
        liability.setLiabilityType(liabilityType);
        liability.setCurrentBalance(dto.getCurrentBalance());
        liability.setOriginalAmount(dto.getOriginalAmount());
        liability.setStartDate(dto.getStartDate());
        liability.setEndDate(dto.getEndDate());
        liability.setInterestRate(dto.getInterestRate());
        liability.setMonthlyPayment(dto.getMonthlyPayment());
        liability.setLender(dto.getLender());
        liability.setDescription(dto.getDescription());
        return liability;
    }
}
