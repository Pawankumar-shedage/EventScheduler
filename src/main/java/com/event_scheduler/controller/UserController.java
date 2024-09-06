package com.event_scheduler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_scheduler.dto.AvailabilityRequest;
import com.event_scheduler.helper.ResourceNotFoundException;
import com.event_scheduler.model.Availability;
import com.event_scheduler.model.User;
import com.event_scheduler.service.UserService;
import java.util.List;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*") 
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/do-register")
    public ResponseEntity<?> addUser(@RequestBody User user){
        System.out.println("Received User: " + user); // Debug log
        
        // Save user to database
        User saveUser = this.userService.addUser(user);
        return ResponseEntity.ok(saveUser);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(){
        return ResponseEntity.ok(this.userService.getAllUsers());
    }

    @PostMapping("/do-login")
    public String loginUser(String email,String password){

        // if user is valid: success-message,redirect to dashboard
        boolean isUserValid = this.userService.loginUser(email, password);

        if(isUserValid){
            System.out.println("Login successful"); // Debug log
            return "redirect:/dashboard";
        }
        else{
            System.out.println("Login failed"); // Debug log
            return "redirect:/";
        }
        // else
        // invalid credentials
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
        availability.setStart(request.getStart());
        availability.setEnd(request.getEnd());
        availability.setDuration(request.getDuration());

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

    // @DeleteMapping("")

    

}
