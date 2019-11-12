package pl.travel.travelteam;

public class WrongPasswordException extends RuntimeException {
    public WrongPasswordException(String message){
        super(message);
        //Toast.makeText(MainActivity.context, message,
        //        Toast.LENGTH_LONG).show();
    }
}
