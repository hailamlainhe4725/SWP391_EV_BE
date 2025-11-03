package com.example.demo.service;

import com.example.demo.dto.request.CreateVehicleRequest;
import com.example.demo.dto.request.UpdateVehicleRequest;
import com.example.demo.dto.response.VehicleResponse;
import com.example.demo.entity.Vehicle;
import com.example.demo.enums.VehicleStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public List<VehicleResponse> gettop4() {
        return vehicleRepository.findTop4ByDeletedFalseOrderByVehicleIdAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public VehicleResponse getById(Long id) {
Vehicle vehicle = vehicleRepository.findByIdAndDeletedFalse(id)
    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found or deleted"));

        return mapToResponse(vehicle);
    }

        public VehicleResponse create(CreateVehicleRequest req) {
    try {
        Vehicle v = Vehicle.builder()
                .brand(req.getBrand())
                .model(req.getModel())
                .plateNumber(req.getPlateNumber())
                .color(req.getColor())
                .year(req.getYear())
                .batteryCapacityKwh(req.getBatteryCapacityKwh())
                .seat(req.getSeat())
                .price(req.getPrice())
                .feeChargingPer1PercentUsed(req.getBatteryCapacityKwh() * 2500 * 0.01)
                .feeOverKm(3000.0)
                .operationPerMonthPerShare(req.getPrice() * 0.01 * 0.1)
                .description(req.getDescription())
                .status(req.getStatus())
                .build();

        // --- Xử lý ảnh upload ---
        MultipartFile imageFile = req.getImageFile();
        if (imageFile != null && !imageFile.isEmpty()) {
            String uploadDir = "uploads/vehicles/";
            Files.createDirectories(Paths.get(uploadDir));

            String fileName = "vehicle_" + System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            imageFile.transferTo(filePath);

            v.setImageUrl(filePath.toString());
        }

        vehicleRepository.save(v);
        return mapToResponse(v);
    } catch (Exception e) {
        throw new RuntimeException("Error uploading image: " + e.getMessage());
    }
}



    public VehicleResponse update(Long id, UpdateVehicleRequest req) {
        Vehicle v = vehicleRepository.findByIdAndDeletedFalse(id)
    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found or deleted"));


        if (req.getBrand() != null) v.setBrand(req.getBrand());
        if (req.getModel() != null) v.setModel(req.getModel());
        if (req.getColor() != null) v.setColor(req.getColor());
        if (req.getYear() != null) v.setYear(req.getYear());
        if (req.getDescription() != null) v.setDescription(req.getDescription());
        if (req.getStatus() != null) v.setStatus(req.getStatus());
        if (req.getFeeChargingPer1PercentUsed() != null) v.setFeeChargingPer1PercentUsed(req.getFeeChargingPer1PercentUsed());
        if (req.getFeeOverKm() != null) v.setFeeOverKm(req.getFeeOverKm());
        if (req.getOperationPerMonthPerShare() != null) v.setOperationPerMonthPerShare(req.getOperationPerMonthPerShare());
        MultipartFile imageFile = req.getImageFile();
        try{
        if (imageFile != null && !imageFile.isEmpty()) {
            String uploadDir = "uploads/vehicles/";
            Files.createDirectories(Paths.get(uploadDir));

            String fileName = "vehicle_" + System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            imageFile.transferTo(filePath);

            v.setImageUrl(filePath.toString());
        }
    }catch(Exception e){
        System.out.println("loi o 111 vehicleService");
    }
        
        if(v.getStatus() == VehicleStatus.Unavailable) v.setDeleted(true);
        vehicleRepository.save(v);
        return mapToResponse(v);
    }

    public void softDelete(Long id) {
        Vehicle v = vehicleRepository.findByIdAndDeletedFalse(id)
    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found or deleted"));

        v.setDeleted(true);
        vehicleRepository.save(v);
    }


    // =====================================================
    // 🧱 MAP ENTITY -> RESPONSE
    // =====================================================
    protected VehicleResponse mapToResponse(Vehicle v) {
        return VehicleResponse.builder()
                .vehicleId(v.getVehicleId())
                .brand(v.getBrand())
                .model(v.getModel())
                .plateNumber(v.getPlateNumber())
                .color(v.getColor())
                .year(v.getYear())
                .batteryCapacityKwh(v.getBatteryCapacityKwh())
                .seat(v.getSeat())
                .price(v.getPrice())
                .feeChargingPer1PercentUsed(v.getFeeChargingPer1PercentUsed())
                .feeOverKm(v.getFeeOverKm())
                .operationPerMonthPerShare(v.getOperationPerMonthPerShare())
                .description(v.getDescription())
                .imageUrl(v.getImageUrl())
                .status(v.getStatus())
                .build();
    }
}
