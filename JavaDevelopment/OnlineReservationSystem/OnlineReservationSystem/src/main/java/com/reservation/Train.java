package com.reservation;

public class Train {
    private final int number;
    private final String name;

    public Train(int number, String name) {
        this.number = number;
        this.name = name;
    }

    public int getNumber() { return number; }
    public String getName() { return name; }
}
