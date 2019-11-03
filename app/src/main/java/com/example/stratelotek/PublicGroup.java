package com.example.stratelotek;

import android.location.Location;

import com.example.stratelotek.ui.group.Message;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class PublicGroup {
    public static int publicGroupCounter;
    private String name;
    private String groupId;
    public double locLat;
    public double locLon;
    public int range;
    public int messageCounter;
    protected ArrayList<User> userList = new ArrayList<>();
    protected ArrayList<Message> messages = new ArrayList<>();

    @Exclude
    protected List<Message> messagesBuf = new ArrayList<>();


    public PublicGroup(String name) throws BlankNameException{
        if(name.equals("")){
            throw new BlankNameException("Please enter the group name.");
        }
        this.name = name;
        messageCounter = 0;
        //54.2328, 16.305
//        addUser(new User("user1", 54.2328, 16.305));
//        addUser(new User("user2", 54.233, 16.31));
//        addUser(new User("user3", 54.22, 16.0));
//        addUser(new User("user4",54.24, 16.32));
//        addUser(new User("user5", 54.242, 16.312));
        publicGroupCounter++;
        groupId = Integer.toString(publicGroupCounter);
    }

    PublicGroup(){

    }

    public boolean addUser(User user) throws SameNameUserException{
        boolean isAdded = true;
        if(user != null){
            for(User u:userList){
                if(u != null && u.getName()!=null && u.getName().equals(user.getName()) && !userList.isEmpty()){
                    isAdded = false;
                    throw new SameNameUserException("User with same name is present in the group, please change your name.");
                }
            }
            if(isAdded){
                user.setUserNumber(userList.size());
                userList.add(user);
                MainActivity.myRef.child("public_groups").child(MainActivity.groupName).child("userList").child(Integer.toString(userList.size()-1)).setValue(user);
            }
        }else{
            isAdded = false;
        }

        return isAdded;
    }

    public void removeUser(User user){
        userList.removeIf(u -> u.getName().equals(user.getName()));

        for(User u:userList){
            if(u.getName().equals(user.getName()));
            userList.remove(u);
        }
        MainActivity.myRef.child("public_groups").child(MainActivity.groupName).child("userList").setValue(userList);
    }

    public boolean tryToDestroy(){
        if(userList.size() <  1){
            destroyGroup();
            return true;
        }else{
            return false;
        }
    }
    public String getName(){
        return name;
    }

    public void destroyGroup(){
        userList.removeAll(userList);
        publicGroupCounter--;
    }

    public ArrayList<User> getUserList(){
        return userList;
    }
//
    public ArrayList<String> getUserNames(){
        ArrayList<String> list = new ArrayList<String>();
        for(User u : userList){
            if(u!=null && u.getName() != null)
            list.add(u.getName());
        }
        return list;
    }

    public boolean isEmpty(){
        if(userList.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    public String getGroupId(){
        return groupId;
    }

    public void addMessage(Message message){
        MainActivity.myRef.child("public_groups").child(MainActivity.groupName).child("messageCounter").setValue(messageCounter);
        MainActivity.myRef.child("public_groups").child(MainActivity.groupName).child("messages").child(Integer.toString(messageCounter)).setValue(message);
    }

    public  void addMessages(List<Message> msgs){
        messageCounter = 0;
        for(Message m:msgs){
            messages.add(m);
            MainActivity.myRef.child("public_groups").child(MainActivity.groupName).child("messageCounter").setValue(messageCounter);
            MainActivity.myRef.child("public_groups").child(MainActivity.groupName).child("messages").child(Integer.toString(messageCounter)).setValue(m);
            messageCounter++;
        }
    }


    public void setLatLng(LatLng loc){
        this.locLat = loc.latitude;
        this.locLon = loc.longitude;
    }


    @Override
    public String toString(){
        return getGroupId() + " " + getName();
    }

    public String toStringRepresentation(){
        return getName() + ", R:" + range + "km, D: " + FunHolder.getDistance(MainActivity.user.getLatLng(), new LatLng(locLat, locLon))/1000 +"km";
    }

    public ArrayList<Message> getMessages(){
        return messages;
    }

    public ArrayList<String> getMessagesText(){
        ArrayList<String> list = new ArrayList<>();
        for(Message s: messages){
            list.add(s.toString());
        }
        return list;
    }

    public double getLocLat(){
        return locLat;
    }

    public double getLocLon(){
        return locLon;
    }
}
