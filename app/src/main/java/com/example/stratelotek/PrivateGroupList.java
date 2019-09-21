package com.example.stratelotek;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;
@IgnoreExtraProperties
public class PrivateGroupList extends PublicGroupList {
    public ArrayList<PrivateGroup> groupListP = new ArrayList<PrivateGroup>();

    public ArrayList<PrivateGroup> getPrivateGroups(){
        return groupListP;
    }

    public boolean addGroup(PrivateGroup group, User u) throws SameGroupNameException{
        boolean isAdded = true;
        for(PrivateGroup g:groupListP){
            if(g.getName().equals(group.getName())){
                isAdded = false;
                throw new SameGroupNameException("Group with this name exists, please select another name.");
            }
        }
        group.addUser(u, group.getPassword());
        if(isAdded){
            groupListP.add(group);
        }
        return isAdded;
    }
    @Override
    public boolean tryToDestroyGroup(){
        return groupListP.removeIf(g -> g.isEmpty());
    }
    @Override
    public List<String> getNamesOfGroups(){
        List<String> namesList = new ArrayList<String>();
        for(PrivateGroup name:groupListP){
            namesList.add(name.getName());
        }
        return namesList;
    }


}
