package com.reddit.clone.repository;

import com.reddit.clone.model.Post;
import com.reddit.clone.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote , Integer> {
    Vote findByPost(Post post);
}
