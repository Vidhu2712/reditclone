package com.reddit.clone.repository;

import com.reddit.clone.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User , Integer> {
    User findByUsername(String username);

    User findByEmail(String username);
    @Query(value = "SELECT * FROM users WHERE LOWER(username) LIKE LOWER(CONCAT('%', :keyword, '%'))", nativeQuery = true)
    List<User> searchByUsername(@Param("keyword") String keyword);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_subreddits (user_id, subreddit_id) VALUES (:userId, :subredditId)", nativeQuery = true)
    void insertUserSubreddit(@Param("userId") int userId, @Param("subredditId") int subredditId);
}
