package com.library.model;

public enum ReservationStatus {
    /** Waiting for a copy of the book to become available. */
    PENDING,
    /** A copy became available and was automatically issued to this reservation's member. */
    FULFILLED,
    /** Cancelled by the member or admin before being fulfilled. */
    CANCELLED
}
