package com.example.clover.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.clover.R;
import com.example.clover.activities.ProfileProgress;

public class ProfileProgressCheck extends Fragment implements View.OnClickListener {
    View view;
    CardView spellingCard, voiceCard, unlockedCard1, unlockedCard2;

    public ProfileProgressCheck() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_progress_check, container, false);

        spellingCard = view.findViewById(R.id.spelling_card);
        spellingCard.setOnClickListener(this);

        voiceCard = view.findViewById(R.id.voice_card);
        voiceCard.setOnClickListener(this);

        unlockedCard1 = view.findViewById(R.id.unlocked1_card);
        unlockedCard1.setOnClickListener(this);

        unlockedCard2 = view.findViewById(R.id.unlocked2_card);
        unlockedCard2.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.spelling_card:
                i = new Intent(getContext(), ProfileProgress.class);
                i.putExtra(ProfileProgress.EXTRA_ID, 0);
                startActivity(i);
                break;
            case R.id.voice_card:
                i = new Intent(getContext(), ProfileProgress.class);
                i.putExtra(ProfileProgress.EXTRA_ID, 1);
                startActivity(i);
                break;
            case R.id.unlocked1_card:
                Toast.makeText(getContext(), "Get Clover Pro to unlock this game.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.unlocked2_card:
                Toast.makeText(getContext(), "Get Clover Pro to unlock this game.", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
