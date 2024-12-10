package com.reddit.clone.controller;

import com.reddit.clone.model.User;
import com.reddit.clone.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Controller
@RequestMapping("/reddit")
public class UserController {
    public final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute User user,
            @RequestParam("profile_picture") MultipartFile profilePicture) throws IOException {
        boolean isRegistered = userService.register(user,profilePicture);
        if(isRegistered){
            return "redirect:/reddit/login";
        }else {
            return "redirect:/reddit/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        userService.logout(request,response);
        return "redirect:/reddit/login";
    }

    @GetMapping("/login-page")
    public String showLogin(){
        return "login";
    }


    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response,
            Model model) {
       boolean isAuthenticated = userService.login(username,password,response);
       if(isAuthenticated){
           return "redirect:/reddit/posts";
       }else{
           return "login";
       }
    }

    @GetMapping("/userDetails/{id}")
    public String getUserDetails(@PathVariable Long id,Model model){
      User user = userService.findById(Math.toIntExact(id));
      model.addAttribute("user",user);
      return "user-details";
    }
}
