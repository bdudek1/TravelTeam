package pl.travel.travelteam;

import android.widget.Toast;

public class TooLongNameException extends RuntimeException {
    public TooLongNameException(String message){
        super(message);
        Toast.makeText(MainActivity.context, message,
                Toast.LENGTH_LONG).show();
    }
}
