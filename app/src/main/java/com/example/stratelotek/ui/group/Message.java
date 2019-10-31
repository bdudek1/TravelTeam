package com.example.stratelotek.ui.group;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.example.stratelotek.MainActivity;
import com.example.stratelotek.User;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Message {
    public String text;
    //public SpannableString spannableText;
    public Message(){
        this.text = "Default message";
    }
    public Message(User u, String msg){
        msg = u.getName() +": " + msg;
        this.text = msg;
        //this.spannableText =string;
    }
    public Message(String msg){
        this.text = msg.trim();
        //this.spannableText =string;
        }

    @Override
    public String toString(){
        return text;
    }

    public SpannableString toSpannableString(){
        String sBuf = text.substring(0, text.indexOf(':'));
        String sBuf2 = text.substring(text.indexOf(':'));
        SpannableString string = new SpannableString(sBuf + sBuf2);
        if(sBuf.equals(MainActivity.user.getName())){
            string.setSpan(new ForegroundColorSpan(Color.RED), 0, text.indexOf(':'), 0);
        }else{
            string.setSpan(new ForegroundColorSpan(Color.BLUE), 0, text.indexOf(':'), 0);
        }
        return string;
    }
}
