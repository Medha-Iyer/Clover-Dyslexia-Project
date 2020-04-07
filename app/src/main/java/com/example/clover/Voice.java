package  com.example.clover;

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

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;


public class Voice extends AppCompatActivity implements View.OnClickListener {

    private TextView voiceResult;
    private TextView gameWord;
    private ImageView speakWord;
    private Scanner s;
    private ArrayList<String> words = new ArrayList<String>();
    public int age = 12;
    private String currentWord;
    private int wordCount=0;
    ImageView bool;
    private TextToSpeech mTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        //Initialize all variables
        voiceResult = (TextView) findViewById(R.id.voiceResult);
        gameWord = (TextView) findViewById(R.id.gameWord);
        speakWord = findViewById(R.id.speakWord);
        speakWord.setOnClickListener(this);
        BottomNavigationView navView = findViewById(R.id.nav_bar);
        bool = findViewById(R.id.imageView);
        Button next = findViewById(R.id.nextWord);
        next.setOnClickListener(this);

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
        //navView.setSelectedItemId(R.id.home); //TODO set the navigation bar to nothing highlighted

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

        // adds all the words from text file into an arraylist so they can be chosen randomly in the game.
        if(age<=7){
            s = new Scanner(getResources().openRawResource(R.raw.words1));
        }else{
            s = new Scanner(getResources().openRawResource(R.raw.words2));
        }
        try {
            while (s.hasNextLine()) {
                words.add(s.nextLine());
            }
        } finally {
            s.close();
        }

        gameWord.setText(randomLine(words));
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
                    wordCount++;
                    speakWord.setVisibility(View.VISIBLE);
                    if(result.get(0).equalsIgnoreCase(currentWord)){
                        bool.setImageResource(R.drawable.check);
                    }else{
                        bool.setImageResource(R.drawable.x);
                    }
                    if(wordCount==10){

                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.nextWord:
                gameWord.setText(randomLine(words));
                speakWord.setVisibility(View.INVISIBLE);
            case R.id.speakWord:
                Settings.speak(mTTS, currentWord);
        }
    }

    public String randomLine(ArrayList<String> list) {
        currentWord = list.get(new Random().nextInt(list.size()));
        words.remove(currentWord);
        return currentWord;
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
