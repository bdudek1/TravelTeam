package pl.travel.travelteam;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;

import java.util.ArrayList;
import java.util.List;

interface FirebaseCallback{
    void onCallback(List<PublicGroup> list);
}

interface FirebaseCallbackPrivate{
    void onCallback(List<PrivateGroup> list);
}

final public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemClickListener, LocationListener, RewardedVideoAdListener {
    static TextView informacje;
    static TextView wprowadzNick;
    static TextView typGrupy;
    static Button szukajDruzyny;
    static Button stworzDruzyne;
    static Button zmienNick;
    static Button buttonInfo;
    static SeekBar zasiegBar;
    static Context context;
    private static RecyclerView listaGrup;
    private static RecyclerView listaGrupPrywatnych;
    private static BottomNavigationView navigation;
    private static RecyclerViewAdapter.ItemClickListener listenerContext;
    private static RecyclerViewAdapter adapter;
    private static RecyclerViewAdapter adapterPrywatnych;
    static List<PublicGroup> publicGroupList = new ArrayList<>();
    static List<PrivateGroup> privateGroupList = new ArrayList<>();
    static String groupName;
    private static String currentId;
    static boolean isPublic;
    private static boolean isInPublicSection = false;
    public static User user;
    static PublicGroup currentPublicGroup;
    static PrivateGroup currentPrivateGroup;
    static TextView userName;
    static String currentUserName = "User";

    static public FirebaseDatabase database;
    static public DatabaseReference myRef;
    private static DatabaseReference publicGroupsRef;
    private static DatabaseReference privateGroupsRef;

    private static List<String> groupsList = new ArrayList<>();

    private static LocationManager locationManager;


    private Location currentLoc;
    static int range;
    private static ValueEventListener publicGroupsListener;
    private static ValueEventListener privateGroupsListener;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private static RewardedVideoAd mRewardedVideoAd;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        listenerContext = this;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        MobileAds.initialize(this, "ca-app-pub-2337287186342241/8409945040");
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

        user = new User(currentUserName);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();


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
                        break;
                    }
                    case 2:{
                        isInPublicSection = false;
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
        publicGroupsRef = database.getReference("public_groups");
        privateGroupsRef = database.getReference("private_groups");
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
            try{
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(listaGrup.getContext(),
                        getResources().getConfiguration().orientation);
                listaGrup.addItemDecoration(dividerItemDecoration);
            }catch(IllegalArgumentException e){
                e.getMessage();
            }
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
            try{
                DividerItemDecoration privateDividerItemDecoration = new DividerItemDecoration(listaGrupPrywatnych.getContext(),
                        getResources().getConfiguration().orientation);
                listaGrupPrywatnych.addItemDecoration(privateDividerItemDecoration);
            }catch(IllegalArgumentException e){
                e.getMessage();
            }
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




            zasiegBar = rootView.findViewById(R.id.rangeBar);
            zasiegBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    dokladnosc.setText("Range: " + seekBar.getProgress() + "km");
                    if(seekBar.getProgress() == 0){
                        dokladnosc.setText("No range limit");
                    }
                    range = progress;
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                    dokladnosc.setText("Range: " + seekBar.getProgress() + "km");
                    if(seekBar.getProgress() == 0){
                        dokladnosc.setText("No range limit");
                    }
                    range = seekBar.getProgress();
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                    dokladnosc.setText("Range: " + seekBar.getProgress() + "km");
                    if(seekBar.getProgress() == 0){
                        dokladnosc.setText("No range limit");
                    }
                    range = seekBar.getProgress();
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
                                                user = new User(user.getName());
                                                //publicGroupList.addGroup(new PublicGroup(name.getText().toString()), user);
                                                groupName = name.getText().toString();
                                                currentPublicGroup = new PublicGroup(name.getText().toString());
                                                currentPublicGroup.addUser(user);
                                                currentPublicGroup.setRange(range);
                                                currentPublicGroup.setLat(user.getLat());
                                                currentPublicGroup.setLon(user.getLon());
                                                currentId = addPublicGroup(currentPublicGroup);
                                                isPublic = true;
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
                                                groupName = name.getText().toString();
                                                currentPrivateGroup = new PrivateGroup(name.getText().toString(), password.getText().toString());
                                                currentPrivateGroup.addUser(user, currentPrivateGroup.getPassword());
                                                currentPrivateGroup.setRange(range);
                                                currentPrivateGroup.setLat(user.getLat());
                                                currentPrivateGroup.setLon(user.getLon());
                                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setMessage("Watch a short ad to create private group")
                                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                if (mRewardedVideoAd.isLoaded()) {
                                                                    mRewardedVideoAd.show();
                                                                }

                                                            }
                                                        })
                                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {

                                                            }
                                                        });
                                                builder.create().show();
//                                                currentId = addPrivateGroup(currentPrivateGroup);
//                                                isPublic = false;
//                                                changeActivity();
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
                                getPublicGroups(new FirebaseCallback() {
                                    @Override
                                    public void onCallback(List<PublicGroup> list) {

                                    }
                                });
                                listaGrup = rootView.findViewById(R.id.groupList);

                                adapter = new RecyclerViewAdapter(context, FunHolder.getPublicGroupNames());
                                adapter.setClickListener(listenerContext);
                                adapter.notifyDataSetChanged();
                                listaGrup.setAdapter(adapter);
                                listaGrup.invalidate();
                                publicGroupsInit();
                                break;
                            }
                            case 3:{
                                getPrivateGroups(new FirebaseCallbackPrivate() {
                                    @Override
                                    public void onCallback(List<PrivateGroup> list) {

                                    }
                                });
                                listaGrupPrywatnych = rootView.findViewById(R.id.privateGroupList);
                                adapter = new RecyclerViewAdapter(context, FunHolder.getPrivateGroupNames());
                                adapter.setClickListener(listenerContext);
                                adapter.notifyDataSetChanged();
                                listaGrup.setAdapter(adapter);
                                listaGrup.invalidate();
                                privateGroupsInit();
                                break;
                            }
                        }
                    }

                }

            });

            buttonInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Information")
                            .setMessage("Swipe left and right to navigate through application.\n" +
                                    "You can set the range before seeking for group, then only groups in the range will be shown and groups without set range.\n" +
                                    "When creating a group you can set the range so only people in the range will be able to join the group.\n" +
                                    "If you will not set the range before creating a group people from the whole earth will be able to join the group.\n" +
                                    "Private groups require a password to join them.\n" +
                                    "In the group you can see other people on the map, check the distance between you and them and chat with them.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {
                                    //System.exit(0);
                                    //MainActivity.super.onBackPressed();
                                }
                            }).create().show();

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
                                                throw new BlankNameException("Please enter your nickname.");
                                            }else if(name.getText().toString().length()>11){
                                                throw new TooLongNameException("Name should have less than 12 characters.");
                                            }else{
                                                    userName = (TextView) rootView.findViewById(R.id.inputNickText);
                                                    userName.setText(name.getText().toString());
                                                    userName.invalidate();
                                                    user.setName(name.getText().toString());
                                            }

                                        }catch(BlankNameException e){
                                            e.getMessage();
                                        }catch(TooLongNameException e){
                                            e.getMessage();
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
            for(PublicGroup g : publicGroupList){
                try{
                    if(g != null && g.toStringRepresentation().equals(adapter.getItem(position))) {
                        if(g.getRange() > FunHolder.getDistance(user.getLatLng(), new LatLng(g.getLat(), g.getLon())) || g.getRange() == 0){
                            user.setName(userName.getText().toString());
                            groupName = g.getName();
                            currentId = g.getGroupId();
                            if (g.addUser(user)) {
                                isPublic = true;
                                currentPublicGroup = g;
                                changeActivity();
                            }
                        }else{
                            Toast.makeText(this, "The group is too far away!", Toast.LENGTH_SHORT).show();
                        }

                }

                    }catch (IndexOutOfBoundsException e){
                    Toast.makeText(this, "Please refresh the group list.", Toast.LENGTH_SHORT).show();
                        e.getMessage();
                    }catch (SameNameUserException e){
                    Toast.makeText(MainActivity.context, "User with same name is present in the group, please change your name", Toast.LENGTH_LONG).show();
                        e.getMessage();
                  }


            }
        }else{
            privateGroupsInit();
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Enter password").setView(R.layout.private_group_enter)
                    .setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Dialog dialogView = (Dialog) dialog;
                            EditText password=(EditText)dialogView.findViewById(R.id.passwordEntry);
                            for(PrivateGroup g : privateGroupList){
                                    try{
                                        if(g != null && g.toStringRepresentation().equals(adapter.getItem(position))) {
                                            if(g.getRange() > FunHolder.getDistance(user.getLatLng(), new LatLng(g.getLat(), g.getLon())) || g.getRange() == 0){
                                                user.setName(userName.getText().toString());
                                                groupName = g.getName();
                                                currentId = g.getGroupId();
                                                if (g.addUser(user, password.getText().toString())) {
                                                    isPublic = false;
                                                    currentPrivateGroup = g;
                                                    changeActivity();
                                                }
                                            }else{
                                                Toast.makeText(getApplicationContext(), "The group is too far away!", Toast.LENGTH_SHORT).show();
                                            }

                                        }

                                    }catch (SameNameUserException e){
                                        Toast.makeText(MainActivity.context, "User with same name is present in the group, please change your name", Toast.LENGTH_LONG).show();
                                    }catch (WrongPasswordException e){
                                        Toast.makeText(MainActivity.context, "Wrong password, please try again",
                                                Toast.LENGTH_LONG).show();

                                    }catch (IndexOutOfBoundsException e){
                                        Toast.makeText(getApplicationContext(), "Please refresh the group list.", Toast.LENGTH_SHORT).show();
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
        publicGroupList.removeIf(x -> x.equals("null null"));
    }

    private static void privateGroupsInit(){
        listaGrupPrywatnych.setLayoutManager(new LinearLayoutManager(context));
        adapterPrywatnych = new RecyclerViewAdapter(context, FunHolder.getPrivateGroupNames());
        adapterPrywatnych.setClickListener(listenerContext);
        adapterPrywatnych.notifyDataSetChanged();
        listaGrupPrywatnych.setAdapter(adapterPrywatnych);
        listaGrupPrywatnych.invalidate();
    }

    private static void changeActivity() {
        Intent myIntent = new Intent(context, GroupActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(privateGroupsListener!=null)
        privateGroupsRef.removeEventListener(privateGroupsListener);
        if(publicGroupsListener!=null)
        publicGroupsRef.removeEventListener(publicGroupsListener);
        context.startActivity(myIntent);
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
        publicGroupList.add(g);
        myRef.child("public_groups").child(g.getName()).setValue(g).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
        return g.getGroupId();
    }


    private static String addPrivateGroup(PrivateGroup g){
        privateGroupList.add(g);
        myRef.child("private_groups").child(g.getName()).setValue(g).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
        return g.getGroupId();
    }

    private static void getPublicGroups(FirebaseCallback firebaseCallback){
        publicGroupsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> dataChildren = dataSnapshot.getChildren();
                groupsList.clear();
                publicGroupList.clear();
                for(DataSnapshot d:dataChildren){
                    try{
                        PublicGroup g = d.getValue(PublicGroup.class);
                        publicGroupList.add(g);
                    }catch(DatabaseException e){
                        e.getMessage();
                    }

                }
                firebaseCallback.onCallback(publicGroupList);
                publicGroupsInit();

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        };
        publicGroupsRef.addValueEventListener(publicGroupsListener);

    }

    private static void getPrivateGroups(FirebaseCallbackPrivate firebaseCallback){
        privateGroupsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> dataChildren = dataSnapshot.getChildren();
                groupsList.clear();
                privateGroupList.clear();
                for(DataSnapshot d:dataChildren){
                    try{
                        PrivateGroup g = d.getValue(PrivateGroup.class);
                        privateGroupList.add(g);
                    }catch(DatabaseException e){
                        e.getMessage();
                    }

                }
                firebaseCallback.onCallback(privateGroupList);
                publicGroupsInit();

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        };
        privateGroupsRef.addValueEventListener(privateGroupsListener);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLoc = location;
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
                            finish();
                            System.exit(0);
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
        if(privateGroupsListener!=null && privateGroupsRef!=null)
            privateGroupsRef.removeEventListener(privateGroupsListener);
        if(publicGroupsListener!=null && publicGroupsRef!=null)
            publicGroupsRef.removeEventListener(publicGroupsListener);
    }

    @Override
    protected void onResume(){
        mRewardedVideoAd.resume(this);
        super.onResume();
        if(privateGroupsListener!=null && privateGroupsRef!=null)
            privateGroupsRef.addValueEventListener(privateGroupsListener);
        if(publicGroupsListener!=null && publicGroupsRef!=null)
            publicGroupsRef.addValueEventListener(publicGroupsListener);
    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-2337287186342241/8409945040",
                new AdRequest.Builder().build());
    }

    @Override
    public void onRewarded(RewardItem reward) {
        currentId = addPrivateGroup(currentPrivateGroup);
        isPublic = false;
        changeActivity();
        loadRewardedVideoAd();
        //Toast.makeText(this, "onRewarded! currency: " + reward.getType() + "  amount: " +
        //        reward.getAmount(), Toast.LENGTH_SHORT).show();
        // Reward the user.
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        //Toast.makeText(this, "onRewardedVideoAdLeftApplication",
        //        Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdClosed() {
        //Toast.makeText(this, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
       // Toast.makeText(this, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        //Toast.makeText(this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdOpened() {
        //Toast.makeText(this, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoStarted() {
        //Toast.makeText(this, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoCompleted() {
        //Toast.makeText(this, "onRewardedVideoCompleted", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(this);
        super.onDestroy();
    }


}

