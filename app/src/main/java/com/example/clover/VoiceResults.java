package com.example.clover;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class VoiceResults extends AppCompatActivity implements View.OnClickListener {

    CardView playAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_results);
        playAgain = findViewById(R.id.playAgain);
        playAgain.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playAgain:
                startActivity(new Intent(getApplicationContext(), Voice.class));
        }
    }
}
