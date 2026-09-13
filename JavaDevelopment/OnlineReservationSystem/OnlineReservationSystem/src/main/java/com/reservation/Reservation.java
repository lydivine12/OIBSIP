package com.reservation;

public class Reservation {
    private String pnr;
    private String passengerName;
    private int trainNumber;
    private String trainName;
    private String classType;
    private String journeyDate;
    private String sourceStation;
    private String destinationStation;
    private String createdAt;

    public Reservation(String pnr, String passengerName, int trainNumber, String trainName,
                       String classType, String journeyDate, String sourceStation,
                       String destinationStation, String createdAt) {
        this.pnr = pnr;
        this.passengerName = passengerName;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.classType = classType;
        this.journeyDate = journeyDate;
        this.sourceStation = sourceStation;
        this.destinationStation = destinationStation;
        this.createdAt = createdAt;
    }

    public String getPnr() { return pnr; }
    public String getPassengerName() { return passengerName; }
    public int getTrainNumber() { return trainNumber; }
    public String getTrainName() { return trainName; }
    public String getClassType() { return classType; }
    public String getJourneyDate() { return journeyDate; }
    public String getSourceStation() { return sourceStation; }
    public String getDestinationStation() { return destinationStation; }
    public String getCreatedAt() { return createdAt; }
}
