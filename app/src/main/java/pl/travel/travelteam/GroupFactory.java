package pl.travel.travelteam;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import pl.travel.travelteam.group.Message;

public class GroupFactory <T extends PublicGroup> {
    private static String name;
    private static String password;
    private static double lat;
    private static double lon;
    private static Long range;
    private static List<User> userList = new ArrayList<>();
    private static ArrayList<Message> messageList = new ArrayList<>();

    public static PublicGroup getPublicGroup(Queue<Object> queue){
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
                                    u.setUserNumber((String)map.get("userNumber"));
                                    if(!userList.contains(u))
                                    userList.add(u);
                                }

                            }
                            gotUsers = true;
                        }else{
                            for(Object message:(ArrayList)o){
                                if(message!=null)
                                    System.out.println(message);
                                HashMap<String, Object> messageMap = (HashMap)message;
                                messageList.add(new Message((String)messageMap.get("text")));
                            }
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

                }
                Map<String, User> userListBuf = new TreeMap<String, User>();
                for(User u:userList){
                    if(u!=null && u.getUserNumber()!=null){
                        userListBuf.put(u.getUserNumber(), u);
                    }
                }
                pGroup.setName(name);
                pGroup.setMessageList(messageList);
                pGroup.setUserList(userListBuf);
                pGroup.setLat(lat);
                pGroup.setLon(lon);
                pGroup.setLatLng(new LatLng(lat, lon));
                pGroup.setRange(range);
            }
            default:{

            }
        }
        return pGroup;
    }

    public static PrivateGroup getPrivateGroup(Queue<Object> queue){
        userList.clear();
        messageList.clear();
        PrivateGroup pGroup = new PrivateGroup();
        switch(queue.size()){
            case 6: case 8:{
                boolean gotUsers = false;
                boolean gotLat = false;
                boolean gotName = true;
                while(queue.size()>0){
                    Object o = queue.poll();
                    if(o instanceof String){
                        if(gotName){
                            password = (String)o;
                            gotName = false;
                        }else{
                            name = (String)o;
                        }

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
                                    u.setUserNumber((String)map.get("userNumber"));
                                    if(!userList.contains(u))
                                        userList.add(u);
                                }
                            }
                            gotUsers = true;
                        }else{
                            for(Object message:(ArrayList)o){
                                if(message!=null)
                                    System.out.println(message);
                                HashMap<String, Object> messageMap = (HashMap)message;
                                messageList.add(new Message((String)messageMap.get("text")));
                            }
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

                }
                Map<String, User> userListBuf = new TreeMap<String, User>();
                for(User u:userList){
                    if(u!=null && u.getUserNumber()!=null){
                        userListBuf.put(u.getUserNumber(), u);
                    }
                }
                pGroup.setName(name);
                pGroup.setPasswordUnencrypted(password);
                pGroup.setMessageList(messageList);
                pGroup.setUserList(userListBuf);
                pGroup.setLat(lat);
                pGroup.setLon(lon);
                pGroup.setLatLng(new LatLng(lat, lon));
                pGroup.setRange(range);
            }
            default:{

            }
        }
        return pGroup;
    }

}
