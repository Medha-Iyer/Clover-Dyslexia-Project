package com.example.clover;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class LibraryPopActivity extends AppCompatActivity { //change to Activity?

    private TextView mTitle;
    private TextView mText;
    private Button mCloseBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_library_pop);

        Intent intent = getIntent();
        String title = intent.getStringExtra(Intent.EXTRA_TITLE);
        String content = intent.getStringExtra(Intent.EXTRA_TEXT);

        mTitle = findViewById(R.id.titleView);
        mText = findViewById(R.id.textView);

        mTitle.setText(title);
        mText.setText(content);

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


    }
}
