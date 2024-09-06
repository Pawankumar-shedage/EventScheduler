package com.event_scheduler.controller;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_scheduler.dto.SessionRequest;
import com.event_scheduler.helper.ResourceNotFoundException;
import com.event_scheduler.model.Session;
import com.event_scheduler.model.User;
import com.event_scheduler.service.SessionService;
import com.event_scheduler.service.UserService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    // get availabilities
    @GetMapping("/availabilities")
    public ResponseEntity<?> getUsers(){
        
        return ResponseEntity.ok(this.userService.getAllUsers());
    }

    // POST admin/schedule
    @PostMapping("/schedule")
    public ResponseEntity<?> scheduleSession(@RequestBody SessionRequest sessionRequest){
        // 1.Fetch user->2.Check user availability->3.Create session.

        User user = this.userService.getUserByEmail(sessionRequest.getUserEmail())
            .orElseThrow(()->new ResourceNotFoundException("User not found"));

        boolean isAvailable = user.getAvailabilities()
                            .stream().anyMatch(slot ->
                            !sessionRequest.getStart().isBefore(slot.getStart())&&
                            !sessionRequest.getEnd().isAfter(slot.getEnd())&&
                            calculateDuration(sessionRequest.getStart(),sessionRequest.getEnd()) 
                            <= slot.getDuration()
                            );

        if(!isAvailable){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Time slot not available");
        }

        Session session = new Session();
        session.setStart(sessionRequest.getStart());
        session.setEnd(sessionRequest.getEnd());
        session.setDuration(calculateDuration(sessionRequest.getStart(),sessionRequest.getEnd()));
        session.setSessionType(sessionRequest.getSessionType());
        session.setAttendees(sessionRequest.getAttendees());

        // Saving session
        this.sessionService.addSession(session);

        // Update user state.
        user.getSessions().add(session);
        System.out.println("Added session to user: " + user.getSessions().get(0).getId());//debug log
        this.userService.updateUser(user);

        return ResponseEntity.ok("Session scheduled successfully");
    }

    private int calculateDuration(LocalDateTime start, LocalDateTime end){
        // time diff
        long diff = java.time.Duration.between(start, end).toMinutes();
        System.out.println("Duration of session: " + diff);//debug log
        return (int) diff;
    }

    // TODO:Set user as admin

    // --------------------------delete user
    @PostMapping("/delete-user")
    public void deleteUser(String email){
        try{
            this.userService.deleteUser(email);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
    


}
