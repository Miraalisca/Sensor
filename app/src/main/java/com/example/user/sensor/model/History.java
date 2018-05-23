package com.example.user.sensor.model;

/**
 * Created by MY NOTEBOOK on 21/05/2018.
 */

public class History {
    private String percentage;
    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private String savings;

    public void setSavings(String savings) {
        this.savings = savings;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getPercentage() {
        return percentage;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getSavings() {
        return savings;
    }
}
