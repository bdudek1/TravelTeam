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
import androidx.annotation.Nullable;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

interface FirebaseCallback{
    void onCallback(Map<Integer, Set<PublicGroup>> list);
}

interface FirebaseCallbackPrivate{
    void onCallback(Map<Integer, PrivateGroup> list);
}

interface NamesHolder extends Callable {

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
    static Map<Integer, Set<PublicGroup>> publicGroupList = new TreeMap<>();
    static Map<Integer, PrivateGroup> privateGroupList = new TreeMap<>();
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

    //private static List<String> groupsList = new ArrayList<>();

    private static LocationManager locationManager;


    private Location currentLoc;
    static Long range = 0L;
    private static ValueEventListener publicGroupsListener;
    private static ValueEventListener privateGroupsListener;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private static RewardedVideoAd mRewardedVideoAd;

    public static ExecutorService executorService;
    public static Future<List<String>> futureNames;



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
                    range = Integer.toUnsignedLong(progress);
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                    dokladnosc.setText("Range: " + seekBar.getProgress() + "km");
                    if(seekBar.getProgress() == 0){
                        dokladnosc.setText("No range limit");
                    }
                    range = Integer.toUnsignedLong(seekBar.getProgress());
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                    dokladnosc.setText("Range: " + seekBar.getProgress() + "km");
                    if(seekBar.getProgress() == 0){
                        dokladnosc.setText("No range limit");
                    }
                    range = Integer.toUnsignedLong(seekBar.getProgress());
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
                                                currentPublicGroup.addUser(new User("KUZYN"));
                                                currentPublicGroup.addUser(new User("KUZYN2"));
                                                currentPublicGroup.setRange(range);
                                                currentPublicGroup.setLat(user.getLat());
                                                currentPublicGroup.setLon(user.getLon());
                                                //currentId = addPublicGroup(currentPublicGroup);
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
                                                //currentPrivateGroup.addUser(user, currentPrivateGroup.getPassword());
                                                currentPrivateGroup.setRange(range);
                                                currentPrivateGroup.setLat(user.getLat());
                                                currentPrivateGroup.setLon(user.getLon());
                                                //currentId = addPrivateGroup(currentPrivateGroup);
                                                isPublic = false;
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
                                getPublicGroups(new FirebaseCallback() {
                                    @Override
                                    public void onCallback(Map<Integer, Set<PublicGroup>> list) {

                                    }
                                });
                                publicGroupsInit();
                                listaGrup = rootView.findViewById(R.id.groupList);
                                adapter = new RecyclerViewAdapter(context, FunHolder.getPublicGroupNames());
                                System.out.println("MainActivity szukajDruzyny: " + FunHolder.getPublicGroupNames());
                                adapter.setClickListener(listenerContext);
                                adapter.notifyDataSetChanged();
                                listaGrup.setAdapter(adapter);
                                listaGrup.invalidate();
                                break;
                            }
                            case 3:{
                                getPrivateGroups(new FirebaseCallbackPrivate() {
                                    @Override
                                    public void onCallback(Map<Integer, PrivateGroup> list) {

                                    }
                                });
                                privateGroupsInit();
                                listaGrupPrywatnych = rootView.findViewById(R.id.privateGroupList);
                                adapter = new RecyclerViewAdapter(context, FunHolder.getPrivateGroupNames());
                                adapter.setClickListener(listenerContext);
                                adapter.notifyDataSetChanged();
                                listaGrup.setAdapter(adapter);
                                listaGrup.invalidate();
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
                    publicGroupsInit();
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
            for(Integer key : publicGroupList.keySet()){
                if(key < range || range == 0){
                    Iterator it = publicGroupList.entrySet().iterator();
                    while(it.hasNext()){
                        Map.Entry pairs = (Map.Entry) it.next();
                        Set<PublicGroup> publicGroupSet = new TreeSet<PublicGroup>();
                        System.out.println("entrySet class : " + pairs.getValue().getClass());
                        for(Object g:(TreeSet)pairs.getValue()){
                            publicGroupSet.add((PublicGroup)g);
                            System.out.println("G OBJECT GETCLASS = " + g.getClass());
                            System.out.println("G OBJECT STRING VAL = " + g.toString());
                        }
                        for(PublicGroup publicGroup : publicGroupSet){
                            try{
                                System.out.println("PG GROUP = " + publicGroup.toStringRepresentation());
                                System.out.println("ADAPTER NAME = " + adapter.getItem(position));
                                if(publicGroup != null && publicGroup.toStringRepresentation().equals(adapter.getItem(position))) {
                                    groupName = publicGroup.getName();
                                    System.out.println("USERLIST SIZE BEFORE ADD = " + publicGroup.getUserList().size());
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
//                    for(PublicGroup g:publicGroupList.get(key)){
//                        try{
//                            if(g != null && g.toStringRepresentation().equals(adapter.getItem(position))) {
//                                    user.setName(userName.getText().toString());
//                                    groupName = g.getName();
//                                    //currentId = g.getGroupId();
//                                    if (g.addUser(user)) {
//                                        isPublic = true;
//                                        currentPublicGroup = g;
//                                        changeActivity();
//                                    }
//                            }
//                        }catch (IndexOutOfBoundsException e){
//                            Toast.makeText(this, "Please refresh the group list.", Toast.LENGTH_SHORT).show();
//                            e.getMessage();
//                        }catch (SameNameUserException e){
//                            Toast.makeText(MainActivity.context, "User with same name is present in the group, please change your name", Toast.LENGTH_LONG).show();
//                            e.getMessage();
//                        }catch (Exception e){
//                            Toast.makeText(this, "Please refresh the group list.", Toast.LENGTH_SHORT).show();
//                            e.getMessage();
//                        }
//                    }
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
                            for(PrivateGroup g : privateGroupList.values()){
                                    try{
                                        if(g != null && g.toStringRepresentation().equals(adapter.getItem(position))) {
                                            if(g.getRange() > FunHolder.getDistance(user.getLatLng(), new LatLng(g.getLat(), g.getLon())) || g.getRange() == 0){
                                                user.setName(userName.getText().toString());
                                                groupName = g.getName();
                                                //currentId = g.getGroupId();
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

                                    }catch (Exception e){
                                        Toast.makeText(getApplicationContext(), "Please refresh the group list.", Toast.LENGTH_SHORT).show();
                                        System.out.println("ERROR = " + e.getMessage());
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
        //publicGroupList.removeIf(x -> x.equals("null null"));
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

    private static void addPublicGroup(PublicGroup g){
        System.out.println("addPublicGroup gname: " + g.getName());
        publicGroupList.putIfAbsent(FunHolder.getDistance(MainActivity.user.getLatLng(), g.getLatLng()), new TreeSet<PublicGroup>());
        if(publicGroupList.get(FunHolder.getDistance(MainActivity.user.getLatLng(), g.getLatLng())).add(g)){
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
        }else{
            //GROUP EXISTS EXCEPTION
        }


    }


    private static void addPrivateGroup(PrivateGroup g){
        privateGroupList.put(FunHolder.getDistance(MainActivity.user.getLatLng(), g.getLatLng()), g);
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

    }

    private static void getPublicGroups(FirebaseCallback firebaseCallback){
        TreeSet<PublicGroup> setBuf = new TreeSet<>();
        Map<String, Integer> nameDistance = new HashMap<>();
        publicGroupsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Set<PublicGroup>> dataMap = (HashMap<String, Set<PublicGroup>>)dataSnapshot.getValue();
                publicGroupList.clear();
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
                        PublicGroup groupBuf = GroupFactory.getGroup(groupParameters);
                        //distBuf = FunHolder.getDistance(user.getLatLng(), groupBuf.getLatLng());
                        nameDistance.put(groupBuf.getName(), FunHolder.getDistance(user.getLatLng(), groupBuf.getLatLng())/1000);
                        setBuf.add(groupBuf);
                        System.out.println("/////////////BUG/////////////////");
                }
                publicGroupList.put(distBuf, setBuf);
                System.out.println("PUBLIC GROUP LIST = " + publicGroupList);

                executorService = Executors.newSingleThreadExecutor();
                    futureNames = executorService.submit(new NamesHolder(){
                        @Override
                        public List<String> call(){
                            Set<PublicGroup> setBuf = new TreeSet<>();
                            Set<String> buf =  new TreeSet<>();
                            Iterator it = publicGroupList.entrySet().iterator();
                            System.out.println("ITERATOR = " + it);
                            while (it.hasNext()) {
                                Map.Entry pairs = (Map.Entry) it.next();
                                //System.out.println("SETBUF = " + pairs.getValue());
                                setBuf.addAll((TreeSet)pairs.getValue());
                                System.out.println("SETBUF = " + setBuf);
                            }
                            Long rangeGroup = 2L;
                            Long messagesCounter = 0L;
                            boolean gotMessages = false;
                            for(Object g:setBuf){
                                System.out.println("Class: " + g.getClass());
                                if(g instanceof java.lang.Long){
                                    if(!gotMessages){
                                        rangeGroup = (Long) g;
                                        System.out.println("RANGE = " + rangeGroup);
                                        gotMessages = true;
                                    }else{
                                        messagesCounter = (Long) g;
                                    }
                                    System.out.println("Long: " + (Long) g);
                                }
                                if(g instanceof String && !g.toString().startsWith("default")){
                                    buf.add(g.toString() + ", " + nameDistance.get(g.toString()) + " km away");
                                    System.out.println("MAP = " + nameDistance);
                                    System.out.println("KEY = " + g.toString());
                                    System.out.println("VAL = " + nameDistance.get(g.toString()));
                                }
                                if(!g.toString().startsWith("default"))
                                buf.add(g.toString() + ", " + nameDistance.get(g.toString()) + " km away");
                                System.out.println("MAP = " + nameDistance);
                                System.out.println("KEY = " + g.toString());
                                System.out.println("VAL = " + nameDistance.get(g.toString()));
                            }
                            System.out.println("BUF = " + buf);
                            List<String> bufNames = new ArrayList<>();
                            buf.forEach(a->bufNames.add(a));
                            return bufNames;
                        }
                    });
                    System.out.println("publicGroupList : " + publicGroupList);
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
                privateGroupList.clear();
                for(DataSnapshot d:dataChildren){
                    try{
                        PrivateGroup g = d.getValue(PrivateGroup.class);
                        privateGroupList.put(FunHolder.getDistance(MainActivity.user.getLatLng(), g.getLatLng()), g);
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
        //currentId = addPrivateGroup(currentPrivateGroup);
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

