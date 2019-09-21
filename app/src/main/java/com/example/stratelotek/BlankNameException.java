package com.example.stratelotek;

import android.widget.Toast;

public class BlankNameException extends RuntimeException {
    public BlankNameException(String message){
        super(message);
        Toast.makeText(MainActivity.context, message,
                Toast.LENGTH_LONG).show();
    }
}
