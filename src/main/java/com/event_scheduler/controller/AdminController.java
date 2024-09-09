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
import java.util.List;
import java.util.ArrayList;
import com.event_scheduler.dto.SessionRequest;
import com.event_scheduler.helper.ResourceNotFoundException;
import com.event_scheduler.helper.AvailabilityHelper;
import com.event_scheduler.helper.CalculateDuration;
import com.event_scheduler.model.Session;
import com.event_scheduler.model.User;
import com.event_scheduler.service.UserService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AvailabilityHelper availabilityHelper;

    // get users
    @GetMapping("/usersList")
    public ResponseEntity<?> getUsers(){
        
        return ResponseEntity.ok(this.userService.getAllUsers());
    }

    // POST admin/schedule
    @PostMapping("/schedule")
    public ResponseEntity<?> scheduleSession(@RequestBody SessionRequest sessionRequest){
        // 1.Fetch user->2.Check user availability->3.Create session.
        User user = this.userService.getUserByEmail(sessionRequest.getUserEmail())
            .orElseThrow(()->new ResourceNotFoundException("User not found"));
            
        // CONFLICT CHECKING
        boolean isAvailable =
        AvailabilityHelper.isTimeSlotAvailable(user.getAvailabilities(), sessionRequest.getStart(), sessionRequest.getEnd());
       
        if(!isAvailable){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Time slot not available,Couldn't schedule the session");
        }

        Session session = new Session();
        session.setStart(sessionRequest.getStart());
        session.setEnd(sessionRequest.getEnd());

        int sessionDuration = CalculateDuration.duratioinBetween(sessionRequest.getStart(),sessionRequest.getEnd());
        session.setDuration(sessionDuration);
        session.setSessionType(sessionRequest.getSessionType());
        session.setAttendees(sessionRequest.getAttendees());
        
        if(sessionDuration >= 1){
            // session duration must be atleast for 1 minute
            userService.addSessionToUser(user, session);
        }else{
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Session duration must be atleast for 1 minute");
        }

        // Update availability after the session is booked.
        availabilityHelper.updateAvailabilityAfterSession(user,session);

        return ResponseEntity.ok("Session scheduled successfully");
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
