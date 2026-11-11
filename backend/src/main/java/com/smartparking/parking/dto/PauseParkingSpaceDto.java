package com.smartparking.parking.dto;

import java.time.LocalDateTime;

/**
 * Alias DTO for PauseParkingSpaceRequest.
 */
public class PauseParkingSpaceDto extends PauseParkingSpaceRequest {

    public PauseParkingSpaceDto() {
        super();
    }

    public PauseParkingSpaceDto(Integer hours) {
        super(hours);
    }

    public PauseParkingSpaceDto(LocalDateTime pauseUntil) {
        super(pauseUntil);
    }

    public PauseParkingSpaceDto(Boolean indefinite) {
        super(indefinite);
    }
}
