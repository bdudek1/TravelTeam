package pl.travel.travelteam;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.function.Consumer;

public class GroupFactory {
    private static String name;
    private static double lat;
    private static double lon;
    private static List<User> userList = new ArrayList<>();
    private static List<User> messageList = new ArrayList<>();

    public static void getGroup(Queue<Object> queue){
        switch(queue.size()){
            case 5: case 7:{
                int arrays = 0;
                int longs = 0;
                for(Object n: queue){
                    if(n instanceof Long)
                        longs++;
                    if(n instanceof ArrayList)
                        arrays++;

                }
                boolean gotUsers = false;
                while(queue.size()>0){
                    Object o = queue.poll();
                    if(o instanceof String){
                        name = (String)o;
                    }
                    if(o instanceof ArrayList){
                        if(!gotUsers){
                            for(Object user: (ArrayList)o){
                                //System.out.println("User = " + user);
                                if(user!=null){
                                    //System.out.println("User class = " + user.getClass());
                                    //HashMap<String, Object> map = (HashMap)user;
                                    //System.out.println("User name map = " + map.get("name"));
                                    //System.out.println("User lon map = " + map.get("lon"));
                                    //System.out.println("User lat map = " + map.get("lat"));
                                    //System.out.println("User userNumber map = " + map.get("userNumber"));
                                }

                            }
                            gotUsers = true;
                        }else{
                            List<String> messageList = new ArrayList<>();
                            for(Object message:(ArrayList)o){
                                System.out.println("Message = " + message);
                                if(message!=null)
                                    System.out.println(message.getClass());
                                HashMap<String, Object> messageMap = (HashMap)message;
                                //Consumer<List<String>> msgAdder = a -> ;
                                messageList.add((String)messageMap.get("text"));
                                //System.out.println("class of msg texts = " + messageMap.get("text").getClass());
                            }
                            System.out.println("messageList = " + messageList);
                        }
                    }
                    System.out.println("Factory class: " + o.getClass());
                    System.out.println("Factory value: " + o);

                }
                System.out.println("Longs: " + longs);
                System.out.println("Arrays: " + arrays);

            }
            case 6: case 8:{

            }
            default:{
                Toast.makeText(MainActivity.context, "Please refresh the group list.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
