package com.example.stratelotek;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MarkerAdapter{
    public String userName = "defaultName";
    Marker marker;
    public MarkerAdapter(User u, GoogleMap gMap){
        userName = u.getName();
            marker = gMap.addMarker(new MarkerOptions()
                    .position(u.getLatLng())
                    .title(u.getName() + ": " + u.getLatLng())
                    .snippet("(mAdapter)Distance: " + FunHolder.getDistance(MainActivity.user.getLatLng(), u.getLatLng()) + "m"));

            marker.setVisible(false);
        //marker.remove();
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof MarkerAdapter)){
            return false;
        }else{
            MarkerAdapter x = (MarkerAdapter)o;
            if(x.userName != null && x.userName.equals(userName)){
                return true;
            }else{
                return false;
            }
        }
    }

    public void setPosition(User u, GoogleMap gMap){

        marker.setVisible(true);
        marker.setTitle(u.getName() + ": " + u.getLatLng());
        String s;
        if(FunHolder.getDistance(MainActivity.user.getLatLng(), u.getLatLng())> 5000){
            s  = "(mAdapter) Distance: " + FunHolder.getDistance(MainActivity.user.getLatLng(), u.getLatLng())/1000 + "km";
        }else{
            s = "(mAdapter) Distance: " + FunHolder.getDistance(MainActivity.user.getLatLng(), u.getLatLng()) + "m";
        }
        marker.setSnippet(s);
        if(u.getName().equals(MainActivity.user.getName())){
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }else{
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }
        marker.setPosition(u.getLatLng());

    }
    public Marker getMarker(){
        return marker;
    }
    public String getUserName(){return userName;}

    @Override
    public String toString(){return getUserName() +" : " + marker.getPosition().toString();}

    public MarkerOptions getMarkerOptions(User u){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(u.getLatLng());
        markerOptions.title(u.getName() + ": " + u.getLatLng());
        markerOptions.snippet("(mAdapter)Distance: " + FunHolder.getDistance(MainActivity.user.getLatLng(), u.getLatLng()) + "m");
        return markerOptions;
    }
}
