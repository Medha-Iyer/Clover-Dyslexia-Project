package com.example.clover.activities;

import android.content.Intent;
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

    private static final String TAG = "VoiceActivity";
    private TextView voiceResult, gameWord, displayScore;
    private ImageView speakWord, bool;
    private Scanner s;
    private ArrayList<String> wordList = new ArrayList<String>();

    public static ArrayList<String> completedWords = new ArrayList<String>();
    public static ArrayList<Integer> voiceIcons = new ArrayList<Integer>();

    int age;
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
            public void onCallback(int a) {
                Log.d(TAG, "This is the age from Firebase: " + a);
                //adds all the words from text file into an arraylist so they can be chosen randomly in the game.
                if(age>=7){
                    Log.d("age", "Valid list");
                    s = new Scanner(getResources().openRawResource(R.raw.words2));
                }else{
                    s = new Scanner(getResources().openRawResource(R.raw.words1));
                }
                while (s.hasNextLine()) {
                    wordList.add(s.nextLine());
                }
                s.close();
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
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    voiceResult.setText("You said: " + result.get(0));
                    speakWord.setVisibility(View.VISIBLE);
                    if(result.get(0).equalsIgnoreCase(currentWord)){
                        bool.setImageResource(R.drawable.check);
                        voiceIcons.add(R.drawable.check);
                        score++;
                        displayScore.setText(String.valueOf(score));
                    }else{
                        bool.setImageResource(R.drawable.x);
                        voiceIcons.add(R.drawable.x);
                    }
                    if(completedWords.size()==10){
                        startActivity(new Intent(getApplicationContext(), VoiceResults.class));
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.nextWord:
                gameWord.setText(randomLine(wordList));
                speakWord.setVisibility(View.INVISIBLE);
                bool.setVisibility(View.INVISIBLE);
                break;
            case R.id.speakWord:
                Settings.speak(mTTS, currentWord);
                break;
        }
    }

    public String randomLine(ArrayList<String> list) {
        //if(!list.isEmpty()) {
            currentWord = list.get(new Random().nextInt(list.size()));
        //}
        completedWords.add(currentWord);
        wordList.remove(currentWord);
        return currentWord;
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
                    f.onCallback(age);
                }
            }
        });
    }

    //allows access of variable age outside of the snapshotlistener
    private interface FirebaseCallback{
        void onCallback(int age);
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
