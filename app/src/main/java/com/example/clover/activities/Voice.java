package com.example.clover.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clover.R;
import com.example.clover.pojo.GameItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;


public class Voice extends AppCompatActivity implements View.OnClickListener {

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String VOICE_LIST = "voiceList";
    private static final String TAG = "VoiceActivity";

    private ArrayList<String> wordList = new ArrayList<String>();
    private ArrayList<GameItem> voiceGame = new ArrayList<GameItem>();
    ArrayList<String> speakResult;

    private TextView voiceResult, gameWord, displayScore;
    private ImageView speakWord, bool;
    private Scanner sc;
    int age, pitch, speed, fl=0;
    int score = 0;
    String currentWord, userId;
    private TextToSpeech mTTS;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    DocumentReference documentReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        //Initialize all variables
        voiceResult = (TextView) findViewById(R.id.voiceResult);
        gameWord = (TextView) findViewById(R.id.gameWord);
        displayScore = findViewById(R.id.displayScore);
        displayScore.setText(String.valueOf(score));
        speakWord = findViewById(R.id.speakWord);
        speakWord.setOnClickListener(this);
        BottomNavigationView navView = findViewById(R.id.nav_bar);
        bool = findViewById(R.id.imageView);
        Button next = findViewById(R.id.nextWord);
        next.setOnClickListener(this);
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        documentReference = fStore.collection("users").document(userId);

        //loads age from firebase
        readData(new FirebaseCallback() {
            @Override
            public void onCallback(int a, int p, int s) {
                Log.d(TAG, "This is the age from Firebase: " + a);
                Log.d(TAG, "This is the pitch from Firebase: " + p);
                Log.d(TAG, "This is the speed from Firebase: " + s);
                //adds all the words from text file into an arraylist so they can be chosen randomly in the game.
                if(age>=7){
                    Log.d("age", "Valid list");
                    sc = new Scanner(getResources().openRawResource(R.raw.words2));
                }else{
                    sc = new Scanner(getResources().openRawResource(R.raw.words1));
                }
                while (sc.hasNextLine()) {
                    wordList.add(sc.nextLine());
                }
                sc.close();
                gameWord.setText(randomLine(wordList));
            }
        });

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

        //set home as selected
        navView.setSelectedItemId(R.id.home); //TODO set the navigation bar to nothing highlighted

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
                        startActivity(new Intent(getApplicationContext(), Settings.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    speakResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    voiceResult.setText("You said: " + speakResult.get(0));
                    speakWord.setVisibility(View.VISIBLE);
                    bool.setVisibility(View.VISIBLE);
                    fl=1;
                    if(speakResult.get(0).equalsIgnoreCase(currentWord)){
                        bool.setImageResource(R.drawable.check);
                        score++;
                        displayScore.setText(String.valueOf(score));
                        voiceGame.add(new GameItem(currentWord, R.drawable.check));
                    }else{
                        bool.setImageResource(R.drawable.cross);
                        voiceGame.add(new GameItem(currentWord, R.drawable.cross));
                    }
                    if(voiceGame.size()==2){ //TODO change to 10
                        sendListToVoice();
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.nextWord:
                if(fl==1) {
                    gameWord.setText(randomLine(wordList));
                    speakWord.setVisibility(View.INVISIBLE);
                    bool.setVisibility(View.INVISIBLE);
                    fl=0;
                }else{
                    Toast.makeText(Voice.this, "Attempt to say the word", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.speakWord:
                Settings.speak(mTTS, currentWord, pitch, speed);
                break;
        }
    }

    public String randomLine(ArrayList<String> list) {
        currentWord = list.get(new Random().nextInt(list.size()));
        Toast.makeText(Voice.this, "Current word: " + currentWord, Toast.LENGTH_SHORT).show();
        wordList.remove(currentWord);
        return currentWord;
    }

    public void sendListToVoice(){
        saveData();
        loadData();
        Intent i = new Intent(Voice.this, VoiceResults.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("voice list", voiceGame);
        i.putExtras(bundle);
        startActivity(i);
        overridePendingTransition(0,0);
    }

    private void readData(final FirebaseCallback f){
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(Voice.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
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

    //allows access of variables outside of the snapshotlistener
    private interface FirebaseCallback{
        void onCallback(int age, int pitch, int speed);
    }

    //to save UI states
    private void saveData(){
        //no other app can change our shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(voiceGame);
        editor.putString(VOICE_LIST, json);
        editor.apply();
    }

    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(VOICE_LIST, null);
        Type type = new TypeToken<ArrayList<GameItem>>() {}.getType();
        voiceGame = gson.fromJson(json, type);

        if (voiceGame == null){
            voiceGame = new ArrayList<GameItem>();
        }
    }

    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }

        super.onDestroy();
    }

}
