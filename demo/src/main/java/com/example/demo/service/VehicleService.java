package com.example.demo.service;

import com.example.demo.dto.request.CreateVehicleRequest;
import com.example.demo.dto.request.UpdateVehicleRequest;
import com.example.demo.dto.response.VehicleResponse;
import com.example.demo.entity.Vehicle;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public List<VehicleResponse> getAll() {
        return vehicleRepository.findAll().stream()
                .filter(v -> !Boolean.TRUE.equals(v.isDeleted()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public VehicleResponse getById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        return mapToResponse(vehicle);
    }

    public VehicleResponse create(CreateVehicleRequest req) {
        Vehicle v = Vehicle.builder()
                .brand(req.getBrand())
                .model(req.getModel())
                .plateNumber(req.getPlateNumber())
                .color(req.getColor())
                .year(req.getYear())
                .batteryCapacityKwh(req.getBatteryCapacityKwh())
                .operatingCostPerDay(req.getOperatingCostPerDay())
                .operatingCostPerKm(req.getOperatingCostPerKm())
                .description(req.getDescription())
                .imageUrl(req.getImageUrl())
                .status(req.getVehicleStatus())
                .build();
        vehicleRepository.save(v);
        return mapToResponse(v);
    }

    public VehicleResponse update(Long id, UpdateVehicleRequest req) {
        Vehicle v = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        if (req.getBrand() != null)
            v.setBrand(req.getBrand());
        if (req.getModel() != null)
            v.setModel(req.getModel());
        if (req.getColor() != null)
            v.setColor(req.getColor());
        if (req.getYear() != null)
            v.setYear(req.getYear());
        if (req.getOperatingCostPerDay() != null)
            v.setOperatingCostPerDay(req.getOperatingCostPerDay());
        if (req.getOperatingCostPerKm() != null)
            v.setOperatingCostPerKm(req.getOperatingCostPerKm());
        if (req.getDescription() != null)
            v.setDescription(req.getDescription());
        if (req.getStatus() != null)
            v.setStatus(req.getStatus());

        vehicleRepository.save(v);
        return mapToResponse(v);
    }

    public void softDelete(Long id) {
        Vehicle v = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        v.setDeleted(true);
        vehicleRepository.save(v);
    }

    private VehicleResponse mapToResponse(Vehicle v) {
        return VehicleResponse.builder()
                .vehicleId(v.getVehicleId())
                .brand(v.getBrand())
                .model(v.getModel())
                .plateNumber(v.getPlateNumber())
                .color(v.getColor())
                .year(v.getYear())
                .batteryCapacityKwh(v.getBatteryCapacityKwh())
                .operatingCostPerDay(v.getOperatingCostPerDay())
                .operatingCostPerKm(v.getOperatingCostPerKm())
                .description(v.getDescription())
                .imageUrl(v.getImageUrl())
                .status(v.getStatus())
                .build();
    }
}
