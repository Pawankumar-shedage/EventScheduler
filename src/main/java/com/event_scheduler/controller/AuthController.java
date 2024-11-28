package com.event_scheduler.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.event_scheduler.dto.LoginRequest;
import com.event_scheduler.dto.UserDTO;
import com.event_scheduler.model.User;
import com.event_scheduler.service.UserService;

@RestController
@CrossOrigin(origins = "http://localhost:5173/")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/public/do-login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        System.out.println("Email: " + email + ", Password: " + password);// Debug log
        try {
            boolean isUserValid = this.userService.loginUser(email, password);
            if (isUserValid) {
                System.out.println("Login successful"); // Debug log

                // return user-> name,id,email
                User user = this.userService.getUserByEmail(email).orElse(null);
                UserDTO userDetails = new UserDTO(user.getName(), user.getEmail(), user.getId(), user.getPassword());

                // sending limited user details in the response.
                return ResponseEntity.ok(userDetails);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/public/do-register")
    public ResponseEntity<?> addUser(@RequestBody User user) {
        System.out.println("Received User: " + user); // Debug log

        // Save user to database
        try {
            User savedUser = this.userService.addUser(user);

            // return user-> name,id,email
            UserDTO userDetails = new UserDTO(savedUser.getName(), savedUser.getEmail(), savedUser.getId(),savedUser.getPassword());
            return ResponseEntity.ok(userDetails.getEmail());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
