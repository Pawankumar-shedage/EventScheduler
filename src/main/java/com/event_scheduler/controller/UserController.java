package com.event_scheduler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(this.userService.getAllUsers());
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping("/{email}")
    public ResponseEntity<?> getUser(@PathVariable String email) {

        User user = this.userService.getUserByEmail(email).orElse(null);
        if (user == null) { // user not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.ok(user);
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
                    if ((availability.getStart().isBefore(a.getEnd())
                            && availability.getEnd().isAfter(a.getStart()))
                            || (availability.getStart().equals(a.getStart())
                                    || availability.getEnd().equals(a.getEnd()))) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Can't add this availability " + availability.getStart() + " to "
                                        + availability.getEnd() + " for user " + user.getEmail());
                    }
                }
            }

            // Session Conflict
            for (Session s : user.getSessions()) {
                // Condition 1: Exact match should be disallowed
                if (request.getStart().isEqual(s.getStart()) && request.getEnd().isEqual(s.getEnd())) {
                    System.out.println("Session timing in updtAvl: " + s.getStart() + " " + s.getEnd());
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Can't add this availability due to exact session conflict " + s.getId());
                }
                // Condition 2: Check for overlap (✔)
                if ((request.getStart().isBefore(s.getEnd()) && request.getEnd().isAfter(s.getStart())) || // Overlapping
                                                                                                           // session
                        (request.getStart().equals(s.getStart()) || request.getEnd().equals(s.getEnd())) // Exact
                                                                                                         // boundary
                                                                                                         // match
                ) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Can't add this availability due to session conflict " + s.getId());
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
        System.out.println("request start: " + request.getStart());
        System.out.println("request end: " + request.getEnd());

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

        System.out.println("Avl start: " + availability.getStart());
        System.out.println("Avl end: " + availability.getEnd());

        // Check for conflict: 1.Availability 2.Sessions
        for (Availability ab : availabilities) {
            // (✔)
            if (request.getStart().equals(ab.getStart()) && request.getEnd().equals(ab.getEnd())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Can't set this availability due to duplicate availability conflict");
            }
        }

        for (Session s : user.getSessions()) {
            // Condition 1: Exact match should be disallowed

            if (request.getStart().isEqual(s.getStart()) && request.getEnd().isEqual(s.getEnd())) {
                System.out.println("Session timing in updtAvl: " + s.getStart() + " " + s.getEnd());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Can't set this availability due to exact session conflict " + s.getId());
            }

            // Condition 2: Check for overlap (✔)
            if ((request.getStart().isBefore(s.getEnd()) && request.getEnd().isAfter(s.getStart())) || // Overlapping
                                                                                                       // session
                    (request.getStart().equals(s.getStart()) || request.getEnd().equals(s.getEnd())) // Exact boundary
                                                                                                     // match
            ) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Can't set this availability due to session conflict " + s.getId());
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
