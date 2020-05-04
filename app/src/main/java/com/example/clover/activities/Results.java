package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.example.clover.R;
import com.example.clover.adapters.GameAdapter;
import com.example.clover.fragments.SettingsPreferences;
import com.example.clover.pojo.GameItem;
import com.example.clover.pojo.Utils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
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
import java.util.Locale;

public class Results extends AppCompatActivity implements View.OnClickListener, GameAdapter.OnItemClickListener {

    private final String TAG = "SpellingResults";

    private FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();
    private DocumentReference documentReference = fStore.collection("users").document(userId);

    private TextToSpeech mTTS;
    private int age, pitch, speed, gameKey;
    private boolean darkmode;

    private RecyclerView mRecyclerView;
    private GameAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private CardView playAgain, goHomeBtn;
    private AdView mAdView;

    private ArrayList<GameItem> list = new ArrayList<GameItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This has to be implemented in every screen to update mode and theme.
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(Results.this, "Error while loading!", Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_results);

        // set up speaking function
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        //read age, pitch, and speed from firebase
        readData(new Results.FirebaseCallback() {
            @Override
            public void onCallback(int a, int p, int s) {
                //get list from Spelling or Voice game
                Bundle bundleObject = getIntent().getExtras();
                if (bundleObject != null) {
                    list = (ArrayList<GameItem>) bundleObject.getSerializable("game list");
                    gameKey = (int) bundleObject.getSerializable("game key");
                }

                buildRecyclerView(list);

                //save progress to firebase depending on game, denoted by path and gameKey
                String path="";
                if(gameKey == 1){
                    path = "spellingprogress";
                } else {
                    path = "voiceprogress";
                }
                saveProgress(path);
            }
        });

        playAgain = findViewById(R.id.play_again);
        playAgain.setOnClickListener(this);

        goHomeBtn = findViewById(R.id.home_button);
        goHomeBtn.setOnClickListener(this);

        //set up ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("9F59EB48A48DC1D3C05FCBCA3FBAC1F9").build();
        mAdView.loadAd(adRequest);

        //set up bottom nav bar
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
                if(gameKey == 0){
                    startActivity(new Intent(getApplicationContext(), Voice.class));
                } else if (gameKey == 1){
                    startActivity(new Intent(getApplicationContext(), Spelling.class));
                }
                break;
            case R.id.home_button:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
        }
    }

    @Override //on click of speaker in card, say the word
    public void onItemClick(int position) {
        String currentWord = list.get(position).getItemWord();
        SettingsPreferences.speak(mTTS, currentWord, pitch, speed);
    }

    public void buildRecyclerView(ArrayList<GameItem> savedList) {
        mRecyclerView =  findViewById(R.id.spellingRecycler);
        mRecyclerView.setHasFixedSize(true); //might need to change false
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new GameAdapter(savedList); //passes to adapter, then presents to viewHolder
        mAdapter.setOnItemClickListener((GameAdapter.OnItemClickListener) Results.this);
        mRecyclerView.setLayoutManager((mLayoutManager));
        mRecyclerView.setAdapter(mAdapter);
    }

    //get the age, speed, and pitch from firebase
    private void readData(final Results.FirebaseCallback f){
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(Results.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d("read data", e.toString());
                    return;
                }

                if (documentSnapshot.exists()) {
                    age = Integer.parseInt(documentSnapshot.getString("age"));
                    pitch = Integer.parseInt(documentSnapshot.getString("pitch"));
                    speed = Integer.parseInt(documentSnapshot.getString("speed"));
                    f.onCallback(age, pitch, speed);
                }
            }
        });
    }

    //save user progress depending on game
    public void saveProgress(String path){
        String word;
        for(int i=0; i< list.size(); i++){
            list.get(i).fixIcons();
            word = list.get(i).getItemWord();
            documentReference = fStore.collection("users")
                    .document(userId)
                    .collection(path)
                    .document(word);
            documentReference.set(list.get(i))
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

    //allows access of variable age, pitch, and speed outside of the snapshotListener
    private interface FirebaseCallback{
        void onCallback(int age, int pitch, int speed);
    }

    //for the speaker function
    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }

        super.onDestroy();
    }
}
