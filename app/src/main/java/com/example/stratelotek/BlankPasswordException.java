package com.example.stratelotek;

import android.widget.Toast;

public class BlankPasswordException extends RuntimeException {
    public BlankPasswordException(String message){
        super(message);
        Toast.makeText(MainActivity.context, message,
                Toast.LENGTH_LONG).show();
    }
}
