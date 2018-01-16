package com.example.user.sensor.model;

import java.util.ArrayList;

/**
 * Created by Hari Nugroho on 29/11/2017.
 */

public class Device {
    private String deviceName;
    private String deviceId;
    private boolean status;

    public Device() {
    }

    public Device(String deviceName, String deviceId) {
        this.deviceName = deviceName;
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
