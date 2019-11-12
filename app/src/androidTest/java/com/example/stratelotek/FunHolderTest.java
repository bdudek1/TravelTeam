package com.example.stratelotek;

import android.util.Log;

import com.example.stratelotek.ui.group.Message;
import com.google.android.gms.maps.model.LatLng;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import static androidx.test.InstrumentationRegistry.getContext;
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
            g.addUser(new User("User"), "abc");
            g.addUser(new User("User"), "abc");
            Assert.fail();
        }catch(SameNameUserException e){
            e.getMessage();
        }

        g.getUserList().clear();
        g.addUser(new User("aad"), "abc");
        g.addUser(new User("user11"), "abc");
        g.addUser(new User("Mateusz"), "abc");
        assertEquals(3, g.getUserList().size());

        PublicGroup pg = new PublicGroup("PublicGroup");

        try{
            pg.addUser(new User("User"));
            pg.addUser(new User("User"));
            Assert.fail();
        }catch(SameNameUserException e){
            e.getMessage();
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

//    @Test
//    public void removeDuplicatesTest(){
//        ArrayList<User> userList = new ArrayList<>();
//        User u1 = new User("user1");
//        u1.locLat = 50.0;
//        u1.locLon = 50.0;
//        User u2 = new User("user1");
//        u1.locLat = 55.0;
//        u1.locLon = 55.0;
//        User u3 = new User("user3");
//        u1.locLat = 60.0;
//        u1.locLon = 60.0;
//
//        userList.add(u1);
//        userList.add(u2);
//        userList.add(u3);
//
//        userList = FunHolder.removeDuplicates(userList);
//        assertEquals(2, userList.size());
//        u2.setName("user2");
//
//        userList.clear();
//
//        userList.add(u1);
//        userList.add(u2);
//        userList.add(u3);
//
//        assertEquals(3, userList.size());
//    }
}