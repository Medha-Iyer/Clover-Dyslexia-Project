package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager.widget.ViewPager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import com.example.clover.R;
import com.example.clover.pojo.Utils;
import com.example.clover.adapters.FragmentAdapter;
import com.example.clover.fragments.LibraryBooks;
import com.example.clover.fragments.LibraryNotes;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.android.material.tabs.TabLayout;

public class Library extends AppCompatActivity {

    private final String TAG = "Library";

    private FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();
    private DocumentReference documentReference = fStore.collection("users").document(userId);

    private boolean darkmode;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This has to be implemented in every screen to update mode and theme.
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(Library.this, "Error while loading!", Toast.LENGTH_SHORT).show();
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
            Utils.changeToDark(this);
        }else{
            Utils.changeToLight(this);
        }
        setContentView(R.layout.activity_library);

        //setting up tabs for fragments
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        FragmentAdapter vpAdapter = new FragmentAdapter(getSupportFragmentManager());
        vpAdapter.AddFragment(new LibraryNotes(), "Notes");
        vpAdapter.AddFragment(new LibraryBooks(), "Books");
        //setting up adapter for fragments
        viewPager.setAdapter(vpAdapter);
        tabLayout.setupWithViewPager(viewPager);

        //set up nav bar
        BottomNavigationView navView = findViewById(R.id.nav_bar);
        navView.setSelectedItemId(R.id.library); //set library as selected
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.camera:
                        startActivity(new Intent(getApplicationContext(), Camera.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.library:
                        return true;
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(), Profile.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), Settings.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
    }
}