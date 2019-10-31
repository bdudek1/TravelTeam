package com.example.stratelotek;


import com.example.stratelotek.ui.group.Message;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
@IgnoreExtraProperties
public class PrivateGroup extends PublicGroup {
    public static int privateGroupCounter;
    private int usersCounter;
    private String password;
    private String groupId;
    public PrivateGroup(String name, String password) throws BlankPasswordException{
        super(name);
        if(password.equals("")){
            throw new BlankPasswordException("Please fill the password field.");
        }
//        addUser(new User("user6p"), "abc");
//        addUser(new User("user7p"), "abc");
//        addUser(new User("user8p"), "abc");
//        addUser(new User("user9p"), "abc");
//        addUser(new User("user10p"), "abc");
        publicGroupCounter--;
        privateGroupCounter++;
        groupId = Integer.toString(privateGroupCounter);
        this.password = password;
    }
    PrivateGroup(){

    }
    public String getPassword(){
        return password;
    }

    @Override
    public boolean addUser(User user) throws SameNameUserException{
        boolean isAdded = true;
        for(User u:userList){
            if(u != null && u.getName()!=null && u.getName().equals(user.getName()) && !userList.isEmpty()){
                isAdded = false;
                throw new SameNameUserException("User with same name is present in the group, please change your name.");
            }
        }
        if(isAdded){
            user.setUserNumber(userList.size());
            userList.add(user);
            usersCounter++;
            MainActivity.myRef.child("private_groups").child(MainActivity.groupName).child("userList").child(Integer.toString(userList.size()-1)).setValue(user);
        }
        return isAdded;
    }

    public boolean addUser(User user, String password) throws SameNameUserException, WrongPasswordException{
        boolean isAdded = true;
        if(!getPassword().equals(password)){
            isAdded = false;
            throw new WrongPasswordException("Wrong password.");
        }
        for(User u:userList){
            if(u != null && u.getName()!=null && u.getName().equals(user.getName()) && !userList.isEmpty()){
                isAdded = false;
                throw new SameNameUserException("User with same name is present in the group, please change your name.");
            }
        }
        if(isAdded){
            usersCounter++;
            user.setUserNumber(userList.size());
            userList.add(user);
            //MainActivity.myRef.child("private_groups").child(MainActivity.groupName).child("userList").child(Integer.toString(usersCounter)).setValue(user);
        }
        return isAdded;
    }
    @Override
    public void destroyGroup(){
        userList.removeAll(userList);
        privateGroupCounter--;
    }
    @Override
    public boolean isEmpty(){
        if(userList.isEmpty()){
            return true;
        }else{
            return false;
        }
    }




    @Override
    public ArrayList<String> getUserNames(){
        ArrayList<String> list = new ArrayList<String>();
        for(User u : userList){
            list.add(u.getName());
        }
        return list;
    }

    public String getGroupId(){
        return groupId;
    }

    @Override
    public void addMessage(Message message){
        messages.add(message);
        messageCounter++;
        MainActivity.myRef.child("private_groups").child(MainActivity.groupName).child("messageCounter").setValue(Integer.toString(FunHolder.getCurrentPrivateGroup().getMessages().size()));
        MainActivity.myRef.child("private_groups").child(MainActivity.groupName).child("messages").child(Integer.toString(FunHolder.getCurrentPrivateGroup().getMessages().size())).setValue(message);
    }
}
