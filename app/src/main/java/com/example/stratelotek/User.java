package com.example.stratelotek;

import android.location.Location;

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


    public double locLat;
    public double locLon;


    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }

        @Exclude
    public void setLocation(LatLng location) {
        if(location.latitude != 0.0 && location.longitude != 0.0){
            this.locLat = location.latitude;
            this.locLon = location.longitude;
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



}
