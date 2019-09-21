package com.example.stratelotek;

import android.widget.Toast;

public class SameNameUserException extends RuntimeException {
    public SameNameUserException(String message){
        super(message);
        Toast.makeText(MainActivity.context, message,
                Toast.LENGTH_LONG).show();
    }
}
