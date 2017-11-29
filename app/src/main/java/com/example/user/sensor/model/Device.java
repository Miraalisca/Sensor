package com.example.user.sensor.model;

import java.util.ArrayList;

/**
 * Created by Hari Nugroho on 29/11/2017.
 */

public class Device {
    private String deviceName;
    private String deviceId;
    private boolean status;
    private ArrayList<AmVolData> ampereDatas;
    private ArrayList<AmVolData> voltageDataas;

    public Device() {
    }

    public Device(String deviceName, String deviceId, boolean status) {
        this.deviceName = deviceName;
        this.deviceId = deviceId;
        this.status = status;
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

    public ArrayList<AmVolData> getAmpereDatas() {
        return ampereDatas;
    }

    public ArrayList<AmVolData> getVoltageDataas() {
        return voltageDataas;
    }
}
