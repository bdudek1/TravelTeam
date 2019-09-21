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
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
import java.util.List;

import static com.example.stratelotek.MainActivity.database;

public class GroupActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    static Context context;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    static RecyclerViewAdapter.ItemClickListener listenerContext;
    static RecyclerViewAdapterChat.ItemClickListener listenerContextChat;
    static BottomNavigationView navigation;
    static TextView tytul;
    static TextView nazwaGrupy;
    static EditText messageEtext;
    static FloatingActionButton sendButton;
    static RecyclerView chat;
    static RecyclerView listaUzytkownikow;
    static RecyclerViewAdapter adapter;
    static RecyclerViewAdapterChat adapterChat;
    static List<String> messages;
    static List<String> users;
    public static GoogleMap mMap;
    public static OnMapReadyCallback mapCallback;
    public static SupportMapFragment mapFragment;

    public static GoogleApiClient mGoogleApiClient;
    private static Location mLocation;
    private static LocationManager mLocationManager;
    private static LocationRequest mLocationRequest;
    private static com.google.android.gms.location.LocationListener listener;
    private static long UPDATE_INTERVAL = 1000;  /* 10 secs */
    private static long FASTEST_INTERVAL = 5000; /* 20 sec */

    private static LatLng latLng;
    private static boolean isPermission;

    private static DatabaseReference messageRef;
    private static DatabaseReference userRef;

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
            messageRef = database.getReference("message/public_groups/"+MainActivity.groupName+"/messages");
            userRef = database.getReference("message/public_groups/"+MainActivity.groupName+"/userList");
        }else{
            messageRef = database.getReference("message/private_groups/"+MainActivity.groupName+"/messages");
            userRef = database.getReference("message/private_groups/"+MainActivity.groupName+"/userList");
        }

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
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
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
            nazwaGrupy.setTextSize(32);
            nazwaGrupy.setText(MainActivity.groupName);
            listaUzytkownikow = rootView.findViewById(R.id.recyclerUsersList);
            chat = rootView.findViewById(R.id.recyclerChat);
            listaUzytkownikow.setLayoutManager(new LinearLayoutManager(context));
            chat.setLayoutManager(new LinearLayoutManager(context));
            adapterChat = new RecyclerViewAdapterChat(context, new ArrayList<String>());
            if(MainActivity.isPublic){
                adapter = new RecyclerViewAdapter(context, FunHolder.getCurrentPublicGroup().getUserNames());
            }else{
                adapter = new RecyclerViewAdapter(context, FunHolder.getCurrentPrivateGroup().getUserNames());
            }
            //adapter = new RecyclerViewAdapter(context, new ArrayList<String>());
            adapter.setClickListener(listenerContext);
            adapterChat.setClickListener(listenerContextChat);
            //listaUzytkownikow.setAdapter(adapter);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(listaUzytkownikow.getContext(),
                    getResources().getConfiguration().orientation);
            listaUzytkownikow.addItemDecoration(dividerItemDecoration);


            for (int i = 0; i < navigation.getMenu().size(); i++) {
                navigation.getMenu().getItem(i).setEnabled(false);
            }

            messageRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<String> post = new ArrayList<>();
                    Iterable<DataSnapshot> dataChildren = dataSnapshot.getChildren();
                    users.clear();
                    for(DataSnapshot d:dataChildren){
                        post.add(d.getValue(Message.class).toString());
                        users.add(d.getValue(Message.class).toString());
                    }
                    System.out.println(post);
                    Toast.makeText(context, "Post: " + post, Toast.LENGTH_SHORT).show();
                    if(post!=null){
                        //messages = post;
                        adapterChat = new RecyclerViewAdapterChat(context, users);
                        adapterChat.setClickListener(listenerContextChat);
                        adapterChat.notifyDataSetChanged();
                        chat.setAdapter(adapterChat);
                        chat.invalidate();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> dataChildren = dataSnapshot.getChildren();
                    List<String> post = new ArrayList<>();
                    //users.clear();
                    for(DataSnapshot d:dataChildren){
                        post.add(d.getValue(User.class).toString());
                        //users.add(d.getValue(User.class).toString());
                    }
                    System.out.println(post);

 //                   if(post!=null){
//                        if(MainActivity.isPublic){
//                            adapter = new RecyclerViewAdapter(context, post);
//                        }else{
//                            adapter = new RecyclerViewAdapter(context, post);
//                        }
//                        adapter = MainActivity.isPublic ? new RecyclerViewAdapter(context, FunHolder.getCurrentPublicGroup().getUserNames()) : new RecyclerViewAdapter(context, FunHolder.getCurrentPrivateGroup().getUserNames());
                        adapter = new RecyclerViewAdapter(context, post);
                        adapter.setClickListener(listenerContext);
                        adapter.notifyDataSetChanged();
                        listaUzytkownikow.setAdapter(adapter);
                        listaUzytkownikow.invalidate();
//                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });



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
                    //adapter = MainActivity.isPublic ? new RecyclerViewAdapter(context, FunHolder.getCurrentPublicGroup().getUserNames()) : new RecyclerViewAdapter(context, FunHolder.getCurrentPrivateGroup().getUserNames());
                    //adapter = new RecyclerViewAdapter(context, users);
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
                    adapterChat = new RecyclerViewAdapterChat(context, messages);
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
            try{
                FunHolder.getCurrentPublicGroup().getUserList().removeIf(x -> x.getName().equals(MainActivity.user.getName()));
            }catch(ArrayIndexOutOfBoundsException e){
                e.getMessage();
            }
            if(MainActivity.publicGroupList.tryToDestroyGroup())
                PublicGroup.publicGroupCounter--;
        }else{
            try{
                FunHolder.getCurrentPrivateGroup().getUserList().removeIf(x -> x.getName().equals(MainActivity.user.getName()));
            }catch(ArrayIndexOutOfBoundsException e){
                e.getMessage();
            }
            if(MainActivity.privateGroupList.tryToDestroyGroup())
                PrivateGroup.privateGroupCounter--;
        }
        MainActivity.currentUserName = MainActivity.user.getName();
        MainActivity.userName.setText(MainActivity.currentUserName);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        if (latLng != null) {
            mMap.clear();
            MainActivity.user.setLocation(latLng);
            if(MainActivity.isPublic){
                try{
                    for(User u:FunHolder.getCurrentPublicGroup().getUserList()){
                        if(u.getLatLng()!=null)
                            mMap.addMarker(new MarkerOptions().position(u.getLatLng()).title(u.getName()));
                    }
                }catch(ArrayIndexOutOfBoundsException e){
                    e.getMessage();
                }

            }else{
                try{
                    for(User u:FunHolder.getCurrentPrivateGroup().getUserList()){
                        if(u.getLatLng()!=null)
                            mMap.addMarker(new MarkerOptions().position(u.getLatLng()).title(u.getName()));
                    }
                }catch(ArrayIndexOutOfBoundsException e){
                    e.getMessage();
                }

            }
            try{
                moveToCurrentLocation(MainActivity.user.getLatLng());
            }catch(NullPointerException e){
                e.getMessage();
            }

        }
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
        // You can now create a LatLng Object for use with maps
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MainActivity.user.setLocation(latLng);
        if(MainActivity.isPublic){
            MainActivity.myRef.child("public_groups").child(MainActivity.groupName).child("userList").child(Integer.toString(MainActivity.user.getUserNumber())).setValue(MainActivity.user);
        }else{
            MainActivity.myRef.child("prvate_groups").child(MainActivity.groupName).child("userList").child(Integer.toString(MainActivity.user.getUserNumber())).setValue(MainActivity.user);
        }
        String msg;

        try{
            msg = "User location: " +
                    MainActivity.user.getLatLng().toString();
        }catch(NullPointerException e){
            msg = "User location is null";
        }

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        if(MainActivity.isPublic){
            MainActivity.myRef.child("public_groups").child(MainActivity.groupName).child("userList").child(Integer.toString(MainActivity.user.getUserNumber())).child("location").setValue(MainActivity.user.getLatLng());
        }else{
            MainActivity.myRef.child("private_groups").child(MainActivity.groupName).child("userList").child(Integer.toString(MainActivity.user.getUserNumber())).child("location").setValue(MainActivity.user.getLatLng());
        }

        mapFragment.getMapAsync(mapCallback);

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
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        mapFragment.onStop();
    }

    @Override
    protected void onResume(){
        super.onResume();
        //mapFragment.onResume();
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
                        //Single Permission is granted
                        Toast.makeText(GroupActivity.this, "Single permission is granted!", Toast.LENGTH_SHORT).show();
                        isPermission = true;
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // check for permanent denial of permission
                        if (response.isPermanentlyDenied()) {
                            isPermission = false;
                        }
                    }


                }).check();

        return isPermission;

    }

    private void moveToCurrentLocation(LatLng currentLocation)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);


    }
}
