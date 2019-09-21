package com.example.stratelotek;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class FunHolder {
    public static void initInfo(){
        MainActivity.userName.setText(MainActivity.user.getName());
        MainActivity.userName.invalidate();
        MainActivity.informacje.setVisibility(View.VISIBLE);
        MainActivity.buttonInfo.setVisibility(View.VISIBLE);
        MainActivity.typGrupy.setVisibility(View.INVISIBLE);
        MainActivity.zasiegBar.setVisibility(View.INVISIBLE);
        MainActivity.szukajDruzyny.setVisibility(View.INVISIBLE);
        MainActivity.stworzDruzyne.setVisibility(View.INVISIBLE);
        MainActivity.wprowadzNick.setVisibility(View.VISIBLE);
        MainActivity.userName.setVisibility(View.VISIBLE);
        MainActivity.zmienNick.setVisibility(View.VISIBLE);
    }
    public static void initGroups(){
        MainActivity.informacje.setVisibility(View.INVISIBLE);
        MainActivity.buttonInfo.setVisibility(View.INVISIBLE);
        MainActivity.szukajDruzyny.setVisibility(View.VISIBLE);
        MainActivity.zasiegBar.setVisibility(View.VISIBLE);
        MainActivity.stworzDruzyne.setVisibility(View.VISIBLE);
        MainActivity.wprowadzNick.setVisibility(View.INVISIBLE);
        MainActivity.userName.setVisibility(View.INVISIBLE);
        MainActivity.zmienNick.setVisibility(View.INVISIBLE);
    }

    public static void adjustGravity(View v) {
        if (v.getId() == com.google.android.material.R.id.smallLabel) {
            ViewGroup parent = (ViewGroup) v.getParent();
            parent.setPadding(0, 0, 0, 0);

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) parent.getLayoutParams();
            params.gravity = Gravity.CENTER;
            parent.setLayoutParams(params);
        }

        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;

            for (int i = 0; i < vg.getChildCount(); i++) {
                adjustGravity(vg.getChildAt(i));
            }
        }
    }

    public static void initChat(){
        GroupActivity.sendButton.setVisibility(View.VISIBLE);
        GroupActivity.chat.setVisibility(View.VISIBLE);
        GroupActivity.messageEtext.setVisibility(View.VISIBLE);
        GroupActivity.listaUzytkownikow.setVisibility(View.INVISIBLE);
        GroupActivity.mapFragment.getView().setVisibility(View.INVISIBLE);
        GroupActivity.mapFragment.getView().setVisibility(View.GONE);
    }

    public static void initUsersList(){
        GroupActivity.sendButton.setVisibility(View.INVISIBLE);
        GroupActivity.chat.setVisibility(View.INVISIBLE);
        GroupActivity.messageEtext.setVisibility(View.INVISIBLE);
        GroupActivity.listaUzytkownikow.setVisibility(View.VISIBLE);
        GroupActivity.mapFragment.getView().setVisibility(View.INVISIBLE);
        GroupActivity.mapFragment.getView().setVisibility(View.GONE);
    }

    public static void initMap(){
        GroupActivity.sendButton.setVisibility(View.INVISIBLE);
        GroupActivity.chat.setVisibility(View.INVISIBLE);
        GroupActivity.messageEtext.setVisibility(View.INVISIBLE);
        GroupActivity.listaUzytkownikow.setVisibility(View.INVISIBLE);
        GroupActivity.mapFragment.getView().setVisibility(View.VISIBLE);
    }

    public static PublicGroup getCurrentPublicGroup(){
        return MainActivity.publicGroupList.getGroups().get(MainActivity.publicGroupList.getNamesOfGroups().indexOf(MainActivity.groupName));
    }

    public static PrivateGroup getCurrentPrivateGroup(){
        return MainActivity.privateGroupList.getPrivateGroups().get(MainActivity.privateGroupList.getNamesOfGroups().indexOf(MainActivity.groupName));
    }

}
