package com.event_scheduler.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.event_scheduler.helper.ResourceNotFoundException;
import com.event_scheduler.model.User;
import com.event_scheduler.repository.UserRepo;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    // constructor injection
    private final UserRepo userRepo;

    public CustomUserDetailsService(UserRepo userRepo){
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username){

        User user = userRepo.findByEmail(username).orElseThrow(()-> new ResourceNotFoundException("User not found with email: "+username));

        String roleName = "ROLE_" + user.getRole().name();
        // String roleName = user.getRole().name();

        System.out.println("User role: "+roleName);

        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            user.isEnabled(),
            true,true, true,
            Collections.singletonList(new SimpleGrantedAuthority(roleName))
            );
    }

}
