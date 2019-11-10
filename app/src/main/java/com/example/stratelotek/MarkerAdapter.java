package com.example.stratelotek;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MarkerAdapter{
    public String userName = "defaultName";
    Marker marker;
    public MarkerAdapter(User u){
        String s;
        if(FunHolder.getDistance(MainActivity.user.getLatLng(), u.getLatLng())> 5000){
            s  = "(mAdapter) Distance: " + FunHolder.getDistance(MainActivity.user.getLatLng(), u.getLatLng())/1000 + "km";
        }else{
            s = "(mAdapter) Distance: " + FunHolder.getDistance(MainActivity.user.getLatLng(), u.getLatLng()) + "m";
        }

        userName = u.getName();
            marker = GroupActivity.mMap.addMarker(new MarkerOptions()
                    .position(u.getLatLng())
                    .title(u.getName() + ": " + u.getLatLng())
                    .snippet(s));
        if(u != null && u.getName().equals(MainActivity.user.getName())){
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }else if(u!=null){
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }
            //marker.setVisible(false);
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

    public void setPosition(User u){
        String s;
        marker.setVisible(true);
        if(FunHolder.getDistance(MainActivity.user.getLatLng(), u.getLatLng())> 5000){
            s  = "(mAdapter) Distance: " + FunHolder.getDistance(MainActivity.user.getLatLng(), u.getLatLng())/1000 + "km";
        }else{
            s = "(mAdapter) Distance: " + FunHolder.getDistance(MainActivity.user.getLatLng(), u.getLatLng()) + "m";
        }
        marker.setTitle(u.getName() + ": " + u.getLatLng());
        marker.setSnippet(s);
        if(u.getName().equals(MainActivity.user.getName())){
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }else{
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }
        marker.setPosition(u.getLatLng());
        marker.setDraggable(true);
        marker.setVisible(true);

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
