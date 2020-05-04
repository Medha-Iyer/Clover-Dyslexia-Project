package com.example.clover.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.clover.R;
import com.example.clover.pojo.GameItem;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.example.clover.pojo.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

public class Voice extends AppCompatActivity implements View.OnClickListener {

    public static final int GAME_KEY = 0;

    private FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();
    private DocumentReference documentReference = fStore.collection("users").document(userId);

    private int age, pitch, speed;
    private TextToSpeech mTTS;
    private Scanner scanner;
    private boolean darkMode;

    private TextView voiceResult, gameWord, correctView, nextWordText;
    private ImageView wordView, speakWord;
    private CardView nextWordBtn;
    private AdView mAdView;

    private String currentWord;
    private ArrayList<String> speakResult;
    private ArrayList<String> wordList = new ArrayList<String>();
    private ArrayList<GameItem> completedList = new ArrayList<GameItem>();
    private int fl = 0;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This has to be implemented in every screen to update mode and theme.
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(Voice.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d("Voice Activity", e.toString());
                    return;
                }
                if (documentSnapshot.exists()) {
                    if(documentSnapshot.getBoolean("darkmode") != null){
                        darkMode = documentSnapshot.getBoolean("darkmode");
                    } else {
                        darkMode = false;
                    }
                    if(documentSnapshot.getString("theme") != null){
                        Utils.setTheme(Integer.parseInt(documentSnapshot.getString("theme")));
                    } else {
                        Utils.setTheme(0);
                    }
                    if(darkMode){
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
        setContentView(R.layout.activity_voice);

        // declare if text to speech is being used
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        speakWord.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        //loads age from firebase, creates random word list
        readData(new Voice.FirebaseCallback() {
            @Override
            public void onCallback(int a, int p, int s) {
                if(age>=7){
                    scanner = new Scanner(getResources().openRawResource(R.raw.words2));
                }else{
                    scanner = new Scanner(getResources().openRawResource(R.raw.words1));
                }
                while (scanner.hasNextLine()) {
                    wordList.add(scanner.nextLine());
                }
                scanner.close();
                setUpWord();
            }
        });

        speakWord = findViewById(R.id.speakWord);
        speakWord.setOnClickListener(this);

        nextWordBtn = findViewById(R.id.next_word);
        nextWordBtn.setOnClickListener(this);

        wordView = findViewById(R.id.word_view);
        gameWord = findViewById(R.id.gameWord);
        correctView = findViewById(R.id.correct_text);
        voiceResult = findViewById(R.id.voiceResult);
        nextWordText = findViewById(R.id.next_word_text);

        reset();

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
        BottomNavigationView navView = findViewById(R.id.nav_bar);
        navView.setSelectedItemId(R.id.home);
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
        switch (v.getId()){
            case R.id.next_word:
                if(completedList.size()==10){
                    sendListToVoice();
                } else {
                    if(fl==1) {
                        reset();
                        setUpWord();
                    }else{
                        Toast.makeText(Voice.this, "Attempt to say the word", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.speakWord:
                getSpeechInput(v);
                break;
        }
    }

    //reset view before each word
    private void reset(){
        correctView.setVisibility(View.GONE);
        voiceResult.setVisibility(View.GONE);
        voiceResult.setText("");
        nextWordBtn.setVisibility(View.GONE);
        gameWord.setText("Say word...");
        wordView.setImageDrawable(getResources().getDrawable(R.drawable.rounded_nav));
    }

    //set up word, after showing word for 2 seconds
    private void setUpWord(){
        //show word for 5 seconds before disappearing
        gameWord.setText(randomLine(wordList));
        mHandler.postDelayed(mShowLoadingRunnable, 2000);
    }
    private Runnable mShowLoadingRunnable = new Runnable() {
        @Override
        public void run() {
            voiceResult.setVisibility(View.VISIBLE);
        }
    };

    //get speech from user
    public void getSpeechInput(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override //check if correct
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    speakResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if(speakResult.get(0).equals("")){
                        Toast.makeText(this, "Please type in the word...", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    voiceResult.setText("You said: " + speakResult.get(0));
                    correctView.setVisibility(View.VISIBLE);
                    fl=1;
                    if(speakResult.get(0).equalsIgnoreCase(currentWord)){
                        correctView.setText("Correct!");
                        correctView.setTextColor(getResources().getColor(R.color.darkGreen));
                        wordView.setImageDrawable(getResources().getDrawable(R.drawable.rounded_light_green));
                        completedList.get(0).setItemIcon(R.drawable.check);
                    }else{
                        correctView.setText("Incorrect!");
                        correctView.setTextColor(getResources().getColor(R.color.darkRed));
                        wordView.setImageDrawable(getResources().getDrawable(R.drawable.rounded_light_red));
                        completedList.get(0).setItemIcon(R.drawable.cross);
                    }

                    nextWordBtn.setVisibility(View.VISIBLE);

                    if(completedList.size()==10){
                        nextWordText.setText("FINISHED!");
                    }
                }
                break;
        }
    }

    //after 10 words, send to Results
    public void sendListToVoice(){
        Intent i = new Intent(Voice.this, Results.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("game list", completedList);
        bundle.putSerializable("game key", GAME_KEY);
        i.putExtras(bundle);
        startActivity(i);
        overridePendingTransition(0,0);
    }

    //get the right age, pitch, and speed depending on firebase
    private void readData(final FirebaseCallback f){
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(Voice.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d("Voice Activity", e.toString());
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

    //get random word from wordList
    public String randomLine(ArrayList<String> list) {
        currentWord = list.get(new Random().nextInt(list.size()));
        //add to beginning of list
        completedList.add(0, new GameItem(currentWord));
        wordList.remove(currentWord);
        return currentWord;
    }

    //allows access of variables outside of the snapshotListener
    private interface FirebaseCallback{
        void onCallback(int age, int pitch, int speed);
    }

    @Override //for speaker function
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }

        super.onDestroy();
    }
}
