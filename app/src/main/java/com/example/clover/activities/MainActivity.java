package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.example.clover.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // TODO add banner ads on screen somewhere
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This has to be implemented in every screen to update mode and theme.
        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.DarkTheme1);
        }else{
            setTheme(R.style.AppTheme);
        }
        setContentView(R.layout.activity_main);

        CardView mvoice = findViewById(R.id.voice_game_btn);
        mvoice.setOnClickListener(this);

        CardView mspelling = findViewById(R.id.spelling_game_btn);
        mspelling.setOnClickListener(this);

        CardView mlocked1 = findViewById(R.id.unlocked1);
        mlocked1.setOnClickListener(this);

        CardView mlocked2 = findViewById(R.id.unlocked2);
        mlocked2.setOnClickListener(this);

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
            case R.id.unlocked1:
                Toast.makeText(this, "Get Clover Pro to unlock this game.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.unlocked2:
                Toast.makeText(this, "Get Clover Pro to unlock this game.", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
