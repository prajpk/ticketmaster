package com.ticketmaster.allocationservice.exception;

public class SeatNotAvailableException extends RuntimeException {
    public SeatNotAvailableException(String message) { super(message); }
}
