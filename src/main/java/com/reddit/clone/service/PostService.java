package com.reddit.clone.service;

import com.reddit.clone.configure.SecurityContext;
import com.reddit.clone.model.*;
import com.reddit.clone.repository.PostRepository;
import com.reddit.clone.repository.UserRepository;
import com.reddit.clone.repository.VoteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    public final S3Service s3Service;
    public final UserRepository userRepository;
    public final SecurityContext securityContext;
    public final PostRepository postRepository;
    public final VoteRepository voteRepository;
    public final VoteService voteService;
    public final CommentService commentService;
    private final SubredditService subredditService;

    @Autowired
    public PostService(CommentService commentService, PostRepository postRepository, S3Service s3Service, SecurityContext securityContext, UserRepository userRepository, VoteRepository voteRepository, VoteService voteService, SubredditService subredditService) {
        this.commentService = commentService;
        this.postRepository = postRepository;
        this.s3Service = s3Service;
        this.securityContext = securityContext;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.voteService = voteService;
        this.subredditService = subredditService;
    }


    @Transactional
    public void save(Post post, Integer subRedditId, MultipartFile image,MultipartFile video) {
        User user = userRepository.findByEmail(securityContext.getAuthentication().getName());
        if (!image.isEmpty()) {
            String imageUrl = s3Service.uploadFile(image);
            post.setImage(imageUrl);
        }
        if (video != null && !video.isEmpty()) {
            String videoUrl = s3Service.uploadFile(video);
            post.setVideo(videoUrl);
        }
        post.setCreatedBy(user);
        post.setPublished(true);
        if(subRedditId != null){
            Subreddit subreddit = subredditService.getSubredditById(subRedditId);
            post.setSubreddit(subreddit);
            post.setPublished(false);
        }
        postRepository.save(post);
    }
    public Post getPost(Integer id){
        return postRepository.findById(id).get();
    }

    @Transactional
    public List<Post> getAllPosts() {
        List<Post> posts = postRepository.findPublishedPosts();
        return posts;
    }

    public int countVotes(int postId) {
        List<Vote> votes = getPost(postId).getVotes();
        int countVotes = 0;
        for (Vote vote : votes) {
           countVotes += vote.isVoteType() ? 1 :-1;
        }
        return countVotes;
    }

    public void upVote(Integer postId) {
        Post post = getPost(postId);
        List<Vote> votes = post.getVotes();
        Optional<Vote> voteOptional = votes.stream()
                .filter(vote -> vote.getUserId()==
                        userRepository.findByEmail(securityContext.getAuthentication().getName()).getId())
                .findFirst();
        if (voteOptional.isPresent()) {
            Vote vote = voteOptional.get();
            if (!vote.isVoteType()) {
                vote.setVoteType(true);
                voteService.save(vote);
            } else {
                post.removeVote(vote);
                postRepository.save(post);
            }
        } else {
            Vote vote = new Vote();
            vote.setVoteType(true);
            vote.setUserId(userRepository.findByEmail(securityContext.getAuthentication().getName()).getId());
            post.addVote(vote);
            postRepository.save(post);
        }
    }

    public void downVote(int postId) {
        Post post = getPost(postId);
        List<Vote> votes = post.getVotes();
        Optional<Vote> voteOptional = votes.stream()
                .filter(vote -> vote.getUserId()==
                        userRepository.findByEmail(securityContext.getAuthentication().getName()).getId())
                .findFirst();
        if (voteOptional.isPresent()) {
            Vote vote = voteOptional.get();
            if (vote.isVoteType()) {
                vote.setVoteType(false);
                voteService.save(vote);
            } else {
                post.removeVote(vote);
                postRepository.save(post);
            }
        } else {
            Vote vote = new Vote();
            vote.setVoteType(false);
            vote.setUserId(userRepository.findByEmail(securityContext.getAuthentication().getName()).getId());
            post.addVote(vote);
            postRepository.save(post);
        }
    }

    public void replyToComment(int postId, int commentId, String replyContent) {
        Post post = getPost(postId);
        Comment parentComment = commentService.getCommentById(commentId);

        Comment replyComment = new Comment();
        replyComment.setContent(replyContent);
        replyComment.setPost(post);
        replyComment.setParentComment(parentComment);
        replyComment.setCreatedBy(securityContext.getAuthentication().getName());
        replyComment.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        commentService.saveComment(replyComment);
    }
    public Post upDatePost(Integer postId,Post post,MultipartFile imageFile,MultipartFile videoFile,Integer subredditId) {

        Post existingPost = postRepository.findById(postId).get();

        if (imageFile != null && !imageFile.isEmpty()) {
            String oldImageFileName = extractFileNameFromUrl(existingPost.getImage());
            s3Service.deleteFile(oldImageFileName);
            String newImageUrl = s3Service.uploadFile(imageFile);
            existingPost.setImage(newImageUrl);
        }

        if (videoFile != null && !videoFile.isEmpty()) {
            if (existingPost.getVideo() != null) {
                String oldVideoFileName = extractFileNameFromUrl(existingPost.getVideo());
                s3Service.deleteFile(oldVideoFileName);
            }
            String newVideoUrl = s3Service.uploadFile(videoFile);
            existingPost.setVideo(newVideoUrl);
        }

        if(subredditId != null){
            Subreddit subreddit = subredditService.getSubredditById(subredditId);
            existingPost.setSubreddit(subreddit);
        }

        existingPost.setContent(post.getContent());
        existingPost.setTitle(post.getTitle());
        return postRepository.save(existingPost);
    }


    public void deletePost(Integer postId) {
        Post post = postRepository.findById(postId).get();
        if (post.getImage() != null) {
            s3Service.deleteFile(extractFileNameFromUrl(post.getImage()));
        }
        if (post.getVideo() != null) {
            s3Service.deleteFile(extractFileNameFromUrl(post.getVideo()));
        }
        postRepository.deleteById(postId);
    }

    public List<Post> getSearch(String query){
        return postRepository.searchByTitle(query);
    }
    public List<Post> getPostsBySubId(int id){
        return postRepository.findPostsBySubredditId(id);
    }

    public List<Post> getUserJoinedPosts(Integer userId){
        return postRepository.findPostsByUser(userId);
    }
    public List<Post> getPostsByVotes(){
        return postRepository.findPostsOrderedByVotes();
    }
    public List<Post> getLatestPosts(){
        return postRepository.findPostsOrderedByCreatedAt();
    }

    public List<Post> getSubredditsByIsPublished(Integer subredditId) {
        return postRepository.findPostsBySubredditIdAndNotPublished(subredditId);
    }

    public void setActive(Integer postId) {
        Post post = getPost(postId);
        post.setPublished(true);
        postRepository.save(post);
    }

    private String extractFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public void setArchive(Integer postId) {
        Post post = getPost(postId);
        post.setPublished(false);
        postRepository.save(post);
    }
}
