package pl.travel.travelteam;


import pl.travel.travelteam.group.Message;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class PrivateGroup extends PublicGroup {
    public static int privateGroupCounter = 0;
    private int usersCounter;
    private String password;
    private String groupId;
    public PrivateGroup(String name, String password) throws BlankPasswordException, BlankNameException{
        if(name.equals("")){
            throw new BlankNameException("Please enter the group name.");
        }
        if(password.equals("")){
            throw new BlankPasswordException("Please fill the password field.");
        }
        this.setName(name);
        //privateGroupCounter++;
        //groupId = Integer.toString(privateGroupCounter);
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
        for(User u:getUserList().values()){
            if(u != null && u.getName()!=null && u.getName().equals(user.getName()) && !getUserList().isEmpty()){
                isAdded = false;
                throw new SameNameUserException("User with same name is present in the group, please change your name.");
            }
        }
        if(isAdded){
            user.setUserNumber(Integer.toString(getUserList().size()));
            getUserList().put(user.getUserNumber(), user);
            usersCounter++;
        }
        return isAdded;
    }

    public boolean addUser(User user, String password) throws SameNameUserException, WrongPasswordException{
        boolean isAdded = true;
        if(!getPassword().equals(password)){
            isAdded = false;
            throw new WrongPasswordException("Wrong password.");
        }
        for(User u:getUserList().values()){
            if(u != null && u.getName()!=null && u.getName().equals(user.getName()) && !getUserList().isEmpty()){
                isAdded = false;
                throw new SameNameUserException("User with same name is present in the group, please change your name.");
            }
        }
        if(isAdded){
            usersCounter++;
            user.setUserNumber(Integer.toString(getUserList().size()));
            getUserList().put(user.getUserNumber(), user);
        }
        return isAdded;
    }
    @Override
    public void destroyGroup(){
        getUserList().clear();
        getMessages().clear();
        MainActivity.myRef.child("private_groups").child(MainActivity.groupName).child("messageCounter").setValue(null);
        MainActivity.myRef.child("private_groups").child(MainActivity.groupName).child("messages").setValue(getMessages());
        MainActivity.myRef.child("private_groups").child(getName()).setValue(null);
        privateGroupCounter--;
    }



    @Override
    public ArrayList<String> getUserNames(){
        ArrayList<String> list = new ArrayList<String>();
        for(User u : getUserList().values()){
            list.add(u.getName());
        }
        return list;
    }

    public String getGroupId(){
        return groupId;
    }

    @Override
    public void addMessage(Message message){
        MainActivity.myRef.child("private_groups").child(MainActivity.groupName).child("messageCounter").setValue(getMessageCounter());
        MainActivity.myRef.child("private_groups").child(MainActivity.groupName).child("messages").child(Integer.toString(getMessageCounter())).setValue(message);
    }

    @Override
    public void addMessages(List<Message> msgs){
        setMessageCounter(0);
        for(Message m:msgs){
            getMessages().add(m);
            MainActivity.myRef.child("private_groups").child(MainActivity.groupName).child("messageCounter").setValue(getMessageCounter());
            MainActivity.myRef.child("private_groups").child(MainActivity.groupName).child("messages").child(Integer.toString(getMessageCounter())).setValue(m);
            incrementMessageCounter();
        }
    }

    @Override
    public void removeUser(User user){
        getUserList().remove(user.getUserNumber(), user);
        MainActivity.myRef.child("private_groups").child(FunHolder.getCurrentPublicGroup().getName()).child("userList").setValue(getUserList());
    }
}
