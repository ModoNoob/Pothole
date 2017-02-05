package com.salty.potholefinder.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;

public class Pothole implements Serializable, ClusterItem {

    public String potholeID;

    public double latitude;

    public double longtitude;

    public String picture;

    public long unixTimeStamp;

    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longtitude);
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public String getSnippet() {
        return "";
    }
}
