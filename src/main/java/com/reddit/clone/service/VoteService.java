package com.reddit.clone.service;

import com.reddit.clone.model.Vote;
import com.reddit.clone.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class VoteService {
    public final VoteRepository voteRepository;

    public Vote getVote(Integer id){
        return voteRepository.findById(id).get();
    }
    public void save(Vote vote) {
        voteRepository.save(vote);
    }
}
