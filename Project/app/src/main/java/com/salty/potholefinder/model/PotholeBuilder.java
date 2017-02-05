package com.salty.potholefinder.model;

public class PotholeBuilder {
    private String potholeID;
    private double latitude;
    private double longitude;
    private String picturePath;
    private long unixTimeStamp;

    public PotholeBuilder(){

    }

    public PotholeBuilder withPotholeID(String potholeID){
        this.potholeID = potholeID;
        return this;
    }
    public PotholeBuilder withLatitude(double latitude){
        this.latitude = latitude;
        return this;
    }
    public PotholeBuilder withLongitude(double longitude){
        this.longitude = longitude;
        return this;
    }
    public PotholeBuilder withPicturePath(String picturePath){
        this.picturePath = picturePath;
        return this;
    }
    public PotholeBuilder withUnixTimeStamp(long unixTimeStamp){
        this.unixTimeStamp = unixTimeStamp;
        return this;
    }

    public Pothole createPothole(){
        Pothole pothole = new Pothole();
        pothole.potholeID = potholeID;
        pothole.latitude = latitude;
        pothole.longitude = longitude;
        pothole.picturePath = picturePath;
        pothole.unixTimeStamp = unixTimeStamp;
        return pothole;
    }
}
