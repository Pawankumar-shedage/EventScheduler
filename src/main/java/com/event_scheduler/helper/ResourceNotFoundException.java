package com.event_scheduler.helper;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message)
    {
        super(message); //calling constructor of parent class.
    }

    public ResourceNotFoundException(){
        super("Resource not found!");
    }
}

