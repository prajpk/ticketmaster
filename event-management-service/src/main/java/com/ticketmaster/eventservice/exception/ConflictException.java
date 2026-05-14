package com.ticketmaster.eventservice.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
