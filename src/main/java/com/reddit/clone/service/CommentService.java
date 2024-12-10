package com.reddit.clone.service;

import com.reddit.clone.model.Comment;
import com.reddit.clone.model.Post;
import com.reddit.clone.repository.CommentRepository;
import com.reddit.clone.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public Comment addComment(int postId, String content, String createdBy) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setCreatedBy(createdBy);

        return commentRepository.save(comment);
    }

    public Comment replyToComment(int parentCommentId, String content, String createdBy) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));

        Comment reply = new Comment();
        reply.setContent(content);
        reply.setParentComment(parentComment);
        reply.setPost(parentComment.getPost());
        reply.setCreatedBy(createdBy);

        return commentRepository.save(reply);
    }

    public Comment getCommentById(int commentId) {
        return commentRepository.findById(commentId).get();
    }

    public void saveComment(Comment replyComment) {
        commentRepository.save(replyComment);
    }
    public List<Comment> getCommentsByPost(Integer postId) {
        return commentRepository.findByPostId(postId);
    }

    public List<Comment> getRepliesForComment(Integer commentId) {
        return commentRepository.findByParentCommentId(commentId);
    }

    public void deleteComment(Integer commentId) {
        commentRepository.deleteById(commentId);
    }
}
