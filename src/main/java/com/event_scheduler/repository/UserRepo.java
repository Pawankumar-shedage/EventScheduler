package com.event_scheduler.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.event_scheduler.model.User;

@Repository
public interface UserRepo extends MongoRepository<User,String> {
    Optional<User> findByEmail(String email); 
}
