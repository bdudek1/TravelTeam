package pl.travel.travelteam;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;


@IgnoreExtraProperties
public class User implements Comparable<User>{
    private String name;
    private String userNumber;
    private double locLat;
    private double locLon;
    @Exclude
    private boolean isRemoved = false;
    User(){
    }
    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }



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
            //
            if(!isRemoved() && MainActivity.isPublic){
                MainActivity.myRef.child(GroupActivity.groupsReference).child(FunHolder.getCurrentPublicGroup().getName()).child("userList").child(getUserNumber()).setValue(this);
            }else if(!isRemoved()){
                MainActivity.myRef.child(GroupActivity.groupsReference).child(FunHolder.getCurrentPrivateGroup().getName()).child("userList").child(getUserNumber()).setValue(this);
            }
//                MainActivity.myRef.child(GroupActivity.groupsReference).child(FunHolder.getCurrentPublicGroup().getName()).child("userList").child(MainActivity.user.getUserNumber()).child("lat").setValue(locLat);
//                MainActivity.myRef.child(GroupActivity.groupsReference).child(FunHolder.getCurrentPublicGroup().getName()).child("userList").child(MainActivity.user.getUserNumber()).child("lon").setValue(locLon);

        }
    }
    public LatLng getLatLng(){
        try{
            return new LatLng(locLat, locLon);
        }catch (Exception e){
            return new LatLng(1.1, 2.2);
        }
    }

    public double getLat(){
        try{
            return new LatLng(locLat, locLon).latitude;
        }catch (Exception e){
            return new LatLng(1.1, 2.2).latitude;
        }
    }

    public double getLon(){
        try{
            return new LatLng(locLat, locLon).longitude;
        }catch (Exception e){
            return new LatLng(1.1, 2.2).longitude;
        }
    }

    public void setLat(double locLat){
        this.locLat = locLat;
    }

    public void setLon(double locLon){
        this.locLon = locLon;
    }



    public User(String name){
        this.name = name;
    }

    public User(String name, double lat, double lon){
        this.name = name;
        this.locLat = lat;
        this.locLon = lon;
    }

    @Override
    public boolean equals(Object o){
        if (o == null || !(o instanceof User)) {
            return false;
        }
        try{
            if(((User)o)!=null && ((User) o).getName().equals(getName())){
                return true;
            }else{
                return false;
            }
        }catch(NullPointerException e){
            return false;
        }

    }

    @Override
    public int hashCode(){
        return Integer.valueOf(getUserNumber());
    }
    @Override
    public String toString(){
        return name;
    }

    public String toStringRepresentation(){
        return getUserNumber() +" "+ getName();
    }

    @Override
    public int compareTo(User u){
        return Integer.valueOf(getUserNumber()) - Integer.valueOf(u.getUserNumber());
    }

    @Exclude
    public boolean isRemoved(){
        return isRemoved;
    }

    @Exclude
    public void setRemoved(boolean removed) {
        isRemoved = removed;
    }
}
