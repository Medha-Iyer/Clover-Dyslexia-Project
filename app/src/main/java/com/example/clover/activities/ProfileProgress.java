package com.example.clover.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.clover.R;
import com.example.clover.adapters.FragmentAdapter;
import com.example.clover.fragments.ProfileCorrect;
import com.example.clover.fragments.ProfileIncorrect;
import com.google.android.material.tabs.TabLayout;

public class ProfileProgress extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TextView title;

    public static final String EXTRA_ID =
            "com.example.clover.EXTRA_ID";

    public static int CODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_progress);

        //setting up tabs for fragments
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        FragmentAdapter vpAdapter = new FragmentAdapter(getSupportFragmentManager());
        vpAdapter.AddFragment(new ProfileCorrect(), "Correct");
        vpAdapter.AddFragment(new ProfileIncorrect(), "Wrong");
        //setting up adapter for fragments
        viewPager.setAdapter(vpAdapter);
        tabLayout.setupWithViewPager(viewPager);

        Intent intent = getIntent();
        CODE = intent.getIntExtra(EXTRA_ID, -1);

        title = findViewById(R.id.game_title);
        if(CODE==0){
            title.setText("SPELLING");
        }else if(CODE==1){
            title.setText("VOICE");
        }

        //set it as popup
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*.9), (int)(height*.8));
        //overlap onto activity
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);
    }
}
