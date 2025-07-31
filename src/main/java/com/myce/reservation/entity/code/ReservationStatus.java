package com.myce.reservation.entity.code;

public enum ReservationStatus {

    CONFIRMED,
    CANCEL_REQUESTED;

    public static ReservationStatus from(String value) {
        for (ReservationStatus status : values()) {
            if (status.name().equalsIgnoreCase(value)) return status;
        }
        return null;
    }

}