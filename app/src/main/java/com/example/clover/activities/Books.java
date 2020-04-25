package com.example.clover.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.example.clover.R;

public class Books extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This has to be implemented in every screen to update mode and theme.
        setContentView(R.layout.activity_books);

        CardView brian = findViewById(R.id.brian);
        brian.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.brian:
                i=new Intent(Intent.ACTION_VIEW, Uri.parse("https://openlibrary.org/works/OL3163138W/My_Name_Is_Brain_Brian"));
                startActivity(i);
        }
    }

}
