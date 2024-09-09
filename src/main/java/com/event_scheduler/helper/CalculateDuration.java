package com.event_scheduler.helper;

import java.time.LocalDateTime;

public class CalculateDuration {

    public static int duratioinBetween(LocalDateTime start, LocalDateTime end){
        // time diff
        long diff = java.time.Duration.between(start, end).toMinutes();
        System.out.println("Duration of session: " + diff);//debug log
        return (int) diff;
    }
}
