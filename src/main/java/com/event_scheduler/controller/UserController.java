package com.event_scheduler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_scheduler.model.User;
import com.event_scheduler.repository.UserRepo;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*") 
public class UserController {
    @Autowired
    private UserRepo userRepo;

    @PostMapping("/register")
    public ResponseEntity<?> addUser(@RequestBody User user){
        System.out.println("Received User: " + user); // Debug log
        
        // Save user to database
        User saveUser = this.userRepo.save(user);
        return ResponseEntity.ok(saveUser);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(){
        return ResponseEntity.ok(this.userRepo.findAll());
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestBody User user){
        this.userRepo.delete(user);
        return ResponseEntity.ok("User deleted");
    }
    

}
