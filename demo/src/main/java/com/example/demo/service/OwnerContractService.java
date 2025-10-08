package com.example.demo.service;

import com.example.demo.entity.OwnerContract;
import com.example.demo.repository.OwnerContractRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class OwnerContractService {
    private final OwnerContractRepository ownerContractRepository;

    public OwnerContractService(OwnerContractRepository ownerContractRepository) {
        this.ownerContractRepository = ownerContractRepository;
    }

    public List<OwnerContract> getAllOwnerContracts() {
        return ownerContractRepository.findAll();
    }

    public Optional<OwnerContract> getById(Long id) {
        return ownerContractRepository.findById(id);
    }

    public OwnerContract save(OwnerContract ownerContract) {
        return ownerContractRepository.save(ownerContract);
    }

    public void delete(Long id) {
        ownerContractRepository.deleteById(id);
    }
}
