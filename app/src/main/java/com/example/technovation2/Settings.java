package com.example.technovation2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class Settings extends AppCompatActivity {

    private SeekBar mSeekBarPitch;
    private SeekBar mSeekBarSpeed;
    private Button mButtonSpeak;
    public static float pitchVal = 1;
    public static float speedVal = 1;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String PITCH = "pitch";
    public static final String SPEED = "speed";

    private static int p;
    private static int s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //initialize and assign variable, do this for every
        BottomNavigationView navView = findViewById(R.id.nav_bar);
        mSeekBarPitch = findViewById(R.id.seek_bar_pitch);
        mSeekBarSpeed = findViewById(R.id.seek_bar_speed);


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
        updateViews(); //TODO make it so that you don't have to go to settings twice for it to update

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

        editor.apply();

        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show();
    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        p = sharedPreferences.getInt(PITCH, 50);
        s = sharedPreferences.getInt(SPEED, 50);
    }

    public void updateViews(){
        mSeekBarPitch.setProgress(p);
        mSeekBarSpeed.setProgress(s);
    }

}
