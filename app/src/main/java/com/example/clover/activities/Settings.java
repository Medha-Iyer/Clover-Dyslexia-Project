package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.example.clover.R;
import com.firebase.client.Firebase;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Settings extends AppCompatActivity implements View.OnClickListener {

    private CardView logout;
    private SeekBar mSeekBarPitch;
    private SeekBar mSeekBarSpeed;
    private Switch switchMode;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    DocumentReference documentReference;
    Firebase fRootRef = new Firebase(""); //TODO Annie give me link
    Firebase fChildRef;
    private int selectedTheme;
    private String userID;
    public static float pitchVal = 1;
    public static float speedVal = 1;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String PITCH = "pitch";
    public static final String SPEED = "speed";
    public static final String DARK_MODE = "dark mode";
    public static final String THEME = "theme";

    private static int p;
    private static int s;
    private static boolean darkmode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.DarkTheme1); // TODO Change so that it takes into consideration what color preset has been selected
        }else{
            setTheme(R.style.AppTheme);
        }
        setContentView(R.layout.activity_settings);

        //initialize and assign variable, do this for every
        BottomNavigationView navView = findViewById(R.id.nav_bar);
        mSeekBarPitch = findViewById(R.id.seek_bar_pitch);
        mSeekBarSpeed = findViewById(R.id.seek_bar_speed);
        logout = findViewById(R.id.logoutButton);
        logout.setOnClickListener(this);
        switchMode = findViewById(R.id.switchmode);
        radioGroup = findViewById(R.id.radioGroup);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        documentReference = fStore.collection("users").document(userID);

        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES) {
            switchMode.setChecked(true);
        }

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
                        saveData();
                        return true;
                    case R.id.library:
                        startActivity(new Intent(getApplicationContext(), Library.class));
                        overridePendingTransition(0,0);
                        saveData();
                        return true;
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0,0);
                        saveData();
                        return true;
                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(), Profile.class));
                        overridePendingTransition(0,0);
                        saveData();
                        return true;
                    case R.id.settings:
                        return true;
                }
                return false;
            }

        });

        loadData();
        saveData();
        updateViews();

    }

    @Override
    public void onClick(View v){
        switch(selectedTheme){
            case R.id.radio_one:
                if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
                    setTheme(R.style.DarkTheme1);
                }else{
                    setTheme(R.style.AppTheme);
                    Toast.makeText(this, "Theme 1", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.radio_two:
                if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
                    setTheme(R.style.DarkTheme2);
                }else{
                    setTheme(R.style.LightTheme2);
                }
                break;
        }

        switch (v.getId()){
            case R.id.logoutButton:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), Login.class));
                break;
        }
    }

    public void checkButton(View v) {
        selectedTheme = radioGroup.getCheckedRadioButtonId();
    }

    @Override
    protected void onPause(){
        super.onPause();
        saveData();
        loadData();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        saveData();
        loadData();
    }

    public void restartApp() {
        Intent i = new Intent(getApplicationContext(), Settings.class);
        startActivity(i);
        finish();
    }

    public static void speak(TextToSpeech mTTS, String word) {
        pitchVal = (float) p / 50;
        if (pitchVal < 0.1) pitchVal = 0.1f;
        speedVal = (float) s / 50;
        if (speedVal < 0.1) speedVal = 0.1f;
        mTTS.setPitch(pitchVal);
        mTTS.setSpeechRate(speedVal);
        mTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(PITCH, mSeekBarPitch.getProgress());
        editor.putInt(SPEED, mSeekBarSpeed.getProgress());
        editor.putBoolean(DARK_MODE, switchMode.isChecked());
        editor.apply();

        fChildRef = fRootRef.child("pitch");
        fChildRef.setValue(mSeekBarPitch.getProgress());
        fChildRef = fRootRef.child("speed");
        fChildRef.setValue(mSeekBarSpeed.getProgress());

        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show();
    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        p = sharedPreferences.getInt(PITCH, 50);
        s = sharedPreferences.getInt(SPEED, 50);
        darkmode = sharedPreferences.getBoolean(DARK_MODE, false);
    }

    public void updateViews(){
        mSeekBarPitch.setProgress(p);
        mSeekBarSpeed.setProgress(s);
        switchMode.setChecked(darkmode);
        switchMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    restartApp();
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });
    }

}
