package com.event_scheduler.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Document(collection="sessions")
public class Session {
    @Id
    private String id;
    private Date start;
     private Date end;
    private String sessionType; // "one-on-one" or "group"
    private List<Attendee> attendees = new ArrayList<>();
}   
