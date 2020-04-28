package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.example.clover.R;
import com.example.clover.pojo.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // TODO add banner ads on screen somewhere

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();
    DocumentReference documentReference = fStore.collection("users").document(userId);
    private final String TAG = "MainActivity";
    private boolean darkmode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This has to be implemented in every screen to update mode and theme.
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(MainActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
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
//                    darkmode = documentSnapshot.getBoolean("darkmode");
//                    Utils.setTheme(Integer.parseInt(documentSnapshot.getString("theme")));
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
            Utils.changeToDark(this);
        }else{
            Utils.changeToLight(this);
        }

        setContentView(R.layout.activity_home);

        CardView mvoice = findViewById(R.id.voice_game_btn);
        mvoice.setOnClickListener(this);

        CardView mspelling = findViewById(R.id.spelling_game_btn);
        mspelling.setOnClickListener(this);

        CardView mlocked1 = findViewById(R.id.unlocked1);
        mlocked1.setOnClickListener(this);

        CardView mlocked2 = findViewById(R.id.unlocked2);
        mlocked2.setOnClickListener(this);

        CardView mbooks = findViewById(R.id.book_btn);
        mbooks.setOnClickListener(this);

        //initialize and assign variable, do this for every button or other interactive feature
        BottomNavigationView navView = findViewById(R.id.nav_bar);
        //set home as selected
        navView.setSelectedItemId(R.id.home);
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
                        return true;
                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(), Profile.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), Settings.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }
    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()){
            case R.id.voice_game_btn:
                i=new Intent(MainActivity.this,Voice.class);
                startActivity(i);
                break;
            case R.id.spelling_game_btn:
                i=new Intent(MainActivity.this,Spelling.class);
                startActivity(i);
                break;
            case R.id.book_btn:
                i=new Intent(MainActivity.this,Books.class);
                startActivity(i);
                break;
            case R.id.unlocked1:
                Toast.makeText(this, "Get Clover Pro to unlock this game.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.unlocked2:
                Toast.makeText(this, "Get Clover Pro to unlock this game.", Toast.LENGTH_SHORT).show();
        }
    }
}
