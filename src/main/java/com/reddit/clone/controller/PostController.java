package com.reddit.clone.controller;

import com.reddit.clone.configure.SecurityContext;
import com.reddit.clone.model.Comment;
import com.reddit.clone.model.Post;
import com.reddit.clone.model.Subreddit;
import com.reddit.clone.model.User;
import com.reddit.clone.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/reddit")
@RequiredArgsConstructor
public class PostController {
    public final PostService postService;
    public final SecurityContext securityContext;
    public final  CommentService commentService;
    public final SubredditService subredditService;
    public final S3Service s3Service;
    private final UserService userService;


    @GetMapping("/posts")
    public String getAllPosts(Model model){
        String name = securityContext.getAuthentication().getName();
        User user = userService.findByEmail(name);
        List<Post> posts = postService.getUserJoinedPosts( user.getId());
        List<Subreddit> createdByUser = subredditService.getPostByCreatedBy(user.getId());
        Set<Subreddit> joinedByUser = user.getSubreddits();
        model.addAttribute("createdByUser",createdByUser);
        model.addAttribute("joinedByUser",joinedByUser);
        model.addAttribute("posts",posts);
        model.addAttribute("profilePicture",user.getProfilePicture());
        return "posts";
    }
    @GetMapping("/latest")
    public String getLatest(Model model){
        String name = securityContext.getAuthentication().getName();
        User user = userService.findByEmail(name);
        List<Post> posts = postService.getLatestPosts();
        List<Subreddit> createdByUser = subredditService.getPostByCreatedBy(user.getId());
        List<Subreddit> joinedByUser = subredditService.getSubredditsJoined(user.getId());
        model.addAttribute("createdByUser",createdByUser);
        model.addAttribute("joinedByUser",joinedByUser);
        model.addAttribute("posts",posts);
        model.addAttribute("profilePicture",user.getProfilePicture());
        return "posts";
    }
    @GetMapping("/explore")
    public String getExplorePosts(Model model){
        String name = securityContext.getAuthentication().getName();
        User user = userService.findByEmail(name);
        List<Post> posts = postService.getAllPosts();
        List<Subreddit> createdByUser = subredditService.getPostByCreatedBy(user.getId());
        List<Subreddit> joinedByUser = subredditService.getSubredditsJoined(user.getId());
        model.addAttribute("createdByUser",createdByUser);
        model.addAttribute("joinedByUser",joinedByUser);
        model.addAttribute("posts",posts);
        model.addAttribute("profilePicture",user.getProfilePicture());
        return "posts";
    }
    @GetMapping("/popular")
    public String getPopularPosts(Model model){
        String name = securityContext.getAuthentication().getName();
        User user = userService.findByEmail(name);
        List<Post> posts = postService.getPostsByVotes();
        List<Subreddit> createdByUser = subredditService.getPostByCreatedBy(user.getId());
        List<Subreddit> joinedByUser = subredditService.getSubredditsJoined(user.getId());
        model.addAttribute("createdByUser",createdByUser);
        model.addAttribute("joinedByUser",joinedByUser);
        model.addAttribute("posts",posts);
        model.addAttribute("profilePicture",user.getProfilePicture());
        return "posts";
    }
    @GetMapping("/post")
    public String showPost(Model model){
        String mail = securityContext.getAuthentication().getName();
        User user = userService.findByEmail(mail);
        Set<Subreddit> subreddits = user.getSubreddits();
        model.addAttribute("subreddits",subreddits);
        model.addAttribute("post",new Post());
        return "post";
    }

    @PostMapping("/post")
    public String savePost(@ModelAttribute Post post,
                           @RequestParam(value = "subredditId", required = false) Integer subredditId,
                           @RequestParam("postImage") MultipartFile image,
                           @RequestParam(value = "postVideo") MultipartFile video) {
        postService.save(post, subredditId,image,video);
        return "redirect:/reddit/posts";
    }


    @GetMapping("/post/{postId}")
    public String viewPost(@PathVariable("postId") int postId, Model model) {
        Post post = postService.getPost(postId);
        int countVotes = postService.countVotes(postId);
        List<Comment> comments = post.getComments();//commentService.getCommentsByPost(postId);
        for (Comment comment : comments) {
            List<Comment> childComments = commentService.getRepliesForComment(comment.getId());
            comment.setChildComments(childComments);
        }
        model.addAttribute("userId",userService.getUserByEmail(securityContext.getAuthentication().getName()).getId());
        model.addAttribute("comments", comments);
        model.addAttribute("countedVotes", countVotes);
        model.addAttribute("post", post);
        return "post-view";
    }

    @GetMapping("/edit/{postId}")
    public String showEditPostForm(@PathVariable int postId, Model model) {
        Post post = postService.getPost(postId);
        String mail = securityContext.getAuthentication().getName();
        User user = userService.findByEmail(mail);
        List<Subreddit> subreddits = subredditService.getSubredditsJoined(user.getId());
        model.addAttribute("post", post);
        model.addAttribute("subreddits", subreddits);
        return "update-post";
    }


    @PostMapping("/edit/{postId}")
    public String updatePost(
            @PathVariable int postId,
            @ModelAttribute("post") Post post,
            @RequestParam(value = "subredditId", required = false) Integer subredditId,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "videoFile", required = false) MultipartFile videoFile) {
        try {
            postService.upDatePost(postId, post, imageFile, videoFile,subredditId);
            return "redirect:/reddit/post/" + postId;
        } catch (Exception e) {
            return "redirect:/reddit/post/" + postId;
        }
    }

    @GetMapping("/vote/{postId}")
    public String upVote(@PathVariable("postId") Integer id) {
        postService.upVote(id);
        return "redirect:/reddit/post/" + id;
    }

    @GetMapping("/dvote/{postId}")
    public String downVote(@PathVariable("postId") int id) {
        postService.downVote(id);
        return "redirect:/reddit/post/" + id;
    }

    @PostMapping("/comment/{postId}")
    public String addComment(@RequestParam("comment") String commentInfo,@PathVariable("postId") Integer postId){
        commentService.addComment(postId,commentInfo,securityContext.getAuthentication().getName());
        return "redirect:/reddit/post/" + postId;
    }

    @PostMapping("/reply/{commentId}")
    public String replyToComment(@PathVariable("commentId") int commentId,
                                 @RequestParam("postId") int postId,
                                 @RequestParam("replyContent") String replyContent) {
        postService.replyToComment(postId,commentId,replyContent);
        return "redirect:/reddit/post/" + postId;
    }
    @DeleteMapping("/comment/{commentId}")
    public String  deleteComment(@RequestParam("postId") Integer postId,@PathVariable("commentId") Integer commentId ){
        commentService.deleteComment(commentId);
        return "redirect:/reddit/post/" + postId;
    }
    @DeleteMapping("/post/{postId}")
    public String deletePost(@PathVariable("postId") Integer postId){
        postService.deletePost(postId);
        return "redirect:/reddit/posts";
    }
    @PostMapping("/active/{postId}")
    public String activePost(@PathVariable("postId") Integer postId){
        postService.setActive(postId);
        Post post = postService.getPost(postId);
        return "redirect:/subreddits/details/"+post.getSubreddit().getId();
    }
    @PostMapping("/archive/{postId}")
    public String archivePost(@PathVariable("postId") Integer postId){
        postService.setArchive(postId);
        Post post = postService.getPost(postId);
        return "redirect:/subreddits/details/"+post.getSubreddit().getId();
    }
    @GetMapping("/postArchieve/{postId}")
    public String getArchivePost(@PathVariable("postId") Integer postId,Model model){
        Post post = postService.getPost(postId);
        model.addAttribute("post",post);
        return "publish-post.html";
    }

    private String extractFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}
