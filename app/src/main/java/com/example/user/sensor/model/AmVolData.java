package com.example.user.sensor.model;

/**
 * Created by Hari Nugroho on 29/11/2017.
 */

public class AmVolData {
    private long unixDate;
    private float value;

    public AmVolData() {
    }

    public AmVolData(long unixDate, float value) {
        this.unixDate = unixDate;
        this.value = value;
    }

    public long getUnixDate() {
        return unixDate;
    }

    public float getValue() {
        return value;
    }
}
