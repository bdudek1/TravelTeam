package pl.travel.travelteam;

import android.widget.Toast;

public class WrongPasswordException extends RuntimeException {
    public WrongPasswordException(String message){
        super(message);
        Toast.makeText(MainActivity.context, message,
                Toast.LENGTH_LONG).show();
    }
    public WrongPasswordException(){
        super("Wrong password!");
        Toast.makeText(MainActivity.context, "Wrong password!",
                Toast.LENGTH_LONG).show();
    }
}
