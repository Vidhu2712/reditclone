package com.reddit.clone.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 50)
    private String roles;

    @Setter
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "profile_picture",nullable = true)
    private String profilePicture;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_subreddits",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "subreddit_id")
    )
    private Set<Subreddit> subreddits = new HashSet<>();

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private Set<Post> posts = new HashSet<>();

    public void addSubreddit(Subreddit subreddit) {
        if (subreddits == null) {
            subreddits = new HashSet<>();
        }
        subreddits.add(subreddit);
    }
    public void exitFromSubreddit(Subreddit subreddit) {
        if (subreddits.contains(subreddit)) {
            subreddits.remove(subreddit);
        } else {
            System.out.println("Subreddit not found in the collection.");
        }
    }

}
