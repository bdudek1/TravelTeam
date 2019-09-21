package com.example.stratelotek;

import android.widget.Toast;

public class SameGroupNameException extends RuntimeException {
    public SameGroupNameException(String message){
        super(message);
        Toast.makeText(MainActivity.context, message,
                Toast.LENGTH_LONG).show();
    }
}
