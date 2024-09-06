package com.event_scheduler.repository;

import java.util.List;

import com.event_scheduler.model.Session;

public interface SessionRepo {
     List<Session> findByAttendeesEmail(String email);
}
