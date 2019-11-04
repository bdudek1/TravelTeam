package com.example.stratelotek;

import android.os.Handler;
import android.os.Looper;

import com.example.stratelotek.FunHolder;
import com.example.stratelotek.GroupActivity;
import com.example.stratelotek.MainActivity;

public class ActivityChecker extends Thread {
    public Handler mHandler;

    public void run() {
        //Looper.prepare();
        try{
            if(GroupActivity.lifeTime>0){
                this.sleep(1000);
                GroupActivity.lifeTime--;
            }else{
                if(MainActivity.isPublic){
                    FunHolder.getCurrentPublicGroup().destroyGroup();
                }else{
                    FunHolder.getCurrentPrivateGroup().destroyGroup();                    }
            }

        }catch(InterruptedException e){
            e.getMessage();
        }

        Looper.loop();
    }
}