package com.reddit.clone.controller;

import com.reddit.clone.configure.SecurityContext;
import com.reddit.clone.model.Post;
import com.reddit.clone.model.Subreddit;
import com.reddit.clone.model.User;
import com.reddit.clone.repository.UserRepository;
import com.reddit.clone.service.PostService;
import com.reddit.clone.service.SubredditService;
import com.reddit.clone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/subreddits")
@RequiredArgsConstructor
public class SubredditController {
    private final SubredditService subredditService;
    private final SecurityContext securityContext;
    private final UserRepository userRepository;
    private final PostService postService;
    private final UserService userService;

    @GetMapping
    public String getAllSubreddits(Model model) {
        List<Subreddit> subreddits = subredditService.getAllSubreddits();
        model.addAttribute("subreddits", subreddits);
        String email = securityContext.getAuthentication().getName();
        User user = userRepository.findByEmail(email);
        model.addAttribute("username",user.getUsername());
        return "view-subreddits";
    }

    @GetMapping("/new")
    public String createSubredditForm(Model model) {
        model.addAttribute("subreddit", new Subreddit());
        return "save-subreddit";
    }

    @PostMapping
    public String saveSubreddit(@ModelAttribute Subreddit subreddit, @RequestParam String genre) {
        Set<String> genres = Stream.of(genre.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        subreddit.setGenre(genres);
        subredditService.saveSubreddit(subreddit);
        return "redirect:/reddit/posts";
    }

    @GetMapping("/edit/{id}")
    public String editSubredditForm(@PathVariable int id, Model model) {
        Subreddit subreddit = subredditService.getSubredditById(id);
        model.addAttribute("subreddit", subreddit);
        return "update-subreddit";
    }

    @PostMapping("/{id}")
    public String updateSubreddit(@PathVariable int id, @ModelAttribute Subreddit subreddit, @RequestParam String genre) {
        Set<String> genres = Stream.of(genre.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        subreddit.setGenre(genres);
        subredditService.updateSubreddit(id, subreddit);
        return "redirect:/subreddits/details/"+ id;
    }

    @GetMapping("/search")
    public String searchFunction(@RequestParam("query") String query,Model model){
        List<Subreddit> subreddits = subredditService.getSearch(query);
        List<Post> posts = postService.getSearch(query);
        List<User> users = userService.getSearch(query);
        model.addAttribute("keyword",query);
        model.addAttribute("subreddits", subreddits);
        model.addAttribute("posts", posts);
        model.addAttribute("users", users);
        return "search-results";
    }
    @GetMapping("/{id}/delete")
    public String deleteSubreddit(@PathVariable int id) {
        subredditService.deleteSubreddit(id);
        return "redirect:/reddit/posts";
    }

    @PostMapping("/join/{id}")
    public String joinSubreddit(@PathVariable Integer id) {
        subredditService.joinSubreddit(id);
        return "redirect:/subreddits/details/"+id;
    }
    @GetMapping("/{id}/users")
    public String viewSubredditUsers(@PathVariable Integer id, Model model) {
        Subreddit subreddit = subredditService.getSubredditById(id);
        Set<User> users = subreddit.getUsers();

        model.addAttribute("subreddit", subreddit);
        model.addAttribute("users", users);
        return "view-users-in-subreddit";
    }

    @GetMapping("/user-subreddits")
    public String getUserSubreddits(Model model) {
        String email = securityContext.getAuthentication().getName();
        User user = userRepository.findByEmail(email);
        Set<Subreddit> userSubreddits = user.getSubreddits();
        model.addAttribute("subreddits", userSubreddits);
        model.addAttribute("username", user.getUsername());
        return "view-subreddits-of-user";
    }
    @PostMapping("/exit/{subredditId}")
    public String exitFromReddit(@PathVariable("subredditId") Integer subredditId){
        User user = userService.getUserByEmail(securityContext.getAuthentication().getName());
        Subreddit subreddit = subredditService.getSubredditById(subredditId);
        user.exitFromSubreddit(subreddit);
        userService.save(user);
        return "redirect:/reddit/posts";
    }
    @GetMapping("/details/{id}")
    public String getSubredditDetails(@PathVariable Integer id, Model model) {
        Subreddit subreddit = subredditService.getSubredditById(Math.toIntExact(id));
        List<Post> posts = postService.getPostsBySubId(id);
        if (subreddit != null) {
            User user = userService.getUserByEmail(securityContext.getAuthentication().getName());
            boolean isJoined =  user.getSubreddits().contains(subreddit);
            model.addAttribute("isJoined",isJoined);
            model.addAttribute("subreddit", subreddit);
            model.addAttribute("posts", new ArrayList<>(posts));
        }
        return "subreddit-details";
    }
    @GetMapping("/control/{id}")
    public String archivedPosts(@PathVariable("id") Integer subredditId,Model model){
        System.out.println(subredditId);
        List<Post> posts = postService.getSubredditsByIsPublished(subredditId);
        model.addAttribute("subredditAttribute",subredditId);
        model.addAttribute("posts",posts);
        return "subreddit-posts";
    }
}
