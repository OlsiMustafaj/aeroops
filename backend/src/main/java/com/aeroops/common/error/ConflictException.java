package com.aeroops.common.error;

/** Thrown when a request would violate a uniqueness or business rule. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
