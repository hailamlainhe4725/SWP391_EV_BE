package com.example.demo.service;

import com.example.demo.entity.Vote;
import com.example.demo.repository.VoteRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class VoteService {
    private final VoteRepository voteRepository;

    public VoteService(VoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }

    public List<Vote> getAllVotes() {
        return voteRepository.findAll();
    }

    public Optional<Vote> getById(Long id) {
        return voteRepository.findById(id);
    }

    public List<Vote> getVotesByTarget(String type, Long relatedId) {
        return voteRepository.findByRelatedTypeAndRelatedId(type, relatedId);
    }

    public Vote save(Vote vote) {
        return voteRepository.save(vote);
    }

    public void delete(Long id) {
        voteRepository.deleteById(id);
    }
}
