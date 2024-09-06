package com.event_scheduler.service;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.event_scheduler.model.Session;
import com.event_scheduler.repository.SessionRepo;

@Service
public class SessionService {

    @Autowired
    private SessionRepo sessionRepo;

    public ResponseEntity<?> addSession(Session session){
        this.sessionRepo.save(session);
        return ResponseEntity.ok("Session added successfully");
    }

}
