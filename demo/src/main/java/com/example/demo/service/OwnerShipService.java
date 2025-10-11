package com.example.demo.service;

import com.example.demo.dto.response.OwnershipResponse;
import com.example.demo.entity.Ownership;
import com.example.demo.repository.OwnershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnershipService {

    private final OwnershipRepository ownershipRepository;

    public List<OwnershipResponse> getAll() {
        return ownershipRepository.findAll()
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OwnershipResponse> getByUserEmail(String email) {
        return ownershipRepository.findByUser_Email(email)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OwnershipResponse mapToResponse(Ownership o) {
        return OwnershipResponse.builder()
                .ownershipId(o.getOwnershipId())
                .userName(o.getUser().getFullName())
                .vehicleName(o.getVehicle().getBrand() + " " + o.getVehicle().getModel())
                .totalSharePercentage(o.getTotalSharePercentage())
                .status(o.getStatus().name())
                .createdAt(o.getCreatedAt())
                .build();
    }
}
