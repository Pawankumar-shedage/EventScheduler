package com.event_scheduler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.event_scheduler.dto.SessionRequest;
import com.event_scheduler.helper.ResourceNotFoundException;
import com.event_scheduler.helper.AvailabilityHelper;
import com.event_scheduler.helper.CalculateDuration;
import com.event_scheduler.model.Session;
import com.event_scheduler.model.User;
import com.event_scheduler.service.UserService;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
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
    @PostMapping("/schedule/session")
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
        session.setTitle(sessionRequest.getTitle
        ());
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

   

    @PostMapping("/set-role/{email}")
    public ResponseEntity<?> setAdmin(@PathVariable String email,@RequestBody User userRole){

        boolean setUserAsAdmin = this.userService.setUserRole(email,userRole.getRole());

        if(!setUserAsAdmin){
            System.out.println("Couldn't set user as: "+userRole);//Debug log
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldn't set user as: "+userRole);
        }

        System.out.println("User role set as: "+userRole.getRole());//Debug log

        return ResponseEntity.ok("User role set as: "+userRole.getRole());
    }

    // --------------------------delete user
    @DeleteMapping("/delete-user/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email){
        try{
            this.userService.deleteUser(email);
            return ResponseEntity.ok("User deleted successfully");
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }
    


}
