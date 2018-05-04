package com.example.user.sensor.model;

import java.util.ArrayList;

/**
 * Created by Hari Nugroho on 29/11/2017.
 */

public class Device {
    private String pushKey;
    private String deviceName;
    private String deviceId;
    private boolean status;

    public Device() {
    }

    public Device(String pushKey, String deviceId) {
        this.pushKey = pushKey;
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

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getPushKey() {
        return pushKey;
    }
}
