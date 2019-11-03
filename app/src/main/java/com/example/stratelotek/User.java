package com.example.stratelotek;

import android.location.Location;

import com.example.stratelotek.ui.group.Message;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

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


    public double locLat;
    public double locLon;



    @Exclude
    private Marker marker;

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
        if(location.latitude != 0.0 && location.longitude != 0.0){
            this.locLat = location.latitude;
            this.locLon = location.longitude;


//            if(marker == null && GroupActivity.mMap != null){
//                marker = GroupActivity.mMap.addMarker(new MarkerOptions()
//                        .position(getLatLng())
//                        .title(getName() + ": " + getLatLng())
//                        .snippet("Distance: " + FunHolder.getDistance(MainActivity.user.getLatLng(), getLatLng()) + "m"));
//                marker.setVisible(false);
//            }else if(marker != null){
//                    marker.setVisible(true);
//                    marker.setPosition(getLatLng());
//                    marker.setTitle(getName() + ": " + getLatLng());
//                    marker.setSnippet("Distance: " + FunHolder.getDistance(MainActivity.user.getLatLng(), getLatLng()) + "m");
//                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//            }
            MainActivity.myRef.child(GroupActivity.groupsReference).child(MainActivity.groupName).child("userList").child(Integer.toString(MainActivity.user.getUserNumber())).setValue(this);
            MainActivity.myRef.child(GroupActivity.groupsReference).child(MainActivity.groupName).child("userList").child(Integer.toString(MainActivity.user.getUserNumber())).child("locLat").setValue(locLat);
            MainActivity.myRef.child(GroupActivity.groupsReference).child(MainActivity.groupName).child("userList").child(Integer.toString(MainActivity.user.getUserNumber())).child("locLon").setValue(locLon);
        }
    }
    public LatLng getLatLng(){
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
    public boolean equals(Object o){
        if (!(o instanceof User)) {
            return false;
        }
        if(((User) o).getName().equals(this.name)){
            return true;
        }else{
            return false;
        }
    }
    @Override
    public String toString(){
        return name;
    }
    @Exclude
    public Marker getMarker(){return marker;}

    public void resetMarker(){
        marker = null;
    }


}
