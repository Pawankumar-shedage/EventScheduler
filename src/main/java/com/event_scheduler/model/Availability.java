package com.event_scheduler.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Availability {
    private Date start;
    private Date end;
    private int duration;   //in minute
}
