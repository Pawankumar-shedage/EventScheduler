package com.event_scheduler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_scheduler.dto.AvailabilityRequest;
import com.event_scheduler.dto.LoginRequest;
import com.event_scheduler.dto.UserDTO;
import com.event_scheduler.helper.CalculateDuration;
import com.event_scheduler.helper.ResourceNotFoundException;
import com.event_scheduler.model.Availability;
import com.event_scheduler.model.Session;
import com.event_scheduler.model.User;
import com.event_scheduler.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/do-register")
    public ResponseEntity<?> addUser(@RequestBody User user) {
        System.out.println("Received User: " + user); // Debug log

        // Save user to database
        try {
            User savedUser = this.userService.addUser(user);

            // return user-> name,id,email
            UserDTO userDetails = new UserDTO(savedUser.getName(), savedUser.getEmail(), savedUser.getId());
            return ResponseEntity.ok(userDetails);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(this.userService.getAllUsers());
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getUser(@PathVariable String email) {

        User user = this.userService.getUserByEmail(email).orElse(null);
        if (user == null) { // user not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/do-login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        try {
            boolean isUserValid = this.userService.loginUser(email, password);
            if (isUserValid) {
                System.out.println("Login successful"); // Debug log

                // return user-> name,id,email
                User user = this.userService.getUserByEmail(email).orElse(null);
                UserDTO userDetails = new UserDTO(user.getName(), user.getEmail(), user.getId());

                // sending limited user details in the response.
                return ResponseEntity.ok(userDetails);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // *******USER AVAILABILITY*******
    @PostMapping("/availability") // Add availability
    public ResponseEntity<?> addAvailability(@RequestBody AvailabilityRequest[] requests) {
        for (AvailabilityRequest request : requests) {
            User user = this.userService.getUserByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Availability availability = new Availability();

            availability.setStart(request.getStart());
            availability.setEnd(request.getEnd());
            availability.setDuration(CalculateDuration.duratioinBetween(request.getStart(), request.getEnd()));

            // if start == end
            if (availability.getStart().equals(availability.getEnd())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Availability can't start and end at same time "
                        + availability.getStart() + " to " + availability.getEnd() + " for user " + user.getEmail());
            }
            // Don't add availability if end < start
            if (availability.getEnd().isBefore(availability.getStart())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Availability can't end before starting. "
                        + availability.getStart() + " to " + availability.getEnd() + " for user " + user.getEmail());
            }

            List<Availability> existingAvailabilities = user.getAvailabilities();

            if (existingAvailabilities != null && !existingAvailabilities.isEmpty()) {

                // Check for exisiting availabilitiy OR Coflict
                for (Availability a : user.getAvailabilities()) {
                    if (a.getStart().equals(availability.getStart()) && a.getEnd().equals(availability.getEnd())) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("Availability already exists");
                    }
                    // new availability is between a.start and a.end
                    else if (availability.getStart().isBefore(a.getEnd())
                            && availability.getEnd().isAfter(a.getStart())) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Can't set this availability " + availability.getStart() + " to "
                                        + availability.getEnd() + " for user " + user.getEmail());
                    }
                }
            }

            user.getAvailabilities().add(availability); // add each availability.
            this.userService.updateUser(user); // Update user{}

        }
        return ResponseEntity.ok("Availability set successfully");

        // return ResponseEntity.status(HttpStatus.CONFLICT).body("Bad request");
    }

    @GetMapping("/{email}/availability")
    public ResponseEntity<?> getAvailability(@PathVariable String email) {
        // 1.get user
        User user = this.userService.getUserByEmail(email).orElse(null);

        if (user != null) {
            // return availability
            List<Availability> userAvailability = user.getAvailabilities();
            return ResponseEntity.ok(userAvailability);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

    }

    @DeleteMapping("/{email}/availability/{availabilityId}")
    public ResponseEntity<?> deleteAvailability(@PathVariable String email, @PathVariable String availabilityId) {
        // 1.access user by email
        User user = this.userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2.delete availabilty.
        boolean deleted = user.getAvailabilities()
                .removeIf(availability -> availability.getAvailabilityId().equals(availabilityId));

        if (!deleted) {
            throw new ResourceNotFoundException("Availability not found");
        }

        // 3.Update user after deleted = true,update the availability in user document{}
        try {
            this.userService.updateUser(user);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error updating user,with email id: " + email);
        }

        return ResponseEntity.ok("Availability deleted successfully, with id: " + availabilityId);
    }

    @PutMapping("/{email}/updtAvailability/{availabilityId}")
    public ResponseEntity<?> updateAvailability(@PathVariable String email, @PathVariable String availabilityId,
            @RequestBody AvailabilityRequest request) {
        // 1st get that availability
        // check for existing availability
        User user = this.userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User1 not found"));

        List<Availability> availabilities = user.getAvailabilities();

        Availability availability = null;
        for (Availability a : availabilities) {
            if (a.getAvailabilityId().equals(availabilityId)) {
                availability = a;
                break;
            }
        }

        if (availability == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Availability not found");
        }

        // Check for conflict: 1.Availability 2.Sessions
        for (Availability ab : availabilities) {
            if (request.getStart().isAfter(ab.getStart()) && request.getEnd().isBefore(ab.getEnd())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Can't set this availability due to availability conflict");
            }
        }

        for (Session s : user.getSessions()) {
            if (// Condition 1: request start and end times should not be exactly equal to session start and end times
            (!request.getStart().equals(s.getStart()) && !request.getEnd().equals(s.getEnd())) &&
    
            // Condition 2: request start time should be before session start, and request end time should be less than or equal to session end
            (request.getStart().isBefore(s.getStart()) && request.getEnd().isBefore(s.getEnd())) ||
    
            // Condition 3: request start time is before session end
            (request.getStart().isBefore(s.getEnd()))
            ) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Can't set this availability due to session conflict "+s.getId());
            }
        }

        // set new start and end time
        availability.setStart(request.getStart());
        availability.setEnd(request.getEnd());
        availability.setDuration(CalculateDuration.duratioinBetween(request.getStart(), request.getEnd()));

        // Update user
        this.userService.updateUser(user);

        return ResponseEntity.ok("Availability updated successfully");
    }
    // TODO:Clear schedule(to remove all sessions and availabilities)

}
