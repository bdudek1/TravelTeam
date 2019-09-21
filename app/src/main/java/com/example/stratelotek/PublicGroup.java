package com.example.stratelotek;

import com.example.stratelotek.ui.group.Message;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
@IgnoreExtraProperties
public class PublicGroup {
    public static int publicGroupCounter;
    private int usersCounter;
    private String name;
    private String groupId;
    protected int messageCounter;
    protected ArrayList<User> userList = new ArrayList<>();
    protected ArrayList<Message> messages = new ArrayList<>();

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
//        for(User u:userList){
//            if(u.getName().equals(user.getName()) && !userList.isEmpty()){
//                isAdded = false;
//                throw new SameNameUserException("User with same name is present in the group, please change your name.");
//            }
//        }
        if(isAdded){
            userList.add(user);
            usersCounter++;
            user.setUserNumber(usersCounter);
            MainActivity.myRef.child("public_groups").child(MainActivity.groupName).child("userList").child(Integer.toString(usersCounter)).setValue(user);
        }
        return isAdded;
    }

    public void removeUser(User user){
        userList.removeIf(u -> u.getName().equals(user.getName()));
        usersCounter--;
    }

//    public boolean tryToDestroy(){
//        if(usersCounter < 1){
//            destroyGroup();
//            return true;
//        }else{
//            return false;
//        }
//    }
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

    public ArrayList<String> getUserNames(){
        ArrayList<String> list = new ArrayList<String>();
        for(User u : userList){
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
        //messages.add(message);
        MainActivity.myRef.child("public_groups").child(MainActivity.groupName).child("messages").child(Integer.toString(messageCounter)).setValue(message);
        messageCounter++;
        MainActivity.myRef.child("public_groups").child(MainActivity.groupName).child("messageCounter").setValue(messageCounter);
    }

    public int getMessageCounter(){
        return messageCounter-1;
    }

    @Override
    public String toString(){
        return getGroupId() + " " + getName();
    }
}
