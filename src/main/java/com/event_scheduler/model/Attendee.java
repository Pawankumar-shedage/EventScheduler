package com.event_scheduler.model;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Attendee {
    private String name;
    private String email;
    private String attendeeId;
    @Id
    private String sessionsId;
}
