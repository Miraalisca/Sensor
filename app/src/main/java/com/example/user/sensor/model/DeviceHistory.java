package com.example.user.sensor.model;

/**
 * Created with love by Hari Nugroho on 22/05/2018 at 01.41.
 */
public class DeviceHistory {
    private int ampere;
    private long duration;
    private long start;
    private int voltage;

    public DeviceHistory() {
    }

    public DeviceHistory(int ampere, long duration, long start, int voltage) {
        this.ampere = ampere;
        this.duration = duration;
        this.start = start;
        this.voltage = voltage;
    }

    public int getAmpere() {
        return ampere;
    }

    public long getDuration() {
        return duration;
    }

    public long getStart() {
        return start;
    }

    public int getVoltage() {
        return voltage;
    }
}
