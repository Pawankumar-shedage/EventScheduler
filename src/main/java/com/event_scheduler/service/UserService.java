package com.event_scheduler.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.event_scheduler.helper.ResourceNotFoundException;
import com.event_scheduler.model.User;
import com.event_scheduler.repository.UserRepo;

public class UserService{

    @Autowired
    private UserRepo userRepo;

    private Logger  logger = LoggerFactory.getLogger(this.getClass());

    public User addUser(User user){
        logger.info("Adding user: " + user);
        // userid is automatically generated in mongodb
        // encodepassword
        // user.setPassword();
        return userRepo.save(user);
    }

    public Optional<User> getUserByEmail(String email){
        logger.info("fetch user by email: " + email);
       return userRepo.findByEmail(email);
    }

    public Optional<User> updateUser(User user){
        logger.info("Update user "+user.getEmail());
        User userFromDB = userRepo.findByEmail(user.getEmail()).orElse(null);
        if(userFromDB == null){
            throw new ResourceNotFoundException("User not found");
        }

        // update the userFromDB
        userFromDB.setEmail(user.getEmail());
        userFromDB.setName(user.getName());
        userFromDB.setPassword(user.getPassword());
        userFromDB.setRole(user.getRole());
        userFromDB.setId(user.getId());

        // save updated user.
        return Optional.ofNullable(userRepo.save(user));
    }

    public void deleteUser(String email){
        // fetch user by email
        User user = userRepo.findByEmail(email).orElse(null);
        // then delete user
        if(user != null){
            logger.warn("Deleting user: "+email);
            userRepo.delete(user);
        }
    }

    public List<User> getAllUsers() {
        logger.info("Users list");
        return userRepo.findAll();
    }

}
