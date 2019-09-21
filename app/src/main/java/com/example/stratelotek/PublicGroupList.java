package com.example.stratelotek;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
@IgnoreExtraProperties
public class PublicGroupList {
    public ArrayList<PublicGroup> groupList = new ArrayList<PublicGroup>();

    public boolean addGroup(PublicGroup group, User user) throws SameGroupNameException{
        boolean isAdded = true;
//        for(PublicGroup g:groupList){
//            if(g.getName().equals(group.getName())){
//                isAdded = false;
//                throw new SameGroupNameException("Group with this name exists, please select another name.");
//            }
//        }
        //group.addUser(user);
        if(isAdded){
            groupList.add(group);
        }
        return isAdded;
    }

    public boolean addGroup(PublicGroup group) throws SameGroupNameException{
        boolean isAdded = true;
        for(PublicGroup g:groupList){
            try{
                if(g.getName().equals(group.getName())){
                    isAdded = false;
                    throw new SameGroupNameException("Group with this name exists, please select another name.");
                }
            }catch (NullPointerException e){
                e.getMessage();
            }

        }
        if(isAdded){
            groupList.add(group);
        }
        return isAdded;
    }

    public void addGroups(Map<String, PublicGroup> groupCollection){
        groupList.addAll((ArrayList<PublicGroup>)groupCollection.values());
    }

    public void removeGroup(PublicGroup group){
        groupList.remove(group);
    }

    public ArrayList<PublicGroup> getGroupList(){
        return groupList;
    }
    public List<String> getNamesOfGroups(){
        List<String> namesList = new ArrayList<>();
        for(PublicGroup name:groupList){
            namesList.add(name.getName());
        }
        return namesList;
    }
    public boolean tryToDestroyGroup(){
        return groupList.removeIf(g -> g.isEmpty());
    }


    public static Predicate<PrivateGroup> isPrivateGroupEmpty()
    {
        return p -> p.getUserList().isEmpty();
    }

    public ArrayList<PublicGroup> getGroups(){
        return groupList;
    }
}
