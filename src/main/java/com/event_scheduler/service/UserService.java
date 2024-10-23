package com.event_scheduler.service;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.event_scheduler.helper.ResourceNotFoundException;
import com.event_scheduler.helper.Role;
import com.event_scheduler.model.Availability;
import com.event_scheduler.model.Session;
import com.event_scheduler.model.User;
import com.event_scheduler.repository.UserRepo;

@Service
public class UserService{

    @Autowired
    private UserRepo userRepo;

    private Logger  logger = LoggerFactory.getLogger(this.getClass());

    public User addUser(User user){
        logger.info("Adding user: " + user.getEmail());

        // Check if the email is already in use
        Optional<User> existingUser = userRepo.findByEmail(user.getEmail());
        
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("User with email " + user.getEmail() +" already exists");
            // return null;
        }
        // userid is automatically generated in mongodb
        // encode password
        logger.info("encrypting password");

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
    
        return userRepo.save(user);
    }

    public Optional<User> getUserByEmail(String email){
        logger.info("fetch user by email: " + email);
       return userRepo.findByEmail(email);
    }

    // login,
    public boolean loginUser(String email, String password){
        logger.info("Login user " + email);

        User storedUser = userRepo.findByEmail(email).orElse(null);
        if(storedUser != null)
        {
            //verify password
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean isPasswordMatch = passwordEncoder.matches(password,storedUser.getPassword());
            return isPasswordMatch;
        }
        return false;
    }

    public Optional<User> updateUser(User user){
        logger.info("Updated user "+user.getEmail());

        User userFromDB = userRepo.findByEmail(user.getEmail()).orElse(null);
        if(userFromDB == null){
            throw new ResourceNotFoundException("User not found "+user.getEmail());
        }

        // update the userFromDB (each field)
        userFromDB.setEmail(user.getEmail());
        userFromDB.setName(user.getName());
        userFromDB.setPassword(user.getPassword());
        userFromDB.setRole(user.getRole());
        userFromDB.setId(user.getId());
        userFromDB.setAvailabilities(user.getAvailabilities());
        userFromDB.setSessions(user.getSessions());

        // save updated user
        return Optional.ofNullable(userRepo.save(userFromDB));
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

    public void addSessionToUser(User user,Session session){
        user.getSessions().add(session);
        updateUser(user);
    }

    public boolean setUserRole(String email,Role role){
        User user = userRepo.findByEmail(email).orElse(null);

        if(user == null){
            System.out.println("User not found to set role: "+role);  //Debug log
            return false;
        }

        System.out.println("User role set to "+role);//debug log
        user.setRole(role);
        // update user
        updateUser(user);

        return true;
    }

    // user availabilitiies
    public void deleteAvailability(User user,String availabilityId){
        for(Availability availability : user.getAvailabilities()){
            if(availability.getAvailabilityId().equals(availabilityId)){
                System.out.println("Deleting availability: "+availabilityId + " for user: "+user.getEmail());

                user.getAvailabilities().remove(availability);
                updateUser(user);
                break;
            }
        }
    }
}
