package com.gruppo3.ai.lab3.utils;

public class PositionErrors {
    private boolean error = false;
    private boolean latitude = false;
    private boolean longitude = false;
    private boolean timeStamp = false;
    private boolean speed = false;

    public boolean isLatitude() {
        return latitude;
    }

    public void setLatitude(boolean latitude) {
        this.latitude = latitude;
    }

    public boolean isLongitude() {
        return longitude;
    }

    public void setLongitude(boolean longitude) {
        this.longitude = longitude;
    }

    public boolean isTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(boolean timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isSpeed() {
        return speed;
    }

    public void setSpeed(boolean speed) {
        this.speed = speed;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getErrors() {
        String errorType = "";
        if (latitude) {
            errorType += " " + "latitude";
        }
        if (longitude) {
            errorType += " " + "longitude";
        }

        if (timeStamp) {
            errorType += " " + "timestamp";
        }

        if (speed) {
            errorType += " " + "speed";
        }
        return errorType.substring(1);
    }
}