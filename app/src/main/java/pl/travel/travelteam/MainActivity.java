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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

interface FirebaseCallback{
    void onCallback(Map<Integer, Set<PublicGroup>> list);
}

interface FirebaseCallbackPrivate{
    void onCallback(Map<Integer, Set<PrivateGroup>> list);
}

interface NamesHolder extends Callable {

}

final public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemClickListener, LocationListener {
    static TextView informacje;
    static TextView enterNick;
    static TextView groupType;
    static Button findTeamButton;
    static Button createTeamButton;
    static Button changeNickButton;
    static Button informationButton;
    static SeekBar rangeBar;
    static Context context;
    private static RecyclerView groupsView;
    private static RecyclerView privateGroupsView;
    private static BottomNavigationView navigation;
    private static RecyclerViewAdapter.ItemClickListener listenerContext;
    private static RecyclerViewAdapter adapter;
    private static RecyclerViewAdapter privateAdapter;
    static Map<Integer, Set<PublicGroup>> publicGroupList = new TreeMap<>();
    static Map<Integer, Set<PrivateGroup>> privateGroupList = new TreeMap<>();
    static String groupName;
    //private static String currentId;
    static boolean isPublic;
    private static boolean isInPublicSection = false;
    public static User user;
    static PublicGroup currentPublicGroup;
    static PrivateGroup currentPrivateGroup;
    static TextView userName;
    static String currentUserName = "User";
    static int distBuf = 0;

    static public FirebaseDatabase database;
    static public DatabaseReference myRef;
    private static DatabaseReference publicGroupsRef;
    private static DatabaseReference privateGroupsRef;

    private static LocationManager locationManager;

    private Location currentLoc;
    static Long range = 0L;
    private static ValueEventListener publicGroupsListener;
    private static ValueEventListener privateGroupsListener;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    public static ExecutorService executorService;
    public static Future<List<String>> futureNames;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        listenerContext = this;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


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
        publicGroupsRef.keepSynced(false);
        privateGroupsRef.keepSynced(false);
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
        TextView accuracy;
        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            userName = rootView.findViewById(R.id.inputNickTextId);
            userName.setText(user.getName());
            groupsView = rootView.findViewById(R.id.groupListId);
            groupsView.setLayoutManager(new LinearLayoutManager(context));
            adapter = new RecyclerViewAdapter(context, new ArrayList<String>());
            adapter.setClickListener(listenerContext);
            groupsView.setAdapter(adapter);
            try{
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(groupsView.getContext(),
                        getResources().getConfiguration().orientation);
                groupsView.addItemDecoration(dividerItemDecoration);
            }catch(IllegalArgumentException e){
                e.getMessage();
            }
            privateAdapter = new RecyclerViewAdapter(context, new ArrayList<String>());
            privateAdapter.setClickListener(listenerContext);

            navigation = rootView.findViewById(R.id.bottomNavigationId);
            navigation.setSelectedItemId(R.id.home);
            FunHolder.adjustGravity(navigation);
            navigation.setOnClickListener(null);

            for (int i = 0; i < navigation.getMenu().size(); i++) {
                navigation.getMenu().getItem(i).setEnabled(false);
            }

            privateGroupsView = rootView.findViewById(R.id.privateGroupListId);
            privateGroupsView.setLayoutManager(new LinearLayoutManager(context));
            privateGroupsView.setAdapter(privateAdapter);
            try{
                DividerItemDecoration privateDividerItemDecoration = new DividerItemDecoration(privateGroupsView.getContext(),
                        getResources().getConfiguration().orientation);
                privateGroupsView.addItemDecoration(privateDividerItemDecoration);
            }catch(IllegalArgumentException e){
                e.getMessage();
            }
            groupsView = rootView.findViewById(R.id.groupListId);
            groupType = (TextView) rootView.findViewById(R.id.groupTypeId);
            accuracy = (TextView) rootView.findViewById(R.id.accuracyId);
            enterNick = (TextView) rootView.findViewById(R.id.enterNickId);
            informacje = (TextView) rootView.findViewById(R.id.welcomeId);
            findTeamButton = rootView.findViewById(R.id.seekForTeamId);
            createTeamButton = rootView.findViewById(R.id.createTeamId);
            changeNickButton = rootView.findViewById(R.id.buttonChangeNameId);
            enterNick.setTextSize(32);
            groupType.setTextSize(32);
            userName.setTextSize(32);
            accuracy.setTextSize(18);
            informacje.setTextSize(16);
            accuracy.setText("No range limit");
            informationButton = rootView.findViewById(R.id.informationButtonId);




            rangeBar = rootView.findViewById(R.id.rangeBarId);
            rangeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    accuracy.setText("Range: " + seekBar.getProgress() + "km");
                    if(seekBar.getProgress() == 0){
                        accuracy.setText("No range limit");
                    }
                    range = Integer.toUnsignedLong(progress);
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                    accuracy.setText("Range: " + seekBar.getProgress() + "km");
                    if(seekBar.getProgress() == 0){
                        accuracy.setText("No range limit");
                    }
                    range = Integer.toUnsignedLong(seekBar.getProgress());
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                    accuracy.setText("Range: " + seekBar.getProgress() + "km");
                    if(seekBar.getProgress() == 0){
                        accuracy.setText("No range limit");
                    }
                    range = Integer.toUnsignedLong(seekBar.getProgress());
                }

            });
            createTeamButton.setOnClickListener(new View.OnClickListener() {
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
                                                userName = (TextView) rootView.findViewById(R.id.inputNickTextId);
                                                userName.setText(user.getName());
                                                userName.invalidate();
                                                user.setName(userName.getText().toString());
                                                user = new User(user.getName());
                                                groupName = name.getText().toString();
                                                currentPublicGroup = new PublicGroup(name.getText().toString());
                                                user.setRemoved(false);
                                                currentPublicGroup.addUser(user);
                                                currentPublicGroup.setRange(range);
                                                currentPublicGroup.setLat(user.getLat());
                                                currentPublicGroup.setLon(user.getLon());
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
                                                userName = (TextView) rootView.findViewById(R.id.inputNickTextId);
                                                userName.setText(user.getName());
                                                userName.invalidate();
                                                user.setName(userName.getText().toString());
                                                user = new User(user.getName());
                                                groupName = name.getText().toString();
                                                currentPrivateGroup = new PrivateGroup(name.getText().toString(), password.getText().toString());
                                                user.setRemoved(false);
                                                currentPrivateGroup.addUser(user);
                                                currentPrivateGroup.setRange(range);
                                                currentPrivateGroup.setLat(user.getLat());
                                                currentPrivateGroup.setLon(user.getLon());
                                                isPublic = false;
                                                changeActivity();
                                            }catch(SameGroupNameException | BlankPasswordException | BlankNameException e) {

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

            findTeamButton.setOnClickListener(new View.OnClickListener() {
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
                                    public void onCallback(Map<Integer, Set<PublicGroup>> list) {

                                    }
                                });
                                publicGroupsInit();
                                groupsView = rootView.findViewById(R.id.groupListId);
                                adapter = new RecyclerViewAdapter(context, FunHolder.getGroupNames());
                                System.out.println("MainActivity findTeamButton: " + FunHolder.getGroupNames());
                                adapter.setClickListener(listenerContext);
                                adapter.notifyDataSetChanged();
                                groupsView.setAdapter(adapter);
                                groupsView.invalidate();
                                break;
                            }
                            case 3:{
                                getPrivateGroups(new FirebaseCallbackPrivate() {
                                    @Override
                                    public void onCallback(Map<Integer, Set<PrivateGroup>> list) {

                                    }
                                });
                                privateGroupsInit();
                                privateGroupsView = rootView.findViewById(R.id.privateGroupListId);
                                adapter = new RecyclerViewAdapter(context, FunHolder.getGroupNames());
                                adapter.setClickListener(listenerContext);
                                adapter.notifyDataSetChanged();
                                groupsView.setAdapter(adapter);
                                groupsView.invalidate();
                                break;
                            }
                        }
                    }

                }

            });

            informationButton.setOnClickListener(new View.OnClickListener() {
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
                                    "In the group you can see other people on the map, check the distance between you and them and chat with them.\n" +
                                    "The application is safe. Messages and passwords are encrypted.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {
                                    //System.exit(0);
                                    //MainActivity.super.onBackPressed();
                                }
                            }).create().show();

                }

            });

            changeNickButton.setOnClickListener(new View.OnClickListener() {
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
                                                    userName = (TextView) rootView.findViewById(R.id.inputNickTextId);
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
                    builder.create();
                    builder.show();
                }

            });

            switch(getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:{
                    FunHolder.initInfo();
                    navigation.setSelectedItemId(R.id.home);
                    accuracy.setVisibility(View.INVISIBLE);
                    groupsView.setVisibility(View.INVISIBLE);
                    privateGroupsView.setVisibility(View.INVISIBLE);
                    break;
                }
                case 2:{
                    publicGroupsInit();
                    FunHolder.initGroups();
                    groupType.setText("Public groups");
                    navigation.setSelectedItemId(R.id.public_groups);
                    accuracy.setVisibility(View.VISIBLE);
                    groupsView.setVisibility(View.VISIBLE);
                    privateGroupsView.setVisibility(View.INVISIBLE);
                    groupsView = rootView.findViewById(R.id.groupListId);
                    break;
                }
                case 3:{
                    privateGroupsInit();
                    FunHolder.initGroups();
                    groupType.setText("Private groups");
                    navigation.setSelectedItemId(R.id.private_groups);
                    accuracy.setVisibility(View.VISIBLE);
                    groupsView.setVisibility(View.INVISIBLE);
                    privateGroupsView.setVisibility(View.VISIBLE);
                    privateGroupsView = rootView.findViewById(R.id.privateGroupListId);
                    break;
                }

            }
            return rootView;


        }



    }
    @Override
    public void onItemClick(View view, int position) {
        if(isInPublicSection){
            publicGroupsInit();
            for(Integer key : publicGroupList.keySet()){
                if(key < range || range == 0){
                    Iterator it = publicGroupList.entrySet().iterator();
                    while(it.hasNext()){
                        Map.Entry pairs = (Map.Entry) it.next();
                        Set<PublicGroup> publicGroupSet = new TreeSet<PublicGroup>();
                        for(Object g:(TreeSet)pairs.getValue()){
                            publicGroupSet.add((PublicGroup)g);
                        }
                        for(PublicGroup publicGroup : publicGroupSet){
                            try{
                                if(publicGroup != null && publicGroup.toStringRepresentation().equals(adapter.getItem(position))) {
                                    groupName = publicGroup.getName();
                                    user.setRemoved(false);
                                    FunHolder.setCurrentPublicGroup(publicGroup);
                                    if (publicGroup.addUser(user)) {
                                        isPublic = true;
                                        changeActivity();
                                    }else{
                                        FunHolder.setCurrentPublicGroup(null);
                                    }
                                }
                            }catch (IndexOutOfBoundsException e){
                                Toast.makeText(this, "Please refresh the group list.", Toast.LENGTH_SHORT).show();
                                System.out.println("ERROR = " + e.getMessage());
                            }catch (SameNameUserException e){
                                Toast.makeText(MainActivity.context, "User with same name is present in the group, please change your name", Toast.LENGTH_LONG).show();
                                e.getMessage();
                            }catch (Exception e){
                                Toast.makeText(this, "Please refresh the group list.", Toast.LENGTH_SHORT).show();
                                System.out.println("ERROR = " + e.getMessage());
                            }
                        }

                    }

                }else{
                    Toast.makeText(this, "The group is too far away!", Toast.LENGTH_SHORT).show();
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
                            for(Integer key : privateGroupList.keySet()){
                                if(key < range || range == 0){
                                    Iterator it = privateGroupList.entrySet().iterator();
                                    while(it.hasNext()){
                                        Map.Entry pairs = (Map.Entry) it.next();
                                        Set<PrivateGroup> privateGroupSet = new TreeSet<PrivateGroup>();
                                        System.out.println("entrySet class : " + pairs.getValue().getClass());
                                        for(Object g:(TreeSet)pairs.getValue()){
                                            privateGroupSet.add((PrivateGroup)g);
                                            System.out.println("G OBJECT GETCLASS = " + g.getClass());
                                            System.out.println("G OBJECT STRING VAL = " + g.toString());
                                        }
                                        for(PrivateGroup privateGroup : privateGroupSet){
                                            try {
                                                System.out.println("PG GROUP = " + privateGroup.toStringRepresentation());
                                                System.out.println("ADAPTER NAME = " + adapter.getItem(position));
                                                if (privateGroup != null && privateGroup.toStringRepresentation().equals(adapter.getItem(position))) {
                                                    groupName = privateGroup.getName();
                                                    System.out.println("USERLIST SIZE BEFORE ADD = " + privateGroup.getUserList().size());
                                                    user.setRemoved(false);
                                                    FunHolder.setCurrentPrivateGroup(privateGroup);
                                                    if (privateGroup.addUser(user, password.getText().toString())) {
                                                        isPublic = false;
                                                        changeActivity();
                                                    } else {
                                                        FunHolder.setCurrentPrivateGroup(null);
                                                    }
                                                }
                                            }catch(WrongPasswordException e){

                                            }catch (IndexOutOfBoundsException e){
                                                Toast.makeText(getApplicationContext(), "Please refresh the group list.", Toast.LENGTH_SHORT).show();
                                                System.out.println("ERROR = " + e.getMessage());
                                            }catch (SameNameUserException e){
                                                Toast.makeText(getApplicationContext(), "User with same name is present in the group, please change your name", Toast.LENGTH_LONG).show();
                                                e.getMessage();
                                            }catch (Exception e){
                                                Toast.makeText(getApplicationContext(), "Please refresh the group list.", Toast.LENGTH_SHORT).show();
                                                System.out.println("ERROR = " + e.getMessage());
                                            }
                                        }

                                    }

                                }else{
                                    Toast.makeText(getApplicationContext(), "The group is too far away!", Toast.LENGTH_SHORT).show();
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
        groupsView.setLayoutManager(new LinearLayoutManager(context));
    }

    private static void privateGroupsInit(){
        privateGroupsView.setLayoutManager(new LinearLayoutManager(context));
        privateAdapter = new RecyclerViewAdapter(context, FunHolder.getGroupNames());
        privateAdapter.setClickListener(listenerContext);
        privateAdapter.notifyDataSetChanged();
        privateGroupsView.setAdapter(privateAdapter);
        privateGroupsView.invalidate();
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

    private static void getPublicGroups(FirebaseCallback firebaseCallback){
        TreeSet<PublicGroup> setBuf = new TreeSet<>();
        Map<String, Integer> nameDistance = new HashMap<>();
        publicGroupsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Set<PublicGroup>> dataMap = (HashMap<String, Set<PublicGroup>>)dataSnapshot.getValue();
                publicGroupList.clear();
                setBuf.clear();
                Iterator<Set<PublicGroup>> iterator;
                if(dataMap!=null){
                    iterator = dataMap.values().iterator();
                }else{
                    iterator = new Iterator<Set<PublicGroup>>() {
                        @Override
                        public boolean hasNext() {
                            return false;
                        }

                        @Override
                        public Set<PublicGroup> next() {
                            return null;
                        }
                    };
                }

                Iterator it;
                if(dataMap!=null){
                    it = dataMap.entrySet().iterator();
                }else{
                    it = new Iterator() {
                        @Override
                        public boolean hasNext() {
                            return false;
                        }

                        @Override
                        public Object next() {
                            return null;
                        }
                    };
                }

                double latBuf = 0;
                double lonBuf = 0;
                while(it.hasNext()){
                    Map.Entry pairs = (Map.Entry) it.next();
                    for(Object g:((HashMap)pairs.getValue()).values()){
                            boolean switchLoc = false;
                            if(g instanceof Double){
                                if(!switchLoc){
                                    latBuf = (Double)g;
                                    switchLoc = true;
                                }else{
                                    lonBuf = (Double)g;
                                }
                            }
                    }
                    Queue<Object> groupParameters = new LinkedList<>();
                    for(Object g: ((HashMap)iterator.next()).values()){
                        groupParameters.add(g);
                    }
                        PublicGroup groupBuf = GroupFactory.getPublicGroup(groupParameters);
                        nameDistance.put(groupBuf.getName(), FunHolder.getDistance(user.getLatLng(), groupBuf.getLatLng())/1000);
                        if(!setBuf.contains(groupBuf))
                        setBuf.add(groupBuf);
                }
                publicGroupList.putIfAbsent(distBuf, setBuf);

                executorService = Executors.newSingleThreadExecutor();
                    futureNames = executorService.submit(new NamesHolder(){
                        @Override
                        public List<String> call(){
                            Set<PublicGroup> setBuf = new TreeSet<>();
                            Set<String> buf =  new TreeSet<>();
                            Iterator it = publicGroupList.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pairs = (Map.Entry) it.next();
                                if(!setBuf.containsAll((TreeSet)pairs.getValue()))
                                setBuf.addAll((TreeSet)pairs.getValue());
                            }
                            Long rangeGroup = 2L;
                            Long messagesCounter = 0L;
                            boolean gotMessages = false;
                            for(Object g:setBuf){
                                if(g instanceof java.lang.Long){
                                    if(!gotMessages){
                                        rangeGroup = (Long) g;
                                        gotMessages = true;
                                    }else{
                                        messagesCounter = (Long) g;
                                    }
                                    System.out.println("Long: " + (Long) g);
                                }
                                if(g instanceof String && !g.toString().startsWith("default")){
                                    buf.add(g.toString() + ", " + nameDistance.get(g.toString()) + " km away");
                                }
                                if(!g.toString().startsWith("default"))
                                buf.add(g.toString() + ", " + nameDistance.get(g.toString()) + " km away");
                            }
                            List<String> bufNames = new ArrayList<>();
                            buf.forEach(a->bufNames.add(a));
                            return bufNames;
                        }
                    });
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
        TreeSet<PrivateGroup> setBuf = new TreeSet<>();
        Map<String, Integer> nameDistance = new HashMap<>();
        privateGroupsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Set<PrivateGroup>> dataMap = (HashMap<String, Set<PrivateGroup>>)dataSnapshot.getValue();
                privateGroupList.clear();
                setBuf.clear();
                Iterator<Set<PrivateGroup>> iterator;
                if(dataMap!=null){
                    iterator = dataMap.values().iterator();
                }else{
                    iterator = new Iterator<Set<PrivateGroup>>() {
                        @Override
                        public boolean hasNext() {
                            return false;
                        }

                        @Override
                        public Set<PrivateGroup> next() {
                            return null;
                        }
                    };
                }

                Iterator it;
                if(dataMap!=null){
                    it = dataMap.entrySet().iterator();
                }else{
                    it = new Iterator() {
                        @Override
                        public boolean hasNext() {
                            return false;
                        }

                        @Override
                        public Object next() {
                            return null;
                        }
                    };
                }

                double latBuf = 0;
                double lonBuf = 0;
                while(it.hasNext()){
                    Map.Entry pairs = (Map.Entry) it.next();
                    for(Object g:((HashMap)pairs.getValue()).values()){
                        boolean switchLoc = false;
                        if(g instanceof Double){
                            if(!switchLoc){
                                latBuf = (Double)g;
                                switchLoc = true;
                            }else{
                                lonBuf = (Double)g;
                            }
                        }
                    }
                    Queue<Object> groupParameters = new LinkedList<>();
                    for(Object g: ((HashMap)iterator.next()).values()){
                        groupParameters.add(g);
                    }
                    PrivateGroup groupBuf = GroupFactory.getPrivateGroup(groupParameters);
                    nameDistance.put(groupBuf.getName(), FunHolder.getDistance(user.getLatLng(), groupBuf.getLatLng())/1000);
                    if(!setBuf.contains(groupBuf))
                        setBuf.add(groupBuf);
                }
                privateGroupList.putIfAbsent(distBuf, setBuf);

                executorService = Executors.newSingleThreadExecutor();
                futureNames = executorService.submit(new NamesHolder(){
                    @Override
                    public List<String> call(){
                        Set<PrivateGroup> setBuf = new TreeSet<>();
                        Set<String> buf =  new TreeSet<>();
                        Iterator it = privateGroupList.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pairs = (Map.Entry) it.next();
                            if(!setBuf.containsAll((TreeSet)pairs.getValue()))
                                setBuf.addAll((TreeSet)pairs.getValue());
                        }
                        Long rangeGroup = 2L;
                        Long messagesCounter = 0L;
                        boolean gotMessages = false;
                        for(Object g:setBuf){
                            if(g instanceof java.lang.Long){
                                if(!gotMessages){
                                    rangeGroup = (Long) g;
                                    gotMessages = true;
                                }else{
                                    messagesCounter = (Long) g;
                                }
                                System.out.println("Long: " + (Long) g);
                            }
                            if(g instanceof String && !g.toString().startsWith("default")){
                                buf.add(g.toString() + ", " + nameDistance.get(g.toString()) + " km away");
                            }
                            if(!g.toString().startsWith("default"))
                                buf.add(g.toString() + ", " + nameDistance.get(g.toString()) + " km away");
                        }
                        List<String> bufNames = new ArrayList<>();
                        buf.forEach(a->bufNames.add(a));
                        return bufNames;
                    }
                });
                firebaseCallback.onCallback(privateGroupList);
                privateGroupsInit();

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
        super.onResume();
        if(privateGroupsListener!=null && privateGroupsRef!=null)
            privateGroupsRef.addValueEventListener(privateGroupsListener);
        if(publicGroupsListener!=null && publicGroupsRef!=null)
            publicGroupsRef.addValueEventListener(publicGroupsListener);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}

