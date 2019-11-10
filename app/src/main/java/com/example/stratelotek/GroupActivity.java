package com.example.stratelotek;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.example.stratelotek.ui.group.Message;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stratelotek.ui.group.GroupFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.PermissionListener;


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.stratelotek.MainActivity.database;


public class GroupActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static Context context;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private static RecyclerViewAdapter.ItemClickListener listenerContext;
    private static RecyclerViewAdapterChat.ItemClickListener listenerContextChat;
    private static BottomNavigationView navigation;
    static TextView tytul;
    static TextView nazwaGrupy;
    static EditText messageEtext;
    public static FloatingActionButton sendButton;
    public static RecyclerView chat;
    public static RecyclerView listaUzytkownikow;
    private static RecyclerViewAdapter adapter;
    private static RecyclerViewAdapterChat adapterChat;
    private static List<Message> messages;
    private static List<SpannableString> spannableMessages;
    private static List<String> users;
    public static String groupsReference;
    private static GoogleMap mMap;
    private static OnMapReadyCallback mapCallback;
    public static SupportMapFragment mapFragment;
    public final static List<Message> msgsBuf = new ArrayList<>();

    private static GoogleApiClient mGoogleApiClient;
    private static Location mLocation;
    private static LocationManager mLocationManager;
    private static LocationRequest mLocationRequest;
    private static com.google.android.gms.location.LocationListener listener;
    private static long UPDATE_INTERVAL = 1500;  /* 10 secs */
    private static long FASTEST_INTERVAL = 7500; /* 20 sec */

    private static LatLng latLng;
    private static boolean isPermission;
    private static boolean isMapReady = false;
    public static DatabaseReference messageRef;
    public static DatabaseReference userRef;
    private static DatabaseReference messageCounterRef;


    static ValueEventListener usersListener;
    static ValueEventListener messagesListener;

    private static final String MAP_VIEW_BUNDLE_KEY = "xxxxxx";

    public class SectionsPagerAdapter extends FragmentPagerAdapter{

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppLotek);
        setContentView(R.layout.group_activity);
        Intent stickyService = new Intent(this, StickyService.class);
        startService(stickyService);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_group, GroupFragment.newInstance())
                    .commitNow();
        }
        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapCallback = GroupActivity.this;
        context = GroupActivity.this;
        messages = new ArrayList<>();
        users = new ArrayList<>();
        if(MainActivity.isPublic){
            messageRef = database.getReference("public_groups/"+MainActivity.groupName+"/messages");
            messageCounterRef = database.getReference("public_groups/"+MainActivity.groupName+"/messageCounter");
            userRef = database.getReference("public_groups/"+MainActivity.groupName+"/userList");
            groupsReference = "public_groups";
        }else{
            messageRef = database.getReference("private_groups/"+MainActivity.groupName+"/messages");
            messageCounterRef = database.getReference("public_groups/"+MainActivity.groupName+"/messageCounter");
            userRef = database.getReference("private_groups/"+MainActivity.groupName+"/userList");
            groupsReference = "private_groups";

        }
        messageRef.keepSynced(false);
        messageCounterRef.keepSynced(false);
        userRef.keepSynced(false);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager)findViewById(R.id.container_group);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        if (requestSinglePermission()) {


            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            checkLocation();
        }
    }
    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";


        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {

        }

        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.group_fragment, container, false);

            Bundle mapViewBundle = null;
            if (savedInstanceState != null) {
                mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
            }

            mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.mapView);
            mapFragment.getMapAsync(mapCallback);

            navigation = rootView.findViewById(R.id.bottomNavigationInGroup);
            navigation.setSelectedItemId(R.id.users_list);
            FunHolder.adjustGravity(navigation);
            navigation.setOnClickListener(null);
            messageEtext = rootView.findViewById(R.id.sendMessage);
            sendButton = rootView.findViewById(R.id.sendButton);
            chat = rootView.findViewById(R.id.recyclerChat);
            tytul = rootView.findViewById(R.id.textTitle);
            tytul.setTextSize(32);
            nazwaGrupy = rootView.findViewById(R.id.groupName);
            nazwaGrupy.setTextSize(18);
            nazwaGrupy.setText(MainActivity.groupName);
            listaUzytkownikow = rootView.findViewById(R.id.recyclerUsersList);
            chat = rootView.findViewById(R.id.recyclerChat);
            listaUzytkownikow.setLayoutManager(new LinearLayoutManager(context));
            chat.setLayoutManager(new LinearLayoutManager(context));
            adapterChat = new RecyclerViewAdapterChat(context, new ArrayList<SpannableString>());
            LinkedHashSet<String> hashSet;
            if(MainActivity.isPublic){
                hashSet = new LinkedHashSet<>(FunHolder.getCurrentPublicGroup().getUserNames());
            }else{
                hashSet = new LinkedHashSet<>(FunHolder.getCurrentPrivateGroup().getUserNames());
            }

            ArrayList<String> listWithoutDuplicates = new ArrayList<>(hashSet);
            spannableMessages = new ArrayList<>();
            adapter = new RecyclerViewAdapter(context, listWithoutDuplicates);
            adapter.setClickListener(listenerContext);
            adapterChat.setClickListener(listenerContextChat);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(listaUzytkownikow.getContext(),
                    getResources().getConfiguration().orientation);
            listaUzytkownikow.addItemDecoration(dividerItemDecoration);


            for (int i = 0; i < navigation.getMenu().size(); i++) {
                navigation.getMenu().getItem(i).setEnabled(false);
            }

            messagesListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> dataChildren = dataSnapshot.getChildren();
                    users.clear();
                    messages.clear();
                    spannableMessages.clear();
                    if(MainActivity.isPublic){
                        FunHolder.getCurrentPublicGroup().getMessages().clear();
                    }else{
                        FunHolder.getCurrentPrivateGroup().getMessages().clear();
                    }

                    for(DataSnapshot d:dataChildren){
                        if(MainActivity.isPublic){
                            FunHolder.getCurrentPublicGroup().getMessages().add(d.getValue(Message.class));
                        }else{
                            FunHolder.getCurrentPrivateGroup().getMessages().add(d.getValue(Message.class));
                        }

                        messages.add(d.getValue(Message.class));
                        spannableMessages.add(d.getValue(Message.class).toSpannableString());
                    }
                    if(MainActivity.isPublic){
                        FunHolder.getCurrentPublicGroup().messageCounter = messages.size();
                    }else{
                        FunHolder.getCurrentPrivateGroup().messageCounter = messages.size();
                    }

                    if(messages.size()>0){
                        if(MainActivity.isPublic){
                            FunHolder.getCurrentPublicGroup().messagesBuf = messages;
                        }else{
                            FunHolder.getCurrentPrivateGroup().messagesBuf = messages;
                        }
                    }


                    if(MainActivity.isPublic){
                        if(FunHolder.getCurrentPublicGroup().getMessages()!=null){
                            adapterChat = new RecyclerViewAdapterChat(context, spannableMessages);
                            adapterChat.setClickListener(listenerContextChat);
                            adapterChat.notifyDataSetChanged();
                            chat.setAdapter(adapterChat);
                            chat.invalidate();
                        }
                    }else{
                        if(FunHolder.getCurrentPrivateGroup().getMessages()!=null){
                            adapterChat = new RecyclerViewAdapterChat(context, spannableMessages);
                            adapterChat.setClickListener(listenerContextChat);
                            adapterChat.notifyDataSetChanged();
                            chat.setAdapter(adapterChat);
                            chat.invalidate();
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            };
            messageRef.addValueEventListener(messagesListener);

            usersListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mapFragment = mapFragment.newInstance();
                    Iterable<DataSnapshot> dataChildren = dataSnapshot.getChildren();

                    if(MainActivity.isPublic){
                        FunHolder.getCurrentPublicGroup().getUserList().clear();
                    }else{
                        FunHolder.getCurrentPrivateGroup().getUserList().clear();
                    }
                    try{
                        mapFragment = (SupportMapFragment) getChildFragmentManager()
                                .findFragmentById(R.id.mapView);
                        mapFragment.getMapAsync(mapCallback);
                    }catch(IllegalStateException e){
                        e.getMessage();
                    }


                    for(DataSnapshot d:dataChildren){
                        if(MainActivity.isPublic){
                            FunHolder.getCurrentPublicGroup().getUserList().add(d.getValue(User.class));
                        }else{
                            FunHolder.getCurrentPrivateGroup().getUserList().add(d.getValue(User.class));
                        }
                    }

                    if(MainActivity.isPublic && FunHolder.getCurrentPublicGroup().getUserList().size()>0){
                        if(FunHolder.getCurrentPublicGroup().locLat == 0.0 || FunHolder.getCurrentPublicGroup().locLon == 0.0){
                            FunHolder.getCurrentPublicGroup().setLatLng(new LatLng(FunHolder.getCurrentPublicGroup().getUserList().get(0).locLat, FunHolder.getCurrentPublicGroup().getUserList().get(0).locLon));
                        }
                        MainActivity.myRef.child(groupsReference).child(FunHolder.getCurrentPublicGroup().getName()).child("locLat").setValue(FunHolder.getCurrentPublicGroup().getLocLat());
                        MainActivity.myRef.child(groupsReference).child(FunHolder.getCurrentPublicGroup().getName()).child("locLon").setValue(FunHolder.getCurrentPublicGroup().getLocLon());
                        MainActivity.myRef.child(groupsReference).child(FunHolder.getCurrentPublicGroup().getName()).child("name").setValue(FunHolder.getCurrentPublicGroup().getName());
                        MainActivity.myRef.child(groupsReference).child(FunHolder.getCurrentPublicGroup().getName()).child("groupId").setValue(FunHolder.getCurrentPublicGroup().getGroupId());
                        MainActivity.myRef.child(groupsReference).child(FunHolder.getCurrentPublicGroup().getName()).child("range").setValue(FunHolder.getCurrentPublicGroup().range);
                        if(!MainActivity.publicGroupList.contains(FunHolder.getCurrentPublicGroup())){
                            MainActivity.publicGroupList.add(FunHolder.getCurrentPublicGroup());
                        }
                    }else if(!MainActivity.isPublic && FunHolder.getCurrentPrivateGroup().getUserList().size()>0){
                        if(FunHolder.getCurrentPrivateGroup().locLat == 0){
                            FunHolder.getCurrentPrivateGroup().setLatLng(FunHolder.getCurrentPrivateGroup().getUserList().get(0).getLatLng());
                        }
                        MainActivity.myRef.child(groupsReference).child(FunHolder.getCurrentPrivateGroup().getName()).child("locLat").setValue(FunHolder.getCurrentPrivateGroup().getLocLat());
                        MainActivity.myRef.child(groupsReference).child(FunHolder.getCurrentPrivateGroup().getName()).child("locLon").setValue(FunHolder.getCurrentPrivateGroup().getLocLon());
                        MainActivity.myRef.child(groupsReference).child(FunHolder.getCurrentPrivateGroup().getName()).child("name").setValue(FunHolder.getCurrentPrivateGroup().getName());
                        MainActivity.myRef.child(groupsReference).child(FunHolder.getCurrentPrivateGroup().getName()).child("groupId").setValue(FunHolder.getCurrentPrivateGroup().getGroupId());
                        MainActivity.myRef.child(groupsReference).child(FunHolder.getCurrentPrivateGroup().getName()).child("range").setValue(FunHolder.getCurrentPrivateGroup().range);
                        MainActivity.myRef.child(groupsReference).child(FunHolder.getCurrentPrivateGroup().getName()).child("password").setValue(FunHolder.getCurrentPrivateGroup().getPassword());
                        if(!MainActivity.privateGroupList.contains(FunHolder.getCurrentPrivateGroup())){
                            MainActivity.privateGroupList.add(FunHolder.getCurrentPrivateGroup());
                        }
                    }


                    try{
                        LinkedHashSet<String> hashSet;
                        if(MainActivity.isPublic){
                            hashSet = new LinkedHashSet<>(FunHolder.getCurrentPublicGroup().getUserNames());
                        }else{
                            hashSet = new LinkedHashSet<>(FunHolder.getCurrentPrivateGroup().getUserNames());
                        }


                        ArrayList<String> listWithoutDuplicates = new ArrayList<>(hashSet);

                        adapter = new RecyclerViewAdapter(context, listWithoutDuplicates);
                        adapter.setClickListener(listenerContext);
                        adapter.notifyDataSetChanged();
                        listaUzytkownikow.setAdapter(adapter);
                        listaUzytkownikow.invalidate();


                    }catch(NullPointerException e){
                        e.getMessage();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            };
            userRef.addValueEventListener(usersListener);



            sendButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    messageEtext = rootView.findViewById(R.id.sendMessage);
                    messageEtext.invalidate();
                    if(messageEtext.getText().toString().equals("")){
                        Toast.makeText(context, "Please enter message.", Toast.LENGTH_SHORT).show();
                    }else{
                        if(MainActivity.isPublic){
                            FunHolder.getCurrentPublicGroup().addMessage(new Message(MainActivity.user , messageEtext.getText().toString()));
                        }else{
                            FunHolder.getCurrentPrivateGroup().addMessage(new Message(MainActivity.user , messageEtext.getText().toString()));
                        }
                        messageEtext.setText("");
                        messageEtext.invalidate();
                    }
                }
            });
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1: {
                    FunHolder.initUsersList();
                    navigation.setSelectedItemId(R.id.users_list);
                    tytul.setText("Users list");
                    adapter.setClickListener(listenerContext);
                    adapter.notifyDataSetChanged();
                    listaUzytkownikow.setLayoutManager(new LinearLayoutManager(context));
                    listaUzytkownikow.setAdapter(adapter);
                    listaUzytkownikow.invalidate();
                    break;
                }
                case 2: {
                    FunHolder.initMap();
                    navigation.setSelectedItemId(R.id.mapV);
                    tytul.setText("Map");
                    break;
                }
                case 3: {
                    FunHolder.initChat();
                    adapterChat = new RecyclerViewAdapterChat(context, spannableMessages);
                    adapterChat.setClickListener(listenerContextChat);
                    adapterChat.notifyDataSetChanged();
                    chat.setLayoutManager(new LinearLayoutManager(context));
                    chat.setAdapter(adapterChat);
                    chat.invalidate();
                      navigation.setSelectedItemId(R.id.chat);
                      tytul.setText("Chat");
                    break;
                }

            }
            return rootView;


        }
    }
    @Override
    public void onItemClick(View view, int position) {

    }
    @Override
    public void onBackPressed() {
        if(MainActivity.isPublic){
            msgsBuf.addAll(FunHolder.getCurrentPublicGroup().messagesBuf);
        }else{
            msgsBuf.addAll(FunHolder.getCurrentPrivateGroup().messagesBuf);
        }
        if(MainActivity.isPublic){
            FunHolder.getCurrentPublicGroup().removeUser(MainActivity.user);
        }else{
            FunHolder.getCurrentPrivateGroup().removeUser(MainActivity.user);
        }
        MainActivity.user.setName(MainActivity.user.getName());
        super.onBackPressed();

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onRestart(){
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        if(usersListener!=null && userRef!=null)
            userRef.addValueEventListener(usersListener);
        if(messageRef!=null && messagesListener!=null)
            messageRef.addValueEventListener(messagesListener);
        super.onRestart();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.clear();

        if (latLng != null && latLng.longitude != 0.0 && latLng.latitude != 0.0) {
            MainActivity.user.setLocation(latLng);

            if(isMapReady == false){
                try{
                    moveToCurrentLocation(MainActivity.user.getLatLng());
                }catch(NullPointerException e){
                    e.getMessage();
                }
            }

            if(MainActivity.isPublic){
                for(User u:FunHolder.getCurrentPublicGroup().getUserList()){
                    String s;
                    if(FunHolder.getDistance(MainActivity.user.getLatLng(), u.getLatLng())> 5000){
                        s  = "Distance: " + FunHolder.getDistance(MainActivity.user.getLatLng(), u.getLatLng())/1000 + "km";
                    }else{
                        s = "Distance: " + FunHolder.getDistance(MainActivity.user.getLatLng(), u.getLatLng()) + "m";
                    }
                    mMap.addMarker(new MarkerOptions()
                            .position(u.getLatLng())
                            .title(u.getName())
                            .snippet(s)
                            .icon(u.getName().equals(MainActivity.user.getName()) ? BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED):BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }
            }else{
                for(User u:FunHolder.getCurrentPrivateGroup().getUserList()){
                    String s;
                    if(FunHolder.getDistance(MainActivity.user.getLatLng(), u.getLatLng())> 5000){
                        s  = "Distance: " + FunHolder.getDistance(MainActivity.user.getLatLng(), u.getLatLng())/1000 + "km";
                    }else{
                        s = "Distance: " + FunHolder.getDistance(MainActivity.user.getLatLng(), u.getLatLng()) + "m";
                    }
                    mMap.addMarker(new MarkerOptions()
                            .position(u.getLatLng())
                            .title(u.getName())
                            .snippet(s)
                            .icon(u.getName().equals(MainActivity.user.getName()) ? BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED):BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }
            }


        }
        isMapReady = true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        startLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation == null) {
            startLocationUpdates();
        }
        if (mLocation != null) {
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("CONNECTION: ", "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("CONNECTION: ", "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location)  {
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if(latLng != null && latLng.latitude != 0.0 && latLng.longitude != 0.0){
            MainActivity.user.setLocation(latLng);
        }

    }

    protected void startLocationUpdates() {

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if(usersListener!=null && userRef!=null)
        userRef.removeEventListener(usersListener);
        if(messageRef!=null && messagesListener!=null)
        messageRef.removeEventListener(messagesListener);
        super.onStop();
    }


    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(usersListener!=null && userRef!=null)
        userRef.addValueEventListener(usersListener);
        if(messageRef!=null && messagesListener!=null)
        messageRef.addValueEventListener(messagesListener);
        if(mapFragment!=null){
            try{
                mapFragment.onResume();
            }catch(NullPointerException e){
                e.getMessage();
            }

        }


    }

    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean requestSinglePermission() {

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionRationaleShouldBeShown(com.karumi.dexter.listener.PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Toast.makeText(GroupActivity.this, "Single permission is granted!", Toast.LENGTH_SHORT).show();
                        isPermission = true;
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            isPermission = false;
                        }
                    }


                }).check();

        return isPermission;

    }

    public static void moveToCurrentLocation(LatLng currentLocation)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);


    }

    @Override
    public void onDestroy(){
        if(MainActivity.isPublic){
            FunHolder.getCurrentPublicGroup().removeUser(MainActivity.user);
            FunHolder.getCurrentPublicGroup().tryToDestroy();
            FunHolder.getCurrentPublicGroup().addMessages(msgsBuf);
        }else{
            FunHolder.getCurrentPrivateGroup().removeUser(MainActivity.user);
            FunHolder.getCurrentPrivateGroup().tryToDestroy();
            FunHolder.getCurrentPrivateGroup().addMessages(msgsBuf);
        }
        super.onDestroy();
    }

}
