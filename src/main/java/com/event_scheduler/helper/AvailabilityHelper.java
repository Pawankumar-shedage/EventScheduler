package com.event_scheduler.helper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.event_scheduler.model.Availability;
import com.event_scheduler.model.Session;
import com.event_scheduler.model.User;
import java.util.ArrayList;
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
                int availableDuration = CalculateDuration.duratioinBetween(slot.getStart(), slot.getEnd());
                int requestedDuration = CalculateDuration.duratioinBetween(start, end);

                return availableDuration >= requestedDuration;
            }
        }
        return false;
    }


    public  void updateAvailabilityAfterSession(User user, Session session) {
        List<Availability> availabilities = user.getAvailabilities();
        List<Availability> newAvailabilities = new ArrayList<>();
        for(Availability a:availabilities){

            if(session.getStart().isEqual(a.getStart())){
                // split the availability into two,
                // 1.session starts at a.start, new a.start = s.end, a.end = a.end
                Availability newAvailability1 = new Availability();
                newAvailability1.setStart(session.getEnd());
                newAvailability1.setEnd(a.getEnd());
                newAvailability1.setDuration(CalculateDuration.duratioinBetween(session.getEnd(), a.getEnd()));
                       
                a.setEnd(session.getStart());
                newAvailabilities.add(newAvailability1);
            }
            else if(session.getStart().isAfter(a.getStart())){
                // split the availability into two
                // 2.session starts between availability=>new  a.start = a.start, a.end = s.start
                Availability newAvailability1 = new Availability();
                newAvailability1.setStart(a.getStart());
                newAvailability1.setEnd(session.getStart());
                newAvailability1.setDuration(CalculateDuration.duratioinBetween(a.getStart(), session.getStart()));

                a.setStart(session.getEnd());
                newAvailabilities.add(newAvailability1);
            }
            else if(a.getStart().isEqual(session.getStart())&& a.getEnd().isEqual(session.getEnd())){
                // if slot is booked completely.no availability.
                a.setStart(null);
                a.setEnd(null);
            }
            else if(a.getStart().isBefore(session.getStart()) && a.getEnd().isAfter(session.getEnd())){
                // split the availability into two, session is in middle of availability.

                // save the availability end first
                LocalDateTime availabilityEnd = a.getEnd();

                // a1
                a.setEnd(session.getStart());

                // a2
                Availability newAvailability1 = new Availability();
                newAvailability1.setStart(session.getEnd());
                newAvailability1.setEnd(availabilityEnd);
                newAvailability1.setDuration(CalculateDuration.duratioinBetween(session.getEnd(),a.getEnd()));

                newAvailabilities.add(a);
                newAvailabilities.add(newAvailability1);
            }
            else{
                newAvailabilities.add(a);
            }
        }
        user.setAvailabilities(newAvailabilities);
        System.out.println("Updated availability list: " + user.getAvailabilities());
        this.userService.updateUser(user);
    }

}
