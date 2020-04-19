package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.clover.R;
import com.example.clover.adapters.GameAdapter;
import com.example.clover.adapters.LibraryAdapter;
import com.example.clover.pojo.GameItem;
import com.example.clover.pojo.LibraryCardItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class VoiceResults extends AppCompatActivity implements View.OnClickListener {

    private Button playAgain;

    private RecyclerView mRecyclerView;
    private GameAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<GameItem> voiceList = new ArrayList<GameItem>();

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userId;
    private DocumentReference progressRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_results);

        Bundle bundleObject = getIntent().getExtras();
        if (bundleObject != null) { //TODO find out why this always says null
            voiceList = (ArrayList<GameItem>) bundleObject.getSerializable("voice list");
        }
        buildRecyclerView(voiceList);

        playAgain = findViewById(R.id.play_again);
        playAgain.setOnClickListener(this);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        saveProgress();

        //navView.setSelectedItemId();
        BottomNavigationView navView = findViewById(R.id.nav_bar);
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
        switch (v.getId()) {
            case R.id.play_again:
                startActivity(new Intent(getApplicationContext(), Voice.class));
        }
    }

    public void buildRecyclerView(ArrayList<GameItem> voiceList) {
        mRecyclerView = findViewById(R.id.voiceRecycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new GameAdapter(voiceList);

        mRecyclerView.setLayoutManager((mLayoutManager));
        mRecyclerView.setAdapter(mAdapter);
    }

    public void saveProgress(){
        String word;
        for(int i=0; i< voiceList.size(); i++){
            word = voiceList.get(i).getItemWord();
            progressRef = fStore.collection("users")
                    .document(userId)
                    .collection("progress")
                    .document(word);
            progressRef.set(voiceList.get(i))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Progress", "Data saved to Firestore");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Progress", "Error updating document", e);
                        }
                    });

        }
    }
}
