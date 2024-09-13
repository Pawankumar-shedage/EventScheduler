package com.event_scheduler.service;
import com.event_scheduler.model.User;
import com.event_scheduler.helper.CalculateDuration;
// import com.event_scheduler.service.UserService;
import com.event_scheduler.model.Session;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    @Autowired
    private UserService userService;

    public List<Session> getSessionsForUser(String email){

    User user = this.userService.getUserByEmail(email).orElse(null);
    
    if(user == null){
        return null;
    }

    List<Session> sessions = user.getSessions();
    return sessions;
    }

    public boolean updateSessionForUser(String email,String sessionId, Session sessionData){
        User user = this.userService.getUserByEmail(email).orElse(null);

        if(user == null){
            System.out.println("User not found, to update session");  //Debug log
            return false;
        }

        // return the updated session(sessionId)
        List<Session> sessions = user.getSessions();

        for(Session s: sessions){
            if(s.getId().equals(sessionId)){
                s.setStart(sessionData.getStart());
                s.setEnd(sessionData.getEnd());
                int newDuration = CalculateDuration.duratioinBetween(s.getStart(),s.getEnd());
                s.setDuration(newDuration);

                // update attendees
                s.setAttendees(sessionData.getAttendees());
                System.out.println("Updated session: "+s.getStart()); //Debug log

                // TODO: update availability.
                //update user.
                this.userService.updateUser(user);
                return true;
            }
        }

        System.out.println("Session not found, to update session"); //Debug log
        return false;
    }

    public boolean deleteSessionForUser(String email,String sessionId){
        //1.Get user, 2.get session, 3.delete session, 4.update user-sessions and user availability.
        User user = this.userService.getUserByEmail(email).orElse(null);

        if(user == null){
            System.out.println("User not found, to delete session"); //Debug log
            return false;
        }

        List<Session> sessions = user.getSessions();
        for(Session s : sessions){
            if(s.getId().equals(sessionId)){
                user.getSessions().remove(s);

                // TODO: update availability.
                this.userService.updateUser(user);

                return true;
            }
        }

        System.out.println("Session not found, to delete session"); //Debug log
        return false;
    }
}
