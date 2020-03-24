package pl.travel.travelteam;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Assert;
import org.junit.Test;

import pl.travel.travelteam.FunHolder;
import pl.travel.travelteam.PrivateGroup;
import pl.travel.travelteam.PublicGroup;
import pl.travel.travelteam.SameNameUserException;
import pl.travel.travelteam.User;
import pl.travel.travelteam.WrongPasswordException;

import static org.junit.Assert.*;

public class FunHolderTest {
    @Test
    public void getDistanceTest(){
            assertEquals(0, FunHolder.getDistance(new LatLng(52.1347, 21.0042), new LatLng(52.1347, 21.0042)));

            int x = FunHolder.getDistance(new LatLng(52.1347, 21.0042), new LatLng(52.2424, 16.5547))/1000;
            assertTrue(x >303 && x < 305);
    }

    @Test
    public void groupJoiningTest(){
        PrivateGroup g = new PrivateGroup("Group,", "abc");
        try{
            g.addUser(new User("User"), "cba");
            Assert.fail();
        }catch(WrongPasswordException e){
            e.getMessage();
        }

        try{
            assertTrue(g.addUser(new User("User"), "abc"));
            assertFalse(g.addUser(new User("User"), "abc"));
        }catch(SameNameUserException | WrongPasswordException e){

        }


        g.getUserList().clear();
        g.addUser(new User("aad"), "abc");
        g.addUser(new User("user11"), "abc");
        g.addUser(new User("Mateusz"), "abc");
        assertEquals(3, g.getUserList().size());

        PublicGroup pg = new PublicGroup("PublicGroup");
        boolean bool = false;
        try{
            assertTrue(pg.addUser(new User("User")));
            assertFalse(pg.addUser(new User("User")));
        }catch(SameNameUserException e){

        }



        pg.getUserList().clear();
        pg.addUser(new User("aad"));
        pg.addUser(new User("user11"));
        pg.addUser(new User("Mateusz"));
        assertEquals(3, g.getUserList().size());

    }

    @Test
    public void userEqualsTest(){
        User u1 = new User("user");
        User u2 = new User("user");
        User u3 = new User("user3");
        assertTrue(u1.equals(u2));
        assertFalse(u1.equals(u3));
        assertFalse(u2.equals(u3));
    }

    @Test
    public void encryptionTest(){
        String s1 = "2323gegrg";
        String s2 = "222......   33";
        String s3 = "           ";
        try{
            assertTrue(FunHolder.decrypt(FunHolder.encrypt(s1, "key"), "key").equals(s1));
            assertTrue(FunHolder.decrypt(FunHolder.encrypt(s2, "key"), "key").equals(s2));
            assertTrue(FunHolder.decrypt(FunHolder.encrypt(s3, "key"), "key").equals(s3));
            assertFalse(FunHolder.decrypt(FunHolder.encrypt(s1, "key"), "key").equals(s2));
            assertFalse(FunHolder.decrypt(FunHolder.encrypt(s2, "key"), "key").equals(s3));
            assertFalse(FunHolder.decrypt(FunHolder.encrypt(s1, "key"), "key").equals(s3));
        }catch(Exception e){

        }

    }

}