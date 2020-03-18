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
    //public static int publicGroupCounter = 0;
    private String name = "default";
    //private String groupId;
    private double locLat;
    private double locLon;
    private int range;
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
                user.setUserNumber(Integer.toString(userList.size()+1));
                userList.putIfAbsent(user.getUserNumber(), user);
                MainActivity.myRef.child("public_groups").child(FunHolder.getCurrentPublicGroup().getName()).child("userList").setValue(userList);
            }
        }else{
            isAdded = false;
        }

        return isAdded;
    }

    public void removeUser(User user){
        userList.remove(user.getUserNumber(), user);
        for(User u:userList.values()){
            u.setUserNumber(Integer.toString(Integer.valueOf(u.getUserNumber()) - 1));
        }
        MainActivity.myRef.child("public_groups").child(FunHolder.getCurrentPublicGroup().getName()).child("userList").setValue(userList);
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

        MainActivity.myRef.child("public_groups").child(getName()).child("messageCounter").removeValue();
        MainActivity.myRef.child("public_groups").child(getName()).child("messages").removeValue();
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


//    public String getGroupId(){
//        return groupId;
//    }

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

    @Exclude
    public LatLng getLatLng(){
        try{
            return new LatLng(locLat, locLon);
        }catch (Exception e){
            return new LatLng(1.1, 2.2);
        }
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof PublicGroup)){
            return false;
        }else{
            PublicGroup g = (PublicGroup) o;
            return this.getName().equals(g.getName()) && this.getLatLng().equals(g.getLatLng());
        }
    }

//    @Override
//    public String toString(){
//        return getGroupId() + " " + getName();
//    }

    public String toStringRepresentation(){
        return getName() + ", " + FunHolder.getDistance(MainActivity.user.getLatLng(), new LatLng(getLat(), getLon())) + " km away";
    }


    public Map<String, User> getUserList(){
        return userList;
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

    public int getRange(){
        return range;
    }

    public void setRange(int range){
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
        return FunHolder.getDistance(MainActivity.user.getLatLng(), this.getLatLng()) - FunHolder.getDistance(MainActivity.user.getLatLng(), g.getLatLng());
    }

}
