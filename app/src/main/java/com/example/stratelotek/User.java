package com.example.stratelotek;

import android.location.Location;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String name;

    User(){
    }
    public int getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(int userNumber) {
        this.userNumber = userNumber;
    }

    private int userNumber;


    private double locLat;
    private double locLon;

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    //public Location getLocation() { return new Location(locLat, locLon); }
//    @Exclude
//    public void setLocation(Location location) { this.location = location; }
    @Exclude
    public void setLocation(LatLng location) {
            this.locLat = location.latitude;
            this.locLon = location.longitude;
    }
    public LatLng getLatLng(){
//        if(location!=null){
//            return new LatLng(location.getLatitude(), location.getLongitude());
//        }else{
//            return null;
//        }
        try{
            return new LatLng(locLat, locLon);
        }catch (Exception e){
            return new LatLng(1.1, 2.2);
        }
    }


    public User(String name){
        this.name = name;
    }

    public User(String name, Location location){
        this(name);
        locLat = location.getLatitude();
        locLon = location.getLongitude();
        MainActivity.currentUserName = name;
    }

    public User(String name, double locLat, double locLon){
        this(name);
        this.locLat = locLat;
        this.locLon = locLon;
        MainActivity.currentUserName = name;
    }

    public User(String name, Location location, int userNumber){
        this(name, location);
        this.userNumber = userNumber;
        MainActivity.currentUserName = name;
    }

    @Override
    public String toString(){
        return name;
    }
}
