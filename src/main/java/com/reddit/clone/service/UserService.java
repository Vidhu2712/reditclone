package com.reddit.clone.service;

import com.reddit.clone.model.User;
import com.reddit.clone.repository.UserRepository;
import com.reddit.clone.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    public final UserRepository userRepository;

    public void logout(HttpServletRequest request, HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Authorization".equals(cookie.getName())) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

    public boolean login(String username, String password, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        if (authentication.isAuthenticated()) {
            String token = jwtService.generateToken(authentication.getName());
            Cookie cookie = new Cookie("Authorization", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge( 60*10);
            response.addCookie(cookie);
            return true;
        } else {
            return false;
        }
    }

    public boolean register(User user, MultipartFile profilePicture) {
        User isUserExist = userRepository.findByEmail(user.getEmail());
        if (isUserExist == null) {
            String url = s3Service.uploadFile(profilePicture);
            user.setProfilePicture(url);
            user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            user.setRoles("USER");
            String password = user.getPassword();
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            return true;
        } else {
            return false;
        }
    }
    public List<User> getSearch(String query){
         return userRepository.searchByUsername(query);
    }

    public User findById(int id){
         Optional<User> user = userRepository.findById(id);
         return user.get();
    }

    public User getUserByEmail(String name) {
        return userRepository.findByEmail(name);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public User findByEmail(String name) {
        return userRepository.findByEmail(name);
    }
}
