package pl.travel.travelteam;

import android.location.Location;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class FunHolder {
    public static void initInfo() {
        MainActivity.userName.setText(MainActivity.user.getName());
        MainActivity.userName.invalidate();
        MainActivity.informacje.setVisibility(View.VISIBLE);
        MainActivity.informationButton.setVisibility(View.VISIBLE);
        MainActivity.groupType.setVisibility(View.INVISIBLE);
        MainActivity.rangeBar.setVisibility(View.INVISIBLE);
        MainActivity.findTeamButton.setVisibility(View.INVISIBLE);
        MainActivity.createTeamButton.setVisibility(View.INVISIBLE);
        MainActivity.enterNick.setVisibility(View.VISIBLE);
        MainActivity.userName.setVisibility(View.VISIBLE);
        MainActivity.changeNickButton.setVisibility(View.VISIBLE);
    }

    public static void initGroups() {
        MainActivity.informacje.setVisibility(View.INVISIBLE);
        MainActivity.informationButton.setVisibility(View.INVISIBLE);
        MainActivity.findTeamButton.setVisibility(View.VISIBLE);
        MainActivity.rangeBar.setVisibility(View.VISIBLE);
        MainActivity.createTeamButton.setVisibility(View.VISIBLE);
        MainActivity.enterNick.setVisibility(View.INVISIBLE);
        MainActivity.userName.setVisibility(View.INVISIBLE);
        MainActivity.changeNickButton.setVisibility(View.INVISIBLE);
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

    public static void initChat() {
        GroupActivity.sendButton.setVisibility(View.VISIBLE);
        GroupActivity.chatView.setVisibility(View.VISIBLE);
        GroupActivity.messageEtext.setVisibility(View.VISIBLE);
        GroupActivity.usersListView.setVisibility(View.INVISIBLE);
        GroupActivity.mapFragment.getView().setVisibility(View.INVISIBLE);
        GroupActivity.mapFragment.getView().setVisibility(View.GONE);
    }

    public static void initUsersList() {
        GroupActivity.sendButton.setVisibility(View.INVISIBLE);
        GroupActivity.chatView.setVisibility(View.INVISIBLE);
        GroupActivity.messageEtext.setVisibility(View.INVISIBLE);
        GroupActivity.usersListView.setVisibility(View.VISIBLE);
        GroupActivity.mapFragment.getView().setVisibility(View.INVISIBLE);
        GroupActivity.mapFragment.getView().setVisibility(View.GONE);
    }

    public static void initMap() {
        GroupActivity.sendButton.setVisibility(View.INVISIBLE);
        GroupActivity.chatView.setVisibility(View.INVISIBLE);
        GroupActivity.messageEtext.setVisibility(View.INVISIBLE);
        GroupActivity.usersListView.setVisibility(View.INVISIBLE);
        GroupActivity.mapFragment.getView().setVisibility(View.VISIBLE);
    }

    public static PublicGroup getCurrentPublicGroup() {
        return MainActivity.currentPublicGroup;
    }

    public static void setCurrentPublicGroup(PublicGroup g) {
        MainActivity.currentPublicGroup = g;
    }

    public static PrivateGroup getCurrentPrivateGroup() {
        return MainActivity.currentPrivateGroup;
    }

    public static void setCurrentPrivateGroup(PrivateGroup g) {
        MainActivity.currentPrivateGroup = g;
    }

    public static List<String> getGroupNames() {
        List<String> buf = new ArrayList<>();
        try {
            buf = MainActivity.futureNames.get();
            MainActivity.executorService.shutdown();
            return buf;
        } catch (ExecutionException | InterruptedException | NullPointerException e) {
            System.out.println(e.getMessage());
            return buf;
        }
    }

    public static int getDistance(LatLng loc1, LatLng loc2) {
        Location l1 = new Location("buf");
        Location l2 = new Location("buf");
        l1.setLatitude(loc1.latitude);
        l1.setLongitude(loc1.longitude);
        l2.setLatitude(loc2.latitude);
        l2.setLongitude(loc2.longitude);
        return (int) l1.distanceTo(l2);
    }

    public static String encrypt(String strClearText, String key) throws Exception {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(key);
        String encrypted = encryptor.encrypt(strClearText);
        return encrypted;
    }

    public static String decrypt(String strEncrypted, String key, boolean isMessage) throws Exception {
        if(isMessage){
            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setPassword(key);
            String user = strEncrypted.substring(0, strEncrypted.indexOf(' ') + 1);
            strEncrypted = strEncrypted.substring(strEncrypted.indexOf(' '));
            String decrypted = encryptor.decrypt(strEncrypted);
            return user + decrypted;
        }else{
            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setPassword(key);
            String password = encryptor.decrypt(strEncrypted);
            return password;
        }
    }
}
