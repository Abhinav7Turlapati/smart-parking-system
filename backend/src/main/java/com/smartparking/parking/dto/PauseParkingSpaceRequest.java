package com.smartparking.parking.dto;

import java.time.LocalDateTime;

public class PauseParkingSpaceRequest {

    /**
     * Pause option name if supplied, e.g.:
     * "8_HOURS", "24_HOURS", "CUSTOM", "INDEFINITE"
     */
    private String option;

    /**
     * Number of hours to pause (e.g. 8, 24).
     */
    private Integer hours;

    /**
     * Custom target date-time until which the parking space should be paused.
     */
    private LocalDateTime pauseUntil;

    /**
     * Set to true if the space should remain paused until manually resumed.
     */
    private Boolean indefinite;

    public PauseParkingSpaceRequest() {
    }

    public PauseParkingSpaceRequest(Integer hours) {
        this.hours = hours;
    }

    public PauseParkingSpaceRequest(LocalDateTime pauseUntil) {
        this.pauseUntil = pauseUntil;
    }

    public PauseParkingSpaceRequest(Boolean indefinite) {
        this.indefinite = indefinite;
    }

    public static PauseParkingSpaceRequest eightHours() {
        return new PauseParkingSpaceRequest(8);
    }

    public static PauseParkingSpaceRequest twentyFourHours() {
        return new PauseParkingSpaceRequest(24);
    }

    public static PauseParkingSpaceRequest custom(LocalDateTime pauseUntil) {
        return new PauseParkingSpaceRequest(pauseUntil);
    }

    public static PauseParkingSpaceRequest indefinite() {
        return new PauseParkingSpaceRequest(Boolean.TRUE);
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public LocalDateTime getPauseUntil() {
        return pauseUntil;
    }

    public void setPauseUntil(LocalDateTime pauseUntil) {
        this.pauseUntil = pauseUntil;
    }

    public Boolean getIndefinite() {
        return indefinite;
    }

    public void setIndefinite(Boolean indefinite) {
        this.indefinite = indefinite;
    }
}
