package com.example.carrental.exception;

/**
 * Thrown when a booking request conflicts with an existing active reservation.
 * Mapped to HTTP 409 Conflict by GlobalExceptionHandler.
 */
public class BookingConflictException extends RuntimeException {

    public BookingConflictException(String message) {
        super(message);
    }
}
