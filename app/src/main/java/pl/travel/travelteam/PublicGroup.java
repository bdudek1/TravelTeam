package pl.travel.travelteam;

import pl.travel.travelteam.group.Message;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@IgnoreExtraProperties
public class PublicGroup implements Comparable<PublicGroup> {
    private String name = "default";
    private double locLat;
    private double locLon;
    private Long range;
    private int messageCounter;
    private Map<String, User> userList = new TreeMap<>();
    private ArrayList<Message> messages = new ArrayList<>();

    @Exclude
    public List<Message> messagesBuf = new ArrayList<>();


    public PublicGroup(String name) throws BlankNameException{
        if(name.equals("") || name == null){
            throw new BlankNameException("Please enter the group name.");
        }
        this.name = name;
        messageCounter = 0;
        //publicGroupCounter++;
        //groupId = Integer.toString(publicGroupCounter);
    }

    PublicGroup(){

    }

    public boolean addUser(User user) throws SameNameUserException{
        boolean isAdded = true;
        if(user != null){
            if(userList.containsValue(user)){
                isAdded = false;
                throw new SameNameUserException("User with same name is present in the group, please change your name.");
            }
            for(User u:userList.values()){
                if(u != null && u.getName()!=null && u.getName().equals(user.getName()) && !userList.isEmpty()){
                    isAdded = false;
                    throw new SameNameUserException("User with same name is present in the group, please change your name.");
                }
            }
            if(isAdded && FunHolder.getCurrentPublicGroup()!=null){
                System.out.println("USER LIST SIZE IN PG CODE = " + userList.size());
                user.setUserNumber(Integer.toString(userList.size()+1));
                userList.putIfAbsent(user.getUserNumber(), user);
//                if(userSizeBuf != userList.size()){
                MainActivity.myRef.child("public_groups").child(FunHolder.getCurrentPublicGroup().getName()).child("userList").setValue(userList);
//                    System.out.println("USER SIZE BUF = " + userSizeBuf);
//                    System.out.println("USER LIST SIZE =  " + userList.size());
//                    userSizeBuf = (short)(userList.size()+1);
//                }

            }
        }else{
            isAdded = false;
        }

        return isAdded;
    }

    public void removeUser(User user){
        if(user!=null && user.getUserNumber()!=null){
            Map<String, User> userListBuf = new TreeMap<>();
            userList.remove(user.getUserNumber(), user);
            for(User u:userList.values()){
                if(Integer.valueOf(user.getUserNumber()) < Integer.valueOf(u.getUserNumber())){
                    u.setUserNumber(Integer.toString(Integer.valueOf(u.getUserNumber()) - 1));
                    userListBuf.put(u.getUserNumber(), u);
                }else{
                    userListBuf.put(u.getUserNumber(), u);
                }

            }
            MainActivity.myRef.child("public_groups").child(FunHolder.getCurrentPublicGroup().getName()).child("userList").setValue(userListBuf);
        }
        System.out.println("USER REMOVED");
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

    public void setName(String name){
        this.name = name;
    }

    public void destroyGroup(){
        userList.clear();
        messages.clear();
        messagesBuf.clear();

//        Set<PublicGroup> gSet = MainActivity.publicGroupList.get(FunHolder.getDistance(MainActivity.user.getLatLng(), FunHolder.getCurrentPublicGroup().getLatLng()));
//        if(gSet!=null)
//        for(PublicGroup g:gSet){
//            if(g.equals(this)){
//                gSet.remove(g);
//            }
//        }

        //MainActivity.myRef.child("public_groups").child(getName()).child("messageCounter").removeValue();
        //MainActivity.myRef.child("public_groups").child(getName()).child("messages").removeValue();
        MainActivity.myRef.child("public_groups").child(getName()).removeValue();
        //publicGroupCounter--;
    }

    @Exclude
    public ArrayList<String> getUserNames(){
        ArrayList<String> list = new ArrayList<String>();
        for(User u : userList.values()){
            if(u!=null && u.getName() != null)
            list.add(u.getName());
        }
        return list;
    }

    @Exclude
    public ArrayList<String> getUserRepresentations(){
        ArrayList<String> list = new ArrayList<String>();
        for(User u : userList.values()){
            if(u!=null && u.getName() != null)
                list.add(u.toStringRepresentation());
        }
        return list;
    }

    @Exclude
    public void setUserList(Map<String, User> userList){
        this.userList = userList;
    }


//    public String getGroupId(){
//        return groupId;
//    }

    public void addMessage(Message message){
        MainActivity.myRef.child("public_groups").child(getName()).child("messageCounter").setValue(messageCounter);
        MainActivity.myRef.child("public_groups").child(getName()).child("messages").child(Integer.toString(messageCounter)).setValue(message);
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

    @Exclude
    public LatLng getLatLng(){
        try{
            return new LatLng(locLat, locLon);
        }catch (Exception e){
            return new LatLng(1.1, 2.2);
        }
    }


    @Override
    public String toString(){
        return getName();
    }

    public String toStringRepresentation(){
        return getName() + ", " + FunHolder.getDistance(MainActivity.user.getLatLng(), getLatLng())/1000 + " km away";
    }


    public Map<String, User> getUserList(){
        return userList;
    }

    public void setMessageList(ArrayList<Message> messages){
        this.messages = messages;
    }

    public ArrayList<Message> getMessages(){
        return messages;
    }

    public int getMessageCounter(){
        return messageCounter;
    }

    public void setMessageCounter(int val){
        messageCounter = val;
    }

    public void incrementMessageCounter(){
        messageCounter++;
    }

    public Long getRange(){
        return range;
    }

    public void setRange(Long range){
        this.range = range;
    }

    @Exclude
    public double getLat(){
        return locLat;
    }

    @Exclude
    public double getLon(){
        return locLon;
    }

    @Exclude
    public void setLat(double lat){
        this.locLat = lat;
    }

    @Exclude
    public void setLon(double lon){
        this.locLon = lon;
    }

    @Override
    public int compareTo(PublicGroup g){
        int distance = 0;
        if(g!=null && g.getRange()!=null){
            distance = FunHolder.getDistance(MainActivity.user.getLatLng(), g.getLatLng()) - g.getRange().intValue() - FunHolder.getDistance(MainActivity.user.getLatLng(), this.getLatLng());
        }else{
            return 1;
        }

        try{
            if(!(distance == 0)){
                return distance;
            }else{
                if(this.getLat()>g.getLat()){
                    return 1;
                }else{
                    return -1;
                }
            }

        }catch(Exception e){
            return 1;
        }

    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof PublicGroup)){
            return false;
        }else{
            PublicGroup g = (PublicGroup)o;
            if(getName().equals(g.getName()) && getLat() == g.getLat() && getLon() == g.getLon()){
                return true;
            }else{
                return false;
            }
        }
    }

}
