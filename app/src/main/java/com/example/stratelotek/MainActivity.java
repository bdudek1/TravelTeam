package com.example.stratelotek;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.example.stratelotek.ui.group.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemClickListener, LocationListener {
    static TextView informacje;
    static TextView wprowadzNick;
    static TextView typGrupy;
    static Button szukajDruzyny;
    static Button stworzDruzyne;
    static Button zmienNick;
    static Button buttonInfo;
    static SeekBar zasiegBar;
    static Context context;
    static RecyclerView listaGrup;
    static RecyclerView listaGrupPrywatnych;
    static BottomNavigationView navigation;
    static RecyclerViewAdapter.ItemClickListener listenerContext;
    static RecyclerViewAdapter adapter;
    static RecyclerViewAdapter adapterPrywatnych;
    static PublicGroupList publicGroupList = new PublicGroupList();
    static PrivateGroupList privateGroupList = new PrivateGroupList();
    static String groupName;
    static String currentId;
    static boolean isPublic;
    static boolean isInPublicSection = false;
    public static User user;
    static TextView userName;
    static String currentUserName = "User";

    static public FirebaseDatabase database;
    static public DatabaseReference myRef;
    public static DatabaseReference publicGroupsRef;
    public static DatabaseReference privateGroupsRef;

    public static List<String> groupsList = new ArrayList<>();

    public static LocationManager locationManager;
    public static LocationListener locationListener;

    public Location currentLoc;



    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        listenerContext = this;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        user = new User(currentUserName);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");

        myRef.setValue("Groups database");

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Database error: ", "loadPost:onCancelled", databaseError.toException());
            }
        };

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                switch(position){
                    case 0:{
                        break;
                    }
                    case 1:{
                        isInPublicSection = true;
                        Toast.makeText(MainActivity.this,
                                "In public: " + isInPublicSection, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case 2:{
                        isInPublicSection = false;
                        Toast.makeText(MainActivity.this,
                                "In public: " + isInPublicSection, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        publicGroupsRef = database.getReference("message/public_groups");
        privateGroupsRef = database.getReference("message/private_groups");
        try{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }catch(SecurityException e){
            e.getMessage();
        }
        checkIfLocEnabled();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }


  

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 3;
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
        TextView dokladnosc;
        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            userName = rootView.findViewById(R.id.inputNickText);
            userName.setText(user.getName());
            listaGrup = rootView.findViewById(R.id.groupList);
            listaGrup.setLayoutManager(new LinearLayoutManager(context));
            adapter = new RecyclerViewAdapter(context, new ArrayList<String>());
            adapter.setClickListener(listenerContext);
            listaGrup.setAdapter(adapter);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(listaGrup.getContext(),
                    getResources().getConfiguration().orientation);
            listaGrup.addItemDecoration(dividerItemDecoration);

            adapterPrywatnych = new RecyclerViewAdapter(context, new ArrayList<String>());
            adapterPrywatnych.setClickListener(listenerContext);

            navigation = rootView.findViewById(R.id.bottomNavigation);
            navigation.setSelectedItemId(R.id.home);
            FunHolder.adjustGravity(navigation);
            navigation.setOnClickListener(null);

            for (int i = 0; i < navigation.getMenu().size(); i++) {
                navigation.getMenu().getItem(i).setEnabled(false);
            }

            listaGrupPrywatnych = rootView.findViewById(R.id.privateGroupList);
            listaGrupPrywatnych.setLayoutManager(new LinearLayoutManager(context));
            listaGrupPrywatnych.setAdapter(adapterPrywatnych);
            DividerItemDecoration privateDividerItemDecoration = new DividerItemDecoration(listaGrupPrywatnych.getContext(),
                    getResources().getConfiguration().orientation);
            listaGrupPrywatnych.addItemDecoration(privateDividerItemDecoration);

            listaGrup = rootView.findViewById(R.id.groupList);
            typGrupy = (TextView) rootView.findViewById(R.id.lotekName);
            dokladnosc = (TextView) rootView.findViewById(R.id.dokladnosc);
            wprowadzNick = (TextView) rootView.findViewById(R.id.wprowadzNick);
            informacje = (TextView) rootView.findViewById(R.id.opis);
            szukajDruzyny = rootView.findViewById(R.id.seekForTeam);
            stworzDruzyne = rootView.findViewById(R.id.createTeam);
            zmienNick = rootView.findViewById(R.id.buttonChangeName);
            wprowadzNick.setTextSize(32);
            typGrupy.setTextSize(32);
            userName.setTextSize(32);
            dokladnosc.setTextSize(18);
            informacje.setTextSize(16);
            dokladnosc.setText("No range limit");
            buttonInfo = rootView.findViewById(R.id.buttonInformacje);


            publicGroupsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> dataChildren = dataSnapshot.getChildren();
                    Map<String, PublicGroup> groups = new HashMap<String, PublicGroup>();
                    List<PublicGroup> gList = new ArrayList<>();
                    String currId;
                    groupsList.clear();
                    publicGroupList.getGroupList().clear();
                    for(DataSnapshot d:dataChildren){
                            groupsList.add(d.getValue(PublicGroup.class).toString());
                            publicGroupList.addGroup(d.getValue(PublicGroup.class));
                            //groups.put(d.getKey(), d.getValue(PublicGroup.class));
                        //if(d.getValue(PublicGroup.class)!=null)
                        //currId = getPublicGroup(d.getValue(PublicGroup.class));
                    }
                    publicGroupsInit();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });

            zasiegBar = rootView.findViewById(R.id.rangeBar);
            zasiegBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    dokladnosc.setText("Range: " + seekBar.getProgress() + "km");
                    if(seekBar.getProgress() == 0){
                        dokladnosc.setText("No range limit");
                    }
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                    dokladnosc.setText("Range: " + seekBar.getProgress() + "km");
                    if(seekBar.getProgress() == 0){
                        dokladnosc.setText("No range limit");
                    }
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                    dokladnosc.setText("Range: " + seekBar.getProgress() + "km");
                    if(seekBar.getProgress() == 0){
                        dokladnosc.setText("No range limit");
                    }
                }

            });
            stworzDruzyne.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                        Toast.makeText(context,"Please enable your GPS to use this feature." , Toast.LENGTH_SHORT).show();

                    }else{
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(getArguments().getInt(ARG_SECTION_NUMBER) == 2 ? "Create a public group" : "Create a private group").setView(getArguments().getInt(ARG_SECTION_NUMBER) == 2? R.layout.public_group_create : R.layout.private_group_create)
                                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if(getArguments().getInt(ARG_SECTION_NUMBER) == 2){
                                            Dialog dialogView = (Dialog) dialog;
                                            EditText name = (EditText)dialogView.findViewById(R.id.groupname);
                                            try{
                                                userName = (TextView) rootView.findViewById(R.id.inputNickText);
                                                userName.setText(user.getName());
                                                userName.invalidate();
                                                user.setName(userName.getText().toString());
                                                //publicGroupList.addGroup(new PublicGroup(name.getText().toString()), user);
                                                groupName = name.getText().toString();
                                                currentId = addPublicGroup(new PublicGroup(name.getText().toString()));
                                                isPublic = true;
                                                Toast.makeText(context, "Public group list size: "+publicGroupList.getGroups().size(), Toast.LENGTH_SHORT).show();
                                                Toast.makeText(context, "user.getName(): "+user.getName() + ", userName.getText().toString(): " + userName.getText().toString(), Toast.LENGTH_SHORT).show();
                                                changeActivity();
                                            }catch(SameGroupNameException e){

                                            }catch(BlankNameException e){

                                            }


                                        }else if(getArguments().getInt(ARG_SECTION_NUMBER) == 3){
//                                        isInPublicSection = false;
                                            Dialog dialogView = (Dialog) dialog;
                                            EditText name=(EditText)dialogView.findViewById(R.id.groupname);
                                            EditText password=(EditText)dialogView.findViewById(R.id.password);
                                            try{
                                                userName = (TextView) rootView.findViewById(R.id.inputNickText);
                                                userName.setText(user.getName());
                                                userName.invalidate();
                                                user.setName(userName.getText().toString());
                                                //privateGroupList.addGroup(new PrivateGroup(name.getText().toString(), password.getText().toString()), user);
                                                groupName = name.getText().toString();
                                                currentId = addPrivateGroup(new PrivateGroup(name.getText().toString(), password.getText().toString()));
                                                isPublic = false;
                                                Toast.makeText(context, "Private group list size: "+privateGroupList.getPrivateGroups().size(), Toast.LENGTH_SHORT).show();
                                                Toast.makeText(context, "user.getName(): "+user.getName() + ", userName.getText().toString(): " + userName.getText().toString(), Toast.LENGTH_SHORT).show();
                                                changeActivity();
                                            }catch(SameGroupNameException e){

                                            }catch(BlankPasswordException e){

                                            }catch(BlankNameException e){

                                            }

                                        }

                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                });
                        // Create the AlertDialog object and return it
                        builder.create();
                        builder.show();
                    }

                }

            });

            szukajDruzyny.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                        Toast.makeText(context,"Please enable your GPS to use this feature." , Toast.LENGTH_SHORT).show();
                    }else{
                        user.setName(userName.getText().toString());
                        switch(getArguments().getInt(ARG_SECTION_NUMBER)){
                            case 2:{
                                publicGroupsInit();
                                listaGrup = rootView.findViewById(R.id.groupList);
                                //publicGroupsInit();
                                break;
                            }
                            case 3:{
                                listaGrupPrywatnych = rootView.findViewById(R.id.privateGroupList);
                                privateGroupsInit();
                                break;
                            }
                        }
                    }

                }

            });

            zmienNick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("Enter user name:").setView(R.layout.user_name_change)
                            .setPositiveButton("Set name", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) throws BlankNameException {
                                        Dialog dialogView = (Dialog) dialog;
                                        EditText name = (EditText)dialogView.findViewById(R.id.userNameChange);
                                        try{
                                            if(name.getText().toString().equals("")){
                                                throw new BlankNameException("Please enter your nickname");
                                            }else{
                                                userName = (TextView) rootView.findViewById(R.id.inputNickText);
                                                userName.setText(name.getText().toString());
                                                userName.invalidate();
                                                user.setName(name.getText().toString());
                                            }

                                        }catch(BlankNameException e){

                                        }

                                        Toast.makeText(context, "user.getName(): "+user.getName() + ", userName.getText().toString(): " + userName.getText().toString(), Toast.LENGTH_SHORT).show();

                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });
                    // Create the AlertDialog object and return it
                    builder.create();
                    builder.show();
                }

            });

            switch(getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:{
                    FunHolder.initInfo();
                    navigation.setSelectedItemId(R.id.home);
                    dokladnosc.setVisibility(View.INVISIBLE);
                    listaGrup.setVisibility(View.INVISIBLE);
                    listaGrupPrywatnych.setVisibility(View.INVISIBLE);
                    break;
                }
                case 2:{
                    FunHolder.initGroups();
                    typGrupy.setText("Public groups");
                    navigation.setSelectedItemId(R.id.public_groups);
                    dokladnosc.setVisibility(View.VISIBLE);
                    listaGrup.setVisibility(View.VISIBLE);
                    listaGrupPrywatnych.setVisibility(View.INVISIBLE);
                    listaGrup = rootView.findViewById(R.id.groupList);
                    //publicGroupsInit();
                    break;
                }
                case 3:{
                    FunHolder.initGroups();
                    typGrupy.setText("Private groups");
                    navigation.setSelectedItemId(R.id.private_groups);
                    dokladnosc.setVisibility(View.VISIBLE);
                    listaGrup.setVisibility(View.INVISIBLE);
                    listaGrupPrywatnych.setVisibility(View.VISIBLE);
                    listaGrupPrywatnych = rootView.findViewById(R.id.privateGroupList);
                    privateGroupsInit();
                    break;
                }

            }
            return rootView;


        }



    }
    @Override
    public void onItemClick(View view, int position) {
        if(isInPublicSection){
            //publicGroupsInit();
            Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
            for(PublicGroup g : publicGroupList.getGroups()){
                if(g.toString().equals(adapter.getItem(position))){
                    try{
                        user.setName(userName.getText().toString());
                        groupName = g.getName();
                        currentId = g.getGroupId();
                        if(g.addUser(user)){
                            isPublic = true;
                            Toast.makeText(context, "Public group list size: "+publicGroupList.getGroups().size(), Toast.LENGTH_SHORT).show();
                            changeActivity();
                        }

                    }catch (SameNameUserException e){

                    }

                }
            }
        }else{
            privateGroupsInit();
            //Toast.makeText(this, "You clicked " + adapterPrywatnych.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Enter password").setView(R.layout.private_group_enter)
                    .setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Dialog dialogView = (Dialog) dialog;
                            EditText password=(EditText)dialogView.findViewById(R.id.passwordEntry);
                            Toast.makeText(context, "Private group list size: "+privateGroupList.getPrivateGroups().size(), Toast.LENGTH_SHORT).show();
                            for(PrivateGroup g : privateGroupList.getPrivateGroups()){
                                Toast.makeText(context, "true: " + g.getName().equals(adapterPrywatnych.getItem(position)) , Toast.LENGTH_SHORT).show();
                                if(g.getName().equals(adapterPrywatnych.getItem(position))){
                                    try{
                                        user.setName(userName.getText().toString());
                                        if(g.addUser(user, password.getText().toString())){
                                            groupName = g.getName();
                                            isPublic = false;
                                            changeActivity();
                                            //g.getName().equals(adapterPrywatnych.getItem(position))
                                        }

                                    }catch (SameNameUserException e){

                                    }catch (WrongPasswordException e){

                                    }

                                }
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            builder.create();
            builder.show();

        }

    }

    private static void publicGroupsInit(){
        listaGrup.setLayoutManager(new LinearLayoutManager(context));
        groupsList.removeIf(x -> x.equals("null null"));
        adapter = new RecyclerViewAdapter(context, groupsList);
        System.out.println(groupsList);
        adapter.setClickListener(listenerContext);
        adapter.notifyDataSetChanged();
        listaGrup.setAdapter(adapter);
        listaGrup.invalidate();
    }

    private static void privateGroupsInit(){
        listaGrupPrywatnych.setLayoutManager(new LinearLayoutManager(context));
        adapterPrywatnych = new RecyclerViewAdapter(context, privateGroupList.getNamesOfGroups());
        adapterPrywatnych.setClickListener(listenerContext);
        adapterPrywatnych.notifyDataSetChanged();
        listaGrupPrywatnych.setAdapter(adapterPrywatnych);
        listaGrupPrywatnych.invalidate();
    }

    private static void changeActivity() {
        Intent myIntent = new Intent(context, GroupActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);
        //finish();
    }

    private void checkIfLocEnabled(){
        if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("GPS is not enabled.")
                    .setMessage("Please enable GPS to use this app.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            System.exit(0);
                        }
                    })
                    .show();
        }
    }

    private static String addPublicGroup(PublicGroup g){
        publicGroupList.addGroup(g, user);
        myRef.child("public_groups").child(g.getName()).setValue(g).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Success in adding group to database", Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failure in adding group to database", Toast.LENGTH_SHORT).show();
                    }
                });
        return g.getGroupId();
    }


    private static String addPrivateGroup(PrivateGroup g){
        privateGroupList.addGroup(g, user);
        myRef.child("private_groups").child(g.getName()).setValue(g).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Success in adding group to datavase", Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failure in adding group to datavase", Toast.LENGTH_SHORT).show();
                    }
                });
        return g.getGroupId();
    }
    @Override
    public void onLocationChanged(Location location) {
        currentLoc = location;
        //Toast.makeText(context, location.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }

    @Override
    public void onBackPressed() {
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            MainActivity.super.onBackPressed();
                        }
                    }).create().show();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

}

