package com.reddit.clone.service;

import com.reddit.clone.configure.SecurityContext;
import com.reddit.clone.model.Subreddit;
import com.reddit.clone.model.User;
import com.reddit.clone.repository.SubredditRepository;
import com.reddit.clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubredditService {
    private final SubredditRepository subredditRepository;
    private final SecurityContext securityContext;
    private final UserRepository userRepository;

    public List<Subreddit> getAllSubreddits() {
        return subredditRepository.findAll();
    }

    @Transactional
    public Subreddit getSubredditById(int id) {
        return subredditRepository.findById(id).orElse(null);
    }

    public void saveSubreddit(Subreddit subreddit) {
        subreddit.setCreatedAt(Timestamp.from(Instant.now()));
        String email = securityContext.getAuthentication().getName();
        User user = userRepository.findByEmail(email);
        subreddit.setCreatedBy(user);
        user.addSubreddit(subreddit);
        subredditRepository.save(subreddit);
    }

    public void updateSubreddit(int id, Subreddit updatedSubreddit) {
        Subreddit subreddit = subredditRepository.findById(id).orElse(null);
        if (subreddit != null) {
            subreddit.setName(updatedSubreddit.getName());
            subreddit.setDescription(updatedSubreddit.getDescription());
            subreddit.setGenre(updatedSubreddit.getGenre());
            String email = securityContext.getAuthentication().getName();
            User user = userRepository.findByEmail(email);
            subreddit.setCreatedBy(user);
            subredditRepository.save(subreddit);
        }
    }

    public void deleteSubreddit(int id) {
        subredditRepository.deleteById(id);
    }

    public void joinSubreddit(Integer id) {
        Optional<Subreddit> optionalSubreddit = subredditRepository.findById(id);
        Subreddit subreddit = optionalSubreddit.get();
        String email = securityContext.getAuthentication().getName();
        User user = userRepository.findByEmail(email);
        user.addSubreddit(subreddit);
        userRepository.save(user);
    }

    public List<Subreddit> getSearch(String keyword){
        return subredditRepository.searchByTitle(keyword);
    }
    public List<Subreddit> getPostByCreatedBy(int userId){
        return subredditRepository.findAllByCreatedBy(userId);
    }
    public List<Subreddit> getSubredditsJoined(int userId){
        return subredditRepository.findSubredditsByUserId(Long.valueOf(userId));
    }
}
