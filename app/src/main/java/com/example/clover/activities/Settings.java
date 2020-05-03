package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.clover.R;
import com.example.clover.adapters.FragmentAdapter;
import com.example.clover.fragments.SettingsPersonalInfo;
import com.example.clover.fragments.SettingsPreferences;
import com.example.clover.pojo.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class Settings extends AppCompatActivity {

    private final String TAG = "Settings data";

    private FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();
    private DocumentReference documentReference = fStore.collection("users").document(userId);

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private CardView logout;

    private static boolean darkmode;

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
                    if(documentSnapshot.getBoolean("darkmode") != null){
                        darkmode = documentSnapshot.getBoolean("darkmode");
                    } else {
                        darkmode = false;
                    }
                    if(documentSnapshot.getString("theme") != null){
                        Utils.setTheme(Integer.parseInt(documentSnapshot.getString("theme")));
                    } else {
                        Utils.setTheme(0);
                    }
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

        //setting up tabs for fragments
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        FragmentAdapter vpAdapter = new FragmentAdapter(getSupportFragmentManager());
        vpAdapter.AddFragment(new SettingsPersonalInfo(), "Personal Info");
        vpAdapter.AddFragment(new SettingsPreferences(), "Preferences");
        //setting up adapter for fragments
        viewPager.setAdapter(vpAdapter);
        tabLayout.setupWithViewPager(viewPager);

        logout = findViewById(R.id.logoutBtn);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });

        //set home as selected
        BottomNavigationView navView = findViewById(R.id.nav_bar);
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

<<<<<<< .merge_file_a06232
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
        saveData();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        saveData();
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

    public void saveThemeData(){ //TODO can I delete this now because UTILS?
=======
    public void saveThemeData(){
>>>>>>> .merge_file_a01328
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
}
