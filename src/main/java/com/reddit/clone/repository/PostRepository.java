package com.reddit.clone.repository;
import com.reddit.clone.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PostRepository extends JpaRepository<Post , Integer> {
    @Query(value = "SELECT * FROM posts WHERE LOWER(title) LIKE LOWER(CONCAT('%', :keyword, '%'))", nativeQuery = true)
    List<Post> searchByTitle(@Param("keyword") String keyword);

    @Query(value = "SELECT * FROM posts WHERE subreddit_id = :subredditId AND is_published = true", nativeQuery = true)
    List<Post> findPostsBySubredditId(@Param("subredditId") int subredditId);

    @Query(value = "SELECT * FROM posts WHERE subreddit_id = :subredditId AND is_published = false", nativeQuery = true)
    List<Post> findPostsBySubredditIdAndNotPublished(@Param("subredditId") int subredditId);

    @Query(value = "SELECT p.* FROM posts p JOIN user_subreddits ur ON ur.subreddit_id = p.subreddit_id WHERE ur.user_id = :userId", nativeQuery = true)
    List<Post> findPostsByUserJoinedSubreddits(@Param("userId") Long userId);

    @Query(value = "SELECT p.* " +
            "FROM posts p " +
            "LEFT JOIN user_subreddits ur ON ur.subreddit_id = p.subreddit_id AND ur.user_id = :userId " +
            "WHERE (p.created_by = :userId OR ur.user_id = :userId) AND p.is_published = true", nativeQuery = true)
    List<Post> findPostsByUser(@Param("userId") Integer userId);


    @Query(value = "SELECT p.*, COUNT(v.id) AS vote_count FROM posts p LEFT JOIN votes v ON v.post_id = p.id GROUP BY p.id ORDER BY vote_count DESC", nativeQuery = true)
    List<Post> findPostsOrderedByVotess();

    @Query(value = "SELECT p.* " +
            "FROM posts p " +
            "LEFT JOIN votes v ON v.post_id = p.id " +
            "WHERE p.is_published = true " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(v.id) DESC", nativeQuery = true)
    List<Post> findPostsOrderedByVotes();

    @Query(value = "SELECT * FROM posts WHERE is_published = true ORDER BY created_at DESC", nativeQuery = true)
    List<Post> findPostsOrderedByCreatedAt();


    @Query(value = "SELECT * FROM posts WHERE is_published = true", nativeQuery = true)
    List<Post> findPublishedPosts();

}
