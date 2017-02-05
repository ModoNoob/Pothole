package com.salty.potholefinder;

import com.salty.potholefinder.model.Pothole;

public class PotholeBuilder {
    public Pothole withPotholeID(String potholeID){
        Pothole pothole = new Pothole();
        pothole.potholeID = potholeID;
        return pothole;
    }
    public Pothole withLatittude(double latitude){
        Pothole pothole = new Pothole();
        pothole.latitude = latitude;
        return pothole;
    }
    public Pothole withLongitude(double longitude){
        Pothole pothole = new Pothole();
        pothole.longitude = longitude;
        return pothole;
    }
    public Pothole withPicturePath(String picturePath){
        Pothole pothole = new Pothole();
        pothole.picturePath = picturePath;
        return pothole;
    }
    public Pothole withUnixTimeStamp(long unixTimeStamp){
        Pothole pothole = new Pothole();
        pothole.unixTimeStamp = unixTimeStamp;
        return pothole;
    }
}
