package com.reddit.clone.repository;

import com.reddit.clone.model.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubredditRepository extends JpaRepository<Subreddit, Integer> {
    @Query(value = "SELECT * FROM subreddits WHERE LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%'))", nativeQuery = true)
    List<Subreddit> searchByTitle(@Param("keyword") String keyword);

    @Query(value = "SELECT * FROM subreddits WHERE created_by = :userId", nativeQuery = true)
    List<Subreddit> findAllByCreatedBy(@Param("userId") Integer userId);

    @Query(value = "SELECT s.* " +
            "FROM subreddits s " +
            "JOIN user_subreddits us ON us.subreddit_id = s.id " +
            "WHERE us.user_id = :userId", nativeQuery = true)
    List<Subreddit> findSubredditsByUserId(@Param("userId") Long userId);
}
