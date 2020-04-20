package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.clover.R;
import com.example.clover.adapters.GameAdapter;
import com.example.clover.adapters.ProfileViewPagerAdapter;
import com.example.clover.fragments.ProfilePersonalInfo;
import com.example.clover.fragments.ProfileProgressCheck;
import com.example.clover.pojo.GameItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Profile extends AppCompatActivity implements View.OnClickListener {
    TextView fullname, age;
    Button voiceProgress, spellingProgress;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    DocumentReference progressRef;

    String userId;
    ArrayList<GameItem> correctWords = new ArrayList<GameItem>();
    ArrayList<GameItem> incorrectWords = new ArrayList<GameItem>();

    private RecyclerView mRecyclerView;
    private GameAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //setting up tabs for fragments
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        ProfileViewPagerAdapter vpAdapter = new ProfileViewPagerAdapter(getSupportFragmentManager());
        vpAdapter.AddFragment(new ProfilePersonalInfo(), "Personal Info");
        vpAdapter.AddFragment(new ProfileProgressCheck(), "Progress Check");
        //setting up adapter for fragments
        viewPager.setAdapter(vpAdapter);
        tabLayout.setupWithViewPager(viewPager);

        //initialize and assign variable, do this for every
        voiceProgress = findViewById(R.id.voice_progress);
        voiceProgress.setOnClickListener(this);
        spellingProgress = findViewById(R.id.spelling_progress);
        spellingProgress.setOnClickListener(this);
        fullname = findViewById(R.id.prof_name);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(Profile.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                fullname.setText(documentSnapshot.getString("fname"));
            }
        });

        readVoiceProgress(new ProgressCallback() {
            @Override
            public void onCallback(ArrayList<GameItem> voiceList) {
                //Do what you need to do with your list

                buildRecyclerView(correctWords); //TODO make it so it builds when they click a button
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

//    public void onClick(View v) { //TODO what if they click before they play any games (dialog?)
//        switch (v.getId()) {
//            case R.id.voice_progress:
//                readVoiceProgress(new ProgressCallback() {
//                    @Override
//                    public void onCallback(ArrayList<GameItem> voiceList) { //switches to correct voice words
//                        buildRecyclerView(voiceList);
//                    }
//                });
//            case R.id.spelling_progress:
//                readSpellingProgress(new ProgressCallback() {
//                    @Override
//                    public void onCallback(ArrayList<GameItem> spellingList) { //switches to correct spelling words
//                        buildRecyclerView(spellingList);
//                    }
//                });
//        }
//    }

    public void buildRecyclerView(ArrayList<GameItem> progressView) {
        mRecyclerView = findViewById(R.id.progressRecycler);
        mRecyclerView.setVisibility(View.GONE);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new GameAdapter(progressView);

        mRecyclerView.setLayoutManager((mLayoutManager));
        mRecyclerView.setAdapter(mAdapter);
    }

    public void readVoiceProgress(final ProgressCallback vCallback){
        progressRef = fStore.collection("users")
                .document(userId);
        progressRef.collection("voiceprogress")
                .whereEqualTo("itemIcon", R.drawable.check)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("correct words", document.getId() + " => " + document.getData());
                                GameItem correct = document.toObject(GameItem.class);
                                correctWords.add(correct);
                            }
                            if(correctWords!=null) {
                                Log.d("Load correct words", "Success");
                                vCallback.onCallback(correctWords);
                            }
                        } else {
                            Log.d("Load correct words", "Error getting documents: ", task.getException());
                        }
                    }
                });

//        progressRef.collection("progress")
//                .whereEqualTo("item icon", R.drawable.x)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                GameItem incorrect = document.toObject(GameItem.class);
//                                incorrectWords.add(incorrect);
//                            }
//                            //vCallback.onCallback(incorrectWords);
//                            Log.d("Load incorrect words", "Success");
//                        } else {
//                            Log.d("Load incorrect words", "Error getting documents: ", task.getException());
//                        }
//                    }
//                });
    }

    public void readSpellingProgress(final ProgressCallback vCallback){
        progressRef = fStore.collection("users")
                .document(userId);
        progressRef.collection("spellingprogress")
                .whereEqualTo("itemIcon", R.drawable.check)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("correct words", document.getId() + " => " + document.getData());
                                GameItem correct = document.toObject(GameItem.class);
                                correctWords.add(correct);
                            }
                            if(correctWords!=null) {
                                Log.d("Load correct words", "Success");
                                vCallback.onCallback(correctWords);
                            }
                        } else {
                            Log.d("Load correct words", "Error getting documents: ", task.getException());
                        }
                    }
                });

//        progressRef.collection("spellingprogress")
//                .whereEqualTo("item icon", R.drawable.x)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                GameItem incorrect = document.toObject(GameItem.class);
//                                incorrectWords.add(incorrect);
//                            }
//                            //vCallback.onCallback(incorrectWords);
//                            Log.d("Load incorrect words", "Success");
//                        } else {
//                            Log.d("Load incorrect words", "Error getting documents: ", task.getException());
//                        }
//                    }
//                });
    }

    public interface ProgressCallback {
        void onCallback(ArrayList<GameItem> progressList);
    }
}
