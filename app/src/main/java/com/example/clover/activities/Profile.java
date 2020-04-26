package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.clover.R;
import com.example.clover.adapters.FragmentAdapter;
import com.example.clover.fragments.ProfilePersonalInfo;
import com.example.clover.fragments.ProfileProgressCheck;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class Profile extends AppCompatActivity {
    TextView fullName;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //setting up tabs for fragments
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        FragmentAdapter vpAdapter = new FragmentAdapter(getSupportFragmentManager());
        vpAdapter.AddFragment(new ProfilePersonalInfo(), "Personal Info");
        vpAdapter.AddFragment(new ProfileProgressCheck(), "Progress Check");
        //setting up adapter for fragments
        viewPager.setAdapter(vpAdapter);
        tabLayout.setupWithViewPager(viewPager);

        fullName = findViewById(R.id.prof_name);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(Profile.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                fullName.setText(documentSnapshot.getString("name"));
            }
        });

        //set profile as selected
        BottomNavigationView navView = findViewById(R.id.nav_bar);
        //set home as selected
        navView.setSelectedItemId(R.id.profile);
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
}
