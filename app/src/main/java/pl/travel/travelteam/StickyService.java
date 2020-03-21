package pl.travel.travelteam;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class StickyService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if(MainActivity.isPublic && FunHolder.getCurrentPublicGroup()!=null && FunHolder.getCurrentPublicGroup().getUserList().size()>1){
            GroupActivity.msgsBuf.addAll(FunHolder.getCurrentPublicGroup().messagesBuf);
        }else if(!MainActivity.isPublic && FunHolder.getCurrentPrivateGroup()!=null && FunHolder.getCurrentPrivateGroup().getUserList().size()>1){
            GroupActivity.msgsBuf.addAll(FunHolder.getCurrentPrivateGroup().messagesBuf);
        }

        if(MainActivity.isPublic && FunHolder.getCurrentPublicGroup()!=null){
            FunHolder.getCurrentPublicGroup().removeUser(MainActivity.user);
            FunHolder.getCurrentPublicGroup().tryToDestroy();
        }else if(FunHolder.getCurrentPrivateGroup()!=null){
            FunHolder.getCurrentPrivateGroup().removeUser(MainActivity.user);
            FunHolder.getCurrentPrivateGroup().tryToDestroy();
        }

//        if(MainActivity.isPublic){
//            FunHolder.removeDuplicates(FunHolder.getCurrentPublicGroup().getUserList());
//        }else{
//            FunHolder.removeDuplicates(FunHolder.getCurrentPrivateGroup().getUserList());
//        }


        if(MainActivity.isPublic && FunHolder.getCurrentPublicGroup()!=null && FunHolder.getCurrentPublicGroup().getUserList().size()<2){
            FunHolder.getCurrentPublicGroup().getUserList().clear();
        }else if(!MainActivity.isPublic && FunHolder.getCurrentPrivateGroup()!=null && FunHolder.getCurrentPrivateGroup().getUserList().size()<2){
            FunHolder.getCurrentPrivateGroup().getUserList().clear();
        }
        if(GroupActivity.usersListener!=null && GroupActivity.userRef!=null)
            GroupActivity.userRef.removeEventListener(GroupActivity.usersListener);
        if(GroupActivity.messageRef!=null && GroupActivity.messagesListener!=null)
            GroupActivity.messageRef.removeEventListener(GroupActivity.messagesListener);
    }
}