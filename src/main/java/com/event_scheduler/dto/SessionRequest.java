package com.event_scheduler.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.event_scheduler.helper.SessionType;
import com.event_scheduler.model.Attendee;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionRequest {
    private String userEmail;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SS")
    private LocalDateTime start;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SS")
    private LocalDateTime end;
    private SessionType sessionType;    //INDIVIDUAL or GROUP

    // Attendees
    List<Attendee> attendees;
}
