package com.event_scheduler.helper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.event_scheduler.model.Availability;
import com.event_scheduler.model.Session;
import com.event_scheduler.model.User;
import com.event_scheduler.service.UserService;

import java.time.LocalDateTime;

@Component
public class AvailabilityHelper {

    @Autowired
    private UserService userService;


    public static boolean isTimeSlotAvailable(List<Availability> availabilities, LocalDateTime start, LocalDateTime end) {

        // for every available time slot .
        for(Availability slot: availabilities){
            
            if(slot.getStart().isBefore(end) && slot.getEnd().isAfter(start) || slot.getStart().isEqual(start) || slot.getEnd().isEqual(end)){
                // slot fits, now check for duration
                int availableDuration = (int) java.time.Duration.between(slot.getStart(), slot.getEnd()).toMinutes();
                int requestedDuration = (int) java.time.Duration.between(start, end).toMinutes();

                return availableDuration >= requestedDuration;
            }
        }
        return false;
    }


    public  void updateAvailabilityAfterSession(User user, Session session) {
             
        // Update the availability according to the session.
        List<Availability> availabilities = user.getAvailabilities();

        // LocalDateTime newStartTime = session.getStart()+user.breakForUser; //to add later.
        // LocalDateTime sEnd = session.getEnd();
        
        for(Availability a:availabilities){
            if(session.getStart().isEqual(a.getStart())){
                LocalDateTime newStartTime = session.getEnd();
                a.setStart(newStartTime);
            }
            else if(session.getStart().isAfter(a.getStart())){
                LocalDateTime newEndTime = session.getStart();
                a.setEnd(newEndTime);
            }
            else if(a.getStart().isEqual(session.getStart())&& a.getEnd().isEqual(session.getEnd())){
                // if slot is booked completely.no session.
                a.setStart(null);
                a.setEnd(null);
            }
            // duration update for new availability.
            int duration = CalculateDuration.duratioinBetween(a.getStart(),a.getEnd());
            a.setDuration(duration);
        }
    
        // Update user's availability list
        user.setAvailabilities(availabilities);
        userService.updateUser(user);
        System.out.println("Updated availability list: " + user.getAvailabilities());
        this.userService.updateUser(user);
    }

}
