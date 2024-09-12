package com.event_scheduler.service;
import com.event_scheduler.model.User;
import com.event_scheduler.helper.CalculateDuration;
// import com.event_scheduler.service.UserService;
import com.event_scheduler.model.Session;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
            return false;
        }

        // return the updated session(sessionId)
        List<Session> sessions = user.getSessions();

        for(Session s: sessions){
            if(s.getId() == sessionId){
                s.setStart(sessionData.getStart());
                s.setEnd(sessionData.getEnd());
                int newDuration = CalculateDuration.duratioinBetween(s.getStart(),s.getEnd());
                s.setDuration(newDuration);

                // update attendees
                s.setAttendees(sessionData.getAttendees());

                return true;
            }
        }

        return false;
    }
}
