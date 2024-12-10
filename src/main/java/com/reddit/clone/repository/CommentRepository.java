package com.reddit.clone.repository;

import com.reddit.clone.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment , Integer> {
    List<Comment> findByPostId(Integer postId);

    List<Comment> findByParentCommentId(Integer commentId);
}
