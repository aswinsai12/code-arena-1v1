package com.aswin._v1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.aswin._v1.repository.UserRepo;
import com.aswin._v1.entity.User;
import com.aswin._v1.dto.GoogleAuthRequest;
import java.util.*;
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepo ur;

    @PostMapping("/auth")
    public User authenticateGoogleUser(@RequestBody GoogleAuthRequest req) {
        return ur.findByEmail(req.getEmail()).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(req.getEmail());
            String emailPrefix = req.getEmail().split("@")[0];
            int randomTag = (int)(Math.random() * 9000) + 1000;
            newUser.setUsername(emailPrefix + "#" + randomTag);
            newUser.setRating(100);
            return ur.save(newUser);
        });
    }
    @GetMapping("/leaderboard")
    private List<User> getUsers(Long userId){
        return ur.findAllByOrderByRatingDesc(userId);
    }
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserProfile(@PathVariable Long id) {
        
        User user = ur.findById(id).orElse(null);
        
        if (user != null) {
            return ResponseEntity.ok(user); 
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}