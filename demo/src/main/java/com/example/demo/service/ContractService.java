package com.example.demo.service;

import com.example.demo.dto.request.CreateContractRequest;
import com.example.demo.dto.response.ContractResponse;
import com.example.demo.entity.Contract;
import com.example.demo.entity.User;
import com.example.demo.repository.ContractRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;

    public List<ContractResponse> getByUserEmail(String email) {
        return contractRepository.findByUser_EmailAndDeletedFalse(email)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ContractResponse create(CreateContractRequest req) {
        Contract contract = new Contract();
        contract.setDescription(req.getDescription());
        contract.setStartDate(req.getStartDate());
        contract.setEndDate(req.getEndDate());
        contract.setDeleted(false);

        if (req.getUserId() != null) {
            User user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            contract.setUser(user);
        }

        contractRepository.save(contract);
        return toResponse(contract);
    }

    public ContractResponse update(Long id, CreateContractRequest req) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        contract.setDescription(req.getDescription());
        contract.setStartDate(req.getStartDate());
        contract.setEndDate(req.getEndDate());

        if (req.getUserId() != null) {
            User user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            contract.setUser(user);
        }

        contractRepository.save(contract);
        return toResponse(contract);
    }

    public void softDelete(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        contract.setDeleted(true);
        contractRepository.save(contract);
    }

    private ContractResponse toResponse(Contract c) {
        ContractResponse res = new ContractResponse();
        res.setId(c.getContractId());
        res.setDescription(c.getDescription());
        res.setStartDate(c.getStartDate());
        res.setEndDate(c.getEndDate());
        res.setUserEmail(c.getUser() != null ? c.getUser().getEmail() : null);
        return res;
    }
}
