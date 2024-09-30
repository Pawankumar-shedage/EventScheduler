package com.event_scheduler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_scheduler.dto.AvailabilityRequest;
import com.event_scheduler.dto.LoginRequest;
import com.event_scheduler.dto.UserDTO;
import com.event_scheduler.helper.CalculateDuration;
import com.event_scheduler.helper.ResourceNotFoundException;
import com.event_scheduler.model.Availability;
import com.event_scheduler.model.User;
import com.event_scheduler.service.UserService;
import com.mongodb.DuplicateKeyException;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*") 
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/do-register")
    public ResponseEntity<?> addUser(@RequestBody User user){
        System.out.println("Received User: " + user); // Debug log
        
        // Save user to database
        try {
            User savedUser = this.userService.addUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/")
    public ResponseEntity<?> getUsers(){
        return ResponseEntity.ok(this.userService.getAllUsers());
    }

    @PostMapping("/do-login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest){

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        try{
            boolean isUserValid = this.userService.loginUser(email, password);
            if(isUserValid){
                System.out.println("Login successful"); // Debug log

                // return user-> name,id,email
                User user = this.userService.getUserByEmail(email).orElse(null);
                UserDTO userDetails = new UserDTO(user.getName(),user.getEmail(),user.getId());

                // sending limited user details in the response.
                return ResponseEntity.ok(userDetails);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed");
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }


    // *******USER AVAILABILITY*******
    @PostMapping("/availability")
    public ResponseEntity<?> addAvailability(@RequestBody AvailabilityRequest request){
        // 1.get user
        System.out.println("REQUEST "+request.getStart());  //debug log

        User user = this.userService.getUserByEmail(request.getEmail())
        .orElseThrow(()->new ResourceNotFoundException("User not found"));

        // Create new availability,and add to user{}
        Availability availability = new Availability();
        System.out.println("NEW AVAILABILITY, with ID "+availability.getAvailabilityId());  //debug log

        availability.setStart(request.getStart());
        availability.setEnd(request.getEnd());
        // calculate duration based on start() and end() time
        availability.setDuration(CalculateDuration.duratioinBetween(request.getStart(),request.getEnd()));

        user.getAvailabilities().add(availability);

        // Update user with availability.
        System.out.println("Updated user availaibility "+user);
        this.userService.updateUser(user);

        return ResponseEntity.ok("Availability set successfully"+user.getAvailabilities().get(0).getStart());
    }

    @GetMapping("/{email}/availability")
    public ResponseEntity<?> getAvailability(@PathVariable String email){
        // 1.get user
        User user = this.userService.getUserByEmail(email).orElse(null);

        if(user != null){
            // return availability
            List<Availability> userAvailability = user.getAvailabilities();
            return ResponseEntity.ok(userAvailability);
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        
    }

    @DeleteMapping("/{email}/availability/{availabilityId}")
    public ResponseEntity<?> deleteAvailability(@PathVariable String email,@PathVariable String availabilityId){
        // 1.access user by email
        User user  = this.userService.getUserByEmail(email).orElseThrow(()->new ResourceNotFoundException("User not found"));

        // 2.delete availabilty.
        boolean deleted = user.getAvailabilities().removeIf(availability->availability.getAvailabilityId().equals(availabilityId));

        if(!deleted){
            throw new ResourceNotFoundException("Availability not found");
        }

        // 3.Update user after deleted = true,update the availability in user document{}
        try{
            this.userService.updateUser(user);
        }
        catch(Exception e){
            throw new ResourceNotFoundException("Error updating user,with email id: "+email);
        }

        return ResponseEntity.ok("Availability deleted successfully, with id: "+availabilityId);
    }

    
    // TODO:Clear schedule(to remove all sessions and availabilities)

}
