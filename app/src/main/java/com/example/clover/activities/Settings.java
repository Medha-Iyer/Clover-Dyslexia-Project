package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.example.clover.R;
import com.example.clover.pojo.UserItem;
import com.example.clover.pojo.Utils;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Scanner;

import io.grpc.okhttp.internal.Util;

public class Settings extends AppCompatActivity implements View.OnClickListener {

    private CardView logout;
    private SeekBar mSeekBarPitch;
    private SeekBar mSeekBarSpeed;
    Switch switchMode;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();
    DocumentReference documentReference = fStore.collection("users").document(userId);
    private int selectedTheme;
    private static float pitchVal, speedVal = 1;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String PITCH = "pitch";
    public static final String SPEED = "speed";
    public static final String DARK_MODE = "dark mode";
    public static final String THEME = "theme";
    private final String TAG = "Settings data";

    private static int pitch;
    private static int speed;
    private static boolean darkmode;
    private static int theme;


    //allows access of variables outside of the snapshotlistener
    private interface FirebaseCallback{
        void onCallback(int pitch, int speed, boolean darkMode, int theme);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saveThemeData();
        //This has to be implemented in every screen to update mode and theme.
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(Settings.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                    return;
                }

                if (documentSnapshot.exists()) {
                    if (documentSnapshot.getBoolean("darkmode") != null){
                        darkmode = documentSnapshot.getBoolean("darkmode");
                    } else {
                        darkmode = false;
                    }
                    Utils.setTheme(Integer.parseInt(documentSnapshot.getString("theme")));
                    if(darkmode){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }else{
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                }
            }
        });
        Utils.onActivityCreateSetTheme(this);

        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            Log.d(TAG, "Switching to dark mode");
            Utils.changeToDark(this);
        }else{
            Log.d(TAG, "Switching to light mode");
            Utils.changeToLight(this);
        }
        setContentView(R.layout.activity_settings);

        //initialize and assign variable, do this for every
        BottomNavigationView navView = findViewById(R.id.nav_bar);
        mSeekBarPitch = findViewById(R.id.seek_bar_pitch);
        mSeekBarSpeed = findViewById(R.id.seek_bar_speed);
        logout = findViewById(R.id.logoutCard);
        logout.setOnClickListener(this);
        switchMode = (Switch) findViewById(R.id.switchmode);
        radioGroup = findViewById(R.id.radioGroup);
        Utils.checkRadio(radioGroup);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        documentReference = fStore.collection("users").document(userId);

        // Theme 1: 2131296529
        // Theme 2: 2131886088

        loadData();
        switchMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "SWITCHED to " + isChecked);
                if(darkmode == isChecked){
                    saveData();
                    Log.d(TAG, "This is darkmode switch" + darkmode);
                    return;
                }else {
                    darkmode = isChecked;
                    Log.d(TAG, "This is darkmode switch" + darkmode);
                }
                if(isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Log.d(TAG, "Dark mode is on");
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Log.d(TAG, "Dark mode is off");
                }
                saveData();
                restartApp();
            }
        });

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    UserItem u = snapshot.toObject(UserItem.class);
                    if(darkmode != u.getDarkmode()){
                        darkmode = u.getDarkmode();

                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        //set home as selected
        navView.setSelectedItemId(R.id.settings);

        //perform item selected listener
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.camera:
                        startActivity(new Intent(getApplicationContext(), Camera.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.library:
                        startActivity(new Intent(getApplicationContext(), Library.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(), Profile.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.settings:
                        return true;
                }
                return false;
            }

        });

    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.logoutCard:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), Login.class));
                break;
        }
    }

    public void radioClick(View v) {
        selectedTheme = radioGroup.getCheckedRadioButtonId();
        boolean checked = ((RadioButton) v).isChecked();
        switch(v.getId()) {
            case R.id.radio_one:
                if (checked) {
                    if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
                        theme = R.style.DarkTheme1;
                        Log.d(TAG, "Dark theme 1");
                        Utils.changeToTheme(this, Utils.DARK_THEME_DEFAULT);
                    }else{
                        theme = R.style.LightTheme1;
                        Log.d(TAG, "Light theme 1");
                        Utils.changeToTheme(this, Utils.THEME_DEFAULT);
                        break;
                    }
                }
                break;
            case R.id.radio_two:
                if(checked){
                    if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
                        theme = R.style.DarkTheme2;
                        Utils.changeToTheme(this, Utils.DARK_THEME_PINK);
                    }else{
                        theme = R.style.LightTheme2;
                        Log.d(TAG, "Light theme 2");
                        Utils.changeToTheme(this, Utils.THEME_PINK);
                    }
                }
                break;
            case R.id.radio_three:
                if(checked){
                    if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
                        theme = R.style.DarkTheme3;
                        Utils.changeToTheme(this, Utils.DARK_THEME_GREEN);
                    }else{
                        theme = R.style.LightTheme3;
                        Log.d(TAG, "Light theme 2");
                        Utils.changeToTheme(this, Utils.THEME_GREEN);
                    }
                }
                break;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    public void restartApp() {
        Intent i = new Intent(getApplicationContext(), Settings.class);
        startActivity(i);
        finish();
    }

    public static void speak(TextToSpeech mTTS, String word, int p, int s) {
        pitchVal = (float) p / 50;
        if (pitchVal < 0.1) pitchVal = 0.1f;
        speedVal = (float) s / 50;
        if (speedVal < 0.1) speedVal = 0.1f;
        mTTS.setPitch(pitchVal);
        mTTS.setSpeechRate(speedVal);
        mTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void readData(final Settings.FirebaseCallback f){
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(Settings.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                    return;
                }

                if (documentSnapshot.exists()) {
                    pitch = Integer.parseInt(documentSnapshot.getString("pitch"));
                    speed = Integer.parseInt(documentSnapshot.getString("speed"));
                    if (documentSnapshot.getBoolean("darkmode") != null){
                        darkmode = documentSnapshot.getBoolean("darkmode");
                    } else {
                        darkmode = false;
                    }
                    theme = Integer.parseInt(documentSnapshot.getString("theme"));
                    Log.d(TAG, "This is darkmode " + darkmode);
                    f.onCallback(pitch, speed, darkmode, theme);
                }
            }
        });
    }

    public void saveThemeData(){
        documentReference.update(
                 "theme", Integer.toString(Utils.getTheme()))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Data saved to Firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    public void saveData(){
//        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();

        documentReference.update(
                "pitch", Integer.toString(mSeekBarPitch.getProgress()),
                "speed", Integer.toString(mSeekBarSpeed.getProgress()),
                "darkmode", darkmode, "theme", Integer.toString(Utils.getTheme()))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Data saved to Firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });

//        editor.putInt(PITCH, pitch);
//        editor.putInt(SPEED, speed);
//        editor.putBoolean(DARK_MODE, switchMode.isChecked());
//        editor.apply();

    }

    private void loadData(){
//        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
//        pitch = sharedPreferences.getInt(PITCH, 50);
//        speed = sharedPreferences.getInt(SPEED, 50);
//        darkmode = sharedPreferences.getBoolean(DARK_MODE, false);

        //loads data from firebase
        readData(new Settings.FirebaseCallback() {
            @Override
            public void onCallback(int p, int s, boolean mode, int t) {
                Log.d(TAG, "This is the pitch from Firebase: " + p);
                Log.d(TAG, "This is the speed from Firebase: " + s);
                Log.d(TAG, "Dark mode is set to: " + mode);
                mSeekBarPitch.setProgress(p);
                mSeekBarSpeed.setProgress(s);
                switchMode.setChecked(mode);
                if(mode){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                if(t == 0 || t == 1){
                    ((RadioButton)radioGroup.getChildAt(0)).setChecked(true);
                }else if(t==2 || t==3){
                        ((RadioButton)radioGroup.getChildAt(1)).setChecked(true);
                }
            }
        });
    }

}
