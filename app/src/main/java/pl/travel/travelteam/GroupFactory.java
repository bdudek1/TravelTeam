package pl.travel.travelteam;

import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.function.Consumer;

import pl.travel.travelteam.group.Message;

public class GroupFactory {
    private static String name;
    private static double lat;
    private static double lon;
    private static Long range;
    private static List<User> userList = new ArrayList<>();
    private static ArrayList<Message> messageList = new ArrayList<>();

    public static PublicGroup getGroup(Queue<Object> queue){
        userList.clear();
        messageList.clear();
        PublicGroup pGroup = new PublicGroup();
        switch(queue.size()){
            case 5: case 7:{
                boolean gotUsers = false;
                boolean gotLat = false;
                while(queue.size()>0){
                    Object o = queue.poll();
                    if(o instanceof String){
                        name = (String)o;
                    }
                    if(o instanceof Long){
                        range =(Long)o;
                    }
                    if(o instanceof ArrayList){
                        if(!gotUsers){
                            for(Object user: (ArrayList)o){
                                if(user!=null && gotUsers == false){
                                    HashMap<String, Object> map = (HashMap)user;
                                    User u = new User((String)map.get("name"));
                                    //u.setLocation(new LatLng((double)map.get("lat"), (double)map.get("lon")));
                                    //System.out.println("MAP LAT CLASS = " + ((Long)map.get("lat")).getClass());
                                    //System.out.println("MAP LAT VALUE = " + (Double)map.get("lat"));
                                    u.setUserNumber((String)map.get("userNumber"));
                                    if(!userList.contains(u))
                                    userList.add(u);
                                }
                                System.out.println("userList = " + userList);

                            }
                            gotUsers = true;
                        }else{
                            for(Object message:(ArrayList)o){
                                //System.out.println("Message = " + message);
                                if(message!=null)
                                    System.out.println(message);
                                HashMap<String, Object> messageMap = (HashMap)message;
                                messageList.add(new Message((String)messageMap.get("text")));
                            }
                            //System.out.println("messageList = " + messageList);
                        }
                    }

                    if(o instanceof Double){
                        if(!gotLat){
                            lat = (Double)o;
                            gotLat = true;
                        }else{
                            lon = (Double)o;
                        }
                    }
                    //System.out.println("Factory class: " + o.getClass());
                    //System.out.println("Factory value: " + o);

                }
                Map<String, User> userListBuf = new TreeMap<String, User>();
                for(User u:userList){
                    if(u!=null && u.getUserNumber()!=null)
                    userListBuf.put(u.getUserNumber(), u);
                }
                pGroup.setName(name);
                //messageList.forEach(a -> pGroup.addMessage(new Message(a)));
                //userList.forEach(a -> pGroup.addUser(a));
                pGroup.setMessageList(messageList);
                pGroup.setUserList(userListBuf);
                pGroup.setLat(lat);
                pGroup.setLon(lon);
                pGroup.setLatLng(new LatLng(lat, lon));
                pGroup.setRange(range);
                System.out.println("FROM FACTORY = " + pGroup.toString());
            }
            case 6: case 8:{

            }
            default:{
                Toast.makeText(MainActivity.context, "Please refresh the group list.", Toast.LENGTH_SHORT).show();
            }
        }
        return pGroup;
    }

    public static double getDouble(Long l){
        Object o = (Object)l;
        Double dValue = ((Number)o).doubleValue();
        return dValue;
    }
}
