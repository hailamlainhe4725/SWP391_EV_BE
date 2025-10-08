package com.example.demo.service;

import com.example.demo.entity.VariableFee;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.entity.FixedFee;
import com.example.demo.repository.FeeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeeService {
    private final FeeRepository feeRepository;

    public VariableFee create(VariableFee fee) {
        return feeRepository.save(fee);
    }

    public List<VariableFee> getAll() {
        return feeRepository.findAll();
    }

    public VariableFee getById(Integer id) {
        return feeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee not found"));
    }
}
