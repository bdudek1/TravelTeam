package pl.travel.travelteam;


import pl.travel.travelteam.group.Message;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@IgnoreExtraProperties
public class PrivateGroup extends PublicGroup {
    private final static String key = "sgsgdgth8856h";
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
        try{
            this.password = FunHolder.encrypt(password, key);
            System.out.println("ENCRYPTED PASSWORD" + this.password);
        }catch(Exception e){
            this.password = password;
            System.out.println("ERROR ENCRYPTING PASSWORD" + e.getMessage());
        }

    }
    PrivateGroup(){

    }
    @Exclude
    public String getPassword(){

        try{
            return FunHolder.decrypt(password, key, false);
        }catch(Exception e){
            return password;
        }
    }
    @Exclude
    public void setPassword(String password){
        try{
            this.password = FunHolder.encrypt(password, key);
        }catch(Exception e){
            this.password = password;
        }

    }
    public String getPasswordEncrypted(){
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
        try{
            if(!FunHolder.decrypt(getPassword(), key, false).equals(password)){
                isAdded = false;
                System.out.println("getPassword = " + FunHolder.decrypt(getPassword(),key,false));
                System.out.println("password = "
                        + password);
                throw new WrongPasswordException("Wrong password.");
            }
        }catch(Exception e){
            System.out.println(e.getStackTrace());
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
        MainActivity.myRef.child("private_groups").child(getName()).removeValue();
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
        if(user!=null && user.getUserNumber()!=null){
            Map<String, User> userListBuf = new TreeMap<>();
            if(getUserList().remove(user.getUserNumber(), user))
                user.setRemoved(true);
            for(User u:getUserList().values()){
                if(!userListBuf.containsValue(u) && !u.equals(user))
                    userListBuf.put(u.getUserNumber(), u);
            }
            System.out.println("PRZED USTALENIEM USERLIST = " + userListBuf);
            MainActivity.myRef.child("private_groups").child(FunHolder.getCurrentPrivateGroup().getName()).child("userList").removeValue();
            MainActivity.myRef.child("private_groups").child(FunHolder.getCurrentPrivateGroup().getName()).child("userList").setValue(userListBuf);
        }
        System.out.println("USER REMOVED");
    }
}
