package com.event_scheduler.model;

import java.util.List;


import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.event_scheduler.helper.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user")
public class User {
    private String name;
    @Id
    private String id;
    private String email;
    private String password;  
    @Builder.Default()
    private Enum<Role> role = Role.USER;  // Roles could be "USER", "ADMIN", etc.
   
    // private List<Availability> availabilities = new ArrayList<>();
    // private List<Session> sessions = new ArrayList<>();


}