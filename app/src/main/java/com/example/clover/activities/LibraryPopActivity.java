package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clover.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Locale;

public class LibraryPopActivity extends AppCompatActivity implements View.OnClickListener { //change to Activity?

    private TextView mTitle;
    private TextView mText;
    private ImageView mHearIcon;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userId;
    private DocumentReference documentReference;

    private TextToSpeech mTTS;
    private int pitch, speed;

    private String title;
    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_pop);

        mHearIcon = findViewById(R.id.hear_text);
        mHearIcon.setOnClickListener(this);
        mTitle = findViewById(R.id.titleView);
        mText = findViewById(R.id.textView);
        mText.setMovementMethod(new ScrollingMovementMethod());

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        documentReference = fStore.collection("users").document(userId);

        // declare if text to speech is being used
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

        readData(new LibraryPopActivity.FirebaseCallback() {
            @Override
            public void onCallback(int p, int s) {
                Intent intent = getIntent();
                title = intent.getStringExtra(Intent.EXTRA_TITLE);
                content = intent.getStringExtra(Intent.EXTRA_TEXT);
                mTitle.setText(title);
                mText.setText(content);
            }
        });

        //set it as popup
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*.8), (int)(height*.7));
        //overlap onto Library activity
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.dimAmount = 0.75f;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setAttributes(layoutParams);
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

    //get the right list depending on age
    private void readData(final LibraryPopActivity.FirebaseCallback f){
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(LibraryPopActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d("read data", e.toString());
                    return;
                }

                if (documentSnapshot.exists()) {
                    pitch = Integer.parseInt(documentSnapshot.getString("pitch"));
                    speed = Integer.parseInt(documentSnapshot.getString("speed"));
                    f.onCallback(pitch, speed);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hear_text:
                Settings.speak(mTTS, title+content, pitch,speed);
                break;
        }
    }

    //allows access of variable age outside of the snapshotlistener
    private interface FirebaseCallback{
        void onCallback(int pitch, int speed);
    }
}
