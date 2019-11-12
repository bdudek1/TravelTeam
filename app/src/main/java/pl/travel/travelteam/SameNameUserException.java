package pl.travel.travelteam;

public class SameNameUserException extends RuntimeException {
    public SameNameUserException(String message){
        super(message);
    }
}
