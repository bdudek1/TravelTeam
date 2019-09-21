package com.example.stratelotek.ui.group;

import com.example.stratelotek.User;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Message {
    public String text;
    public Message(){
        this.text = "Default message";
    }
    public Message(User u, String text){
        this.text = u.getName() +": " + text;
    }

    @Override
    public String toString(){
        return text;
    }
}
