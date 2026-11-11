package com.smartparking.parking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPauseRequestException extends InvalidParkingSpaceException {

    public InvalidPauseRequestException(String message) {
        super(message);
    }
}
