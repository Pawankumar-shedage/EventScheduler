package com.event_scheduler.controller;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_scheduler.model.User;
import com.event_scheduler.service.SessionService;
import com.event_scheduler.service.UserService;
import com.event_scheduler.model.Session;
import java.util.List;

@RestController
@RequestMapping("/sessions")
public class SessionController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    // GET sessions/{userEmail}
    @GetMapping("/{email}")
    public ResponseEntity<?> getSessionsForUser(@PathVariable String email){
        User user = this.userService.getUserByEmail(email).orElse(null);
        
        if(user == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User: " + email + " not found");
        }

        List<Session> sessions = sessionService.getSessionsForUser(email);

        return ResponseEntity.ok(sessions);
    }


    @PutMapping("/{email}/{sessionId}")
    public ResponseEntity<?> updateSessionForUser(@PathVariable String email,@PathVariable String sessionId,@RequestBody Session sessionData){
        boolean sessionUpdated = sessionService.updateSessionForUser(email, sessionId, sessionData);

        if(!sessionUpdated){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Session: " + sessionId + " not found");
        }

        // TODO: to update the availability after updating the session.
        return ResponseEntity.ok("session updated successfully");
    }


    // Delete a session. (then update availability it.)
    @DeleteMapping("/{email}/remove/{sessionId}")
    public ResponseEntity<?> deleteSessionForUser(@PathVariable String email,@PathVariable String sessionId){

        boolean sessionDeleted = sessionService.deleteSessionForUser(email, sessionId);
        
        if(!sessionDeleted){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Session: " + sessionId + " not found");
        }

        return ResponseEntity.ok("Session deleted successfully");
    }


}
