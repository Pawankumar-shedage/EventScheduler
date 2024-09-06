package com.event_scheduler.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.event_scheduler.model.Session;

@Repository
public interface SessionRepo extends MongoRepository<Session,String> {
     List<Session> findByAttendeesEmail(String email);
}
