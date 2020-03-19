package pl.travel.travelteam;

import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

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
    private static int range;
    private static List<User> userList = new ArrayList<>();
    private static List<String> messageList = new ArrayList<>();

    public static PublicGroup getGroup(Queue<Object> queue){
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
                        range =(Integer)o;
                    }
                    if(o instanceof ArrayList){
                        if(!gotUsers){
                            for(Object user: (ArrayList)o){
                                if(user!=null && gotUsers == false){
                                    HashMap<String, Object> map = (HashMap)user;
                                    User u = new User((String)map.get("name"));
                                    u.setLocation(new LatLng(Double.valueOf((Long)map.get("lat")), Double.valueOf((Long)map.get("lon"))));
                                    u.setUserNumber((String)map.get("userNumber"));
                                    userList.add(u);
                                }
                                System.out.println("userList = " + userList);

                            }
                            gotUsers = true;
                        }else{
                            for(Object message:(ArrayList)o){
                                System.out.println("Message = " + message);
                                if(message!=null)
                                    System.out.println(message.getClass());
                                HashMap<String, Object> messageMap = (HashMap)message;
                                messageList.add((String)messageMap.get("text"));
                            }
                            System.out.println("messageList = " + messageList);
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
                    System.out.println("Factory class: " + o.getClass());
                    System.out.println("Factory value: " + o);

                }
                pGroup.setName(name);
                messageList.forEach(a -> pGroup.addMessage(new Message(a)));
                userList.forEach(a -> pGroup.addUser(a));
                pGroup.setLat(lat);
                pGroup.setLon(lon);
                pGroup.setRange(range);
            }
            case 6: case 8:{

            }
            default:{
                Toast.makeText(MainActivity.context, "Please refresh the group list.", Toast.LENGTH_SHORT).show();
            }
        }
        return pGroup;
    }
}
