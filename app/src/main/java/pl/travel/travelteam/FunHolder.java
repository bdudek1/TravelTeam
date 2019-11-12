package pl.travel.travelteam;

import android.location.Location;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class FunHolder {
    public static void initInfo(){
        MainActivity.userName.setText(MainActivity.user.getName());
        MainActivity.userName.invalidate();
        MainActivity.informacje.setVisibility(View.VISIBLE);
        MainActivity.buttonInfo.setVisibility(View.VISIBLE);
        MainActivity.typGrupy.setVisibility(View.INVISIBLE);
        MainActivity.zasiegBar.setVisibility(View.INVISIBLE);
        MainActivity.szukajDruzyny.setVisibility(View.INVISIBLE);
        MainActivity.stworzDruzyne.setVisibility(View.INVISIBLE);
        MainActivity.wprowadzNick.setVisibility(View.VISIBLE);
        MainActivity.userName.setVisibility(View.VISIBLE);
        MainActivity.zmienNick.setVisibility(View.VISIBLE);
    }
    public static void initGroups(){
        MainActivity.informacje.setVisibility(View.INVISIBLE);
        MainActivity.buttonInfo.setVisibility(View.INVISIBLE);
        MainActivity.szukajDruzyny.setVisibility(View.VISIBLE);
        MainActivity.zasiegBar.setVisibility(View.VISIBLE);
        MainActivity.stworzDruzyne.setVisibility(View.VISIBLE);
        MainActivity.wprowadzNick.setVisibility(View.INVISIBLE);
        MainActivity.userName.setVisibility(View.INVISIBLE);
        MainActivity.zmienNick.setVisibility(View.INVISIBLE);
    }

    public static void adjustGravity(View v) {
        if (v.getId() == com.google.android.material.R.id.smallLabel) {
            ViewGroup parent = (ViewGroup) v.getParent();
            parent.setPadding(0, 0, 0, 0);

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) parent.getLayoutParams();
            params.gravity = Gravity.CENTER;
            parent.setLayoutParams(params);
        }

        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;

            for (int i = 0; i < vg.getChildCount(); i++) {
                adjustGravity(vg.getChildAt(i));
            }
        }
    }

    public static void initChat(){
        GroupActivity.sendButton.setVisibility(View.VISIBLE);
        GroupActivity.chat.setVisibility(View.VISIBLE);
        GroupActivity.messageEtext.setVisibility(View.VISIBLE);
        GroupActivity.listaUzytkownikow.setVisibility(View.INVISIBLE);
        GroupActivity.mapFragment.getView().setVisibility(View.INVISIBLE);
        GroupActivity.mapFragment.getView().setVisibility(View.GONE);
    }

    public static void initUsersList(){
        GroupActivity.sendButton.setVisibility(View.INVISIBLE);
        GroupActivity.chat.setVisibility(View.INVISIBLE);
        GroupActivity.messageEtext.setVisibility(View.INVISIBLE);
        GroupActivity.listaUzytkownikow.setVisibility(View.VISIBLE);
        GroupActivity.mapFragment.getView().setVisibility(View.INVISIBLE);
        GroupActivity.mapFragment.getView().setVisibility(View.GONE);
    }

    public static void initMap(){
        GroupActivity.sendButton.setVisibility(View.INVISIBLE);
        GroupActivity.chat.setVisibility(View.INVISIBLE);
        GroupActivity.messageEtext.setVisibility(View.INVISIBLE);
        GroupActivity.listaUzytkownikow.setVisibility(View.INVISIBLE);
        GroupActivity.mapFragment.getView().setVisibility(View.VISIBLE);
    }

    public static PublicGroup getCurrentPublicGroup(){
        return MainActivity.currentPublicGroup;
    }

    public static PrivateGroup getCurrentPrivateGroup(){
        return MainActivity.currentPrivateGroup;
    }

    public static List<String> getPublicGroupNames(){
        List<String> names = new ArrayList<>();
        for(PublicGroup g: MainActivity.publicGroupList){
            if(g.getName()!= "" && g.getName() != null){
                if(MainActivity.range == 0){
                    names.add(g.toStringRepresentation());
                }else{
                    if(MainActivity.range > getDistance(MainActivity.user.getLatLng(), new LatLng(g.getLocLat(), g.getLocLon()))){
                        names.add(g.toStringRepresentation());
                    }
                }
            }


        }
        return names;
    }

    public static List<String> getPrivateGroupNames(){
        List<String> names = new ArrayList<>();
        for(PrivateGroup g: MainActivity.privateGroupList){
            if(g.getName()!= "" && g.getName() != null){
                if(MainActivity.range == 0){
                    names.add(g.toStringRepresentation());
                }else{
                    if(MainActivity.range > getDistance(MainActivity.user.getLatLng(), new LatLng(g.getLocLat(), g.getLocLon()))){
                        names.add(g.toStringRepresentation());
                    };
                }
            }


        }
        return names;
    }

    public static int getDistance(LatLng loc1, LatLng loc2){
        Location l1 = new Location("buf");
        Location l2 = new Location("buf");
        l1.setLatitude(loc1.latitude);
        l1.setLongitude(loc1.longitude);
        l2.setLatitude(loc2.latitude);
        l2.setLongitude(loc2.longitude);
        return (int)l1.distanceTo(l2);
    }
    public static void removeDuplicates(ArrayList<User> list){
        List<User> allUsers = list;
        List<User> noRepeat = new ArrayList<User>();

        for (User event : allUsers) {
            boolean isFound = false;
            for (User u : noRepeat) {
                if (u.getName().equals(event.getName())) {
                    isFound = true;
                    break;
                }
            }
            if (!isFound) noRepeat.add(event);
        }
        if(MainActivity.isPublic){
            getCurrentPublicGroup().getUserList().clear();
            getCurrentPublicGroup().getUserList().addAll(noRepeat);
        }else{
            getCurrentPrivateGroup().getUserList().clear();
            getCurrentPrivateGroup().getUserList().addAll(noRepeat);
        }
        MainActivity.myRef.child(GroupActivity.groupsReference).child(MainActivity.groupName).child("userList").setValue(noRepeat);
        //return new ArrayList<User>(noRepeat);
    }

}
