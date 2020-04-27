package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.clover.R;
import com.example.clover.adapters.GameAdapter;
import com.example.clover.adapters.LibraryAdapter;
import com.example.clover.pojo.GameItem;
import com.example.clover.pojo.LibraryCardItem;
import com.example.clover.pojo.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

public class SpellingResults extends AppCompatActivity implements View.OnClickListener {

    //for recycler view format
    private RecyclerView mRecyclerView;
    private GameAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<GameItem> spellingList = new ArrayList<GameItem>();

    private Button playAgain;

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();
    DocumentReference documentReference = fStore.collection("users").document(userId);
    private DocumentReference progressRef;

    private final String TAG = "SpellingResults";
    private boolean darkmode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This has to be implemented in every screen to update mode and theme.
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(SpellingResults.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                    return;
                }

                if (documentSnapshot.exists()) {
                    darkmode = documentSnapshot.getBoolean("darkmode");
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
            Utils.changeToDark(this);
        }else{
            Utils.changeToLight(this);
        }

        setContentView(R.layout.activity_spelling_results);

        Bundle bundleObject = getIntent().getExtras();
        if (bundleObject != null) {
            spellingList = (ArrayList<GameItem>) bundleObject.getSerializable("spelling list");
        }

        buildRecyclerView(spellingList);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        saveProgress();

        playAgain = findViewById(R.id.play_again);
        playAgain.setOnClickListener(this);

        //perform item selected listener
        BottomNavigationView navView = findViewById(R.id.nav_bar); //initialize and assign variable, do this for every
        navView.setSelectedItemId(R.id.home); //set home as selected
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.camera:
                        startActivity(new Intent(getApplicationContext(), Camera.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.library:
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
                startActivity(new Intent(getApplicationContext(), Spelling.class));
        }
    }

    public void buildRecyclerView(ArrayList<GameItem> savedList) {
        mRecyclerView =  findViewById(R.id.spellingRecycler);
        mRecyclerView.setHasFixedSize(true); //might need to change false
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new GameAdapter(savedList); //passes to adapter, then presents to viewholder

        mRecyclerView.setLayoutManager((mLayoutManager));
        mRecyclerView.setAdapter(mAdapter);
    }

    public void saveProgress(){
        String word;
        for(int i=0; i< spellingList.size(); i++){
            word = spellingList.get(i).getItemWord();
            progressRef = fStore.collection("users")
                    .document(userId)
                    .collection("spellingprogress")
                    .document(word);
            progressRef.set(spellingList.get(i))
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
