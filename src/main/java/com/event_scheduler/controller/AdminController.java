package com.event_scheduler.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_scheduler.service.UserService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @PostMapping("/delete")
    public void deleteUser(String email){

    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(){

        return ResponseEntity.ok(this.userService.getAllUsers());
    }

    


}
