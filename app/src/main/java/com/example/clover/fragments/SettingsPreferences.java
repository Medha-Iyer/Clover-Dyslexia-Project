package com.example.clover.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.example.clover.R;
import com.example.clover.pojo.UserItem;
import com.example.clover.pojo.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.example.clover.activities.Settings;

public class SettingsPreferences extends Fragment {

    private View view;
    private final String TAG = "Settings data";

    private static float pitchVal, speedVal = 1;
    private static int pitch;
    private static int speed;
    private static boolean darkmode;
    private static int theme;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userId;
    private DocumentReference documentReference;

    private SeekBar mSeekBarPitch;
    private SeekBar mSeekBarSpeed;
    private Switch switchMode;

    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private int selectedTheme;

    public SettingsPreferences() {
    }

    //allows access of variables outside of the snapshotlistener
    private interface FirebaseCallback{
        void onCallback(int pitch, int speed, boolean darkMode, int theme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_preferences, container, false);

        mSeekBarPitch = view.findViewById(R.id.seek_bar_pitch);
        mSeekBarSpeed = view.findViewById(R.id.seek_bar_speed);

        radioGroup = view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                selectedTheme = radioGroup.getCheckedRadioButtonId();
                radioButton = view.findViewById(selectedTheme);
                switch(selectedTheme) {
                    case R.id.radio_one:
                        if (radioButton.isChecked()) {
                            if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
                                theme = R.style.DarkTheme1;
                                Log.d(TAG, "Dark theme 1");
                                Utils.changeToTheme(getActivity(), Utils.DARK_THEME_DEFAULT);
                            }else{
                                theme = R.style.LightTheme1;
                                Log.d(TAG, "Light theme 1");
                                Utils.changeToTheme(getActivity(), Utils.THEME_DEFAULT);
                                break;
                            }
                        }
                        break;
                    case R.id.radio_two:
                        if(radioButton.isChecked()){
                            if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
                                theme = R.style.DarkTheme2;
                                Utils.changeToTheme(getActivity(), Utils.DARK_THEME_PINK);
                            }else{
                                theme = R.style.LightTheme2;
                                Log.d(TAG, "Light theme 2");
                                Utils.changeToTheme(getActivity(), Utils.THEME_PINK);
                            }
                        }
                        break;
                    case R.id.radio_three:
                        if(radioButton.isChecked()){
                            if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
                                theme = R.style.DarkTheme3;
                                Utils.changeToTheme(getActivity(), Utils.DARK_THEME_GREEN);
                            }else{
                                theme = R.style.LightTheme3;
                                Log.d(TAG, "Light theme 2");
                                Utils.changeToTheme(getActivity(), Utils.THEME_GREEN);
                            }
                        }
                        break;
                }
            }
        });
        Utils.checkRadio(radioGroup);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        documentReference = fStore.collection("users").document(userId);

        loadData();
        switchMode = (Switch) view.findViewById(R.id.switchmode);
        switchMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "SWITCHED to " + isChecked);
                if(darkmode == isChecked){
                    saveData();
                    Log.d(TAG, "This is darkmode switch" + darkmode);
                    return;
                }else {
                    darkmode = isChecked;
                    Log.d(TAG, "This is darkmode switch" + darkmode);
                }
                if(isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Log.d(TAG, "Dark mode is on");
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Log.d(TAG, "Dark mode is off");
                }
                saveData();
                restartApp();
            }
        });

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    UserItem u = snapshot.toObject(UserItem.class);
                    if(darkmode != u.getDarkmode()){
                        darkmode = u.getDarkmode();

                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        return view;
    }

    @Override
    public void onPause(){
        super.onPause();
        saveData();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        saveData();
    }

    public void restartApp() {
        Intent i = new Intent(getContext(), Settings.class);
        startActivity(i);
        getActivity().finish();
    }

    //reads data from firebase
    private void readData(final SettingsPreferences.FirebaseCallback f){
        documentReference.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getContext(), "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                    return;
                }

                if (documentSnapshot.exists()) {
                    pitch = Integer.parseInt(documentSnapshot.getString("pitch"));
                    speed = Integer.parseInt(documentSnapshot.getString("speed"));
                    if (documentSnapshot.getBoolean("darkmode") != null){
                        darkmode = documentSnapshot.getBoolean("darkmode");
                    } else {
                        darkmode = false;
                    }
                    theme = Integer.parseInt(documentSnapshot.getString("theme"));
                    Log.d(TAG, "This is darkmode " + darkmode);
                    f.onCallback(pitch, speed, darkmode, theme);
                }
            }
        });
    }

    //updates data to firebase
    public void saveData(){
        documentReference.update(
                "pitch", Integer.toString(mSeekBarPitch.getProgress()),
                "speed", Integer.toString(mSeekBarSpeed.getProgress()),
                "darkmode", darkmode, "theme", Integer.toString(Utils.getTheme()))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Data saved to Firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    //loads data from firebase
    private void loadData(){
        readData(new FirebaseCallback() {
            @Override
            public void onCallback(int p, int s, boolean mode, int t) {
                Log.d(TAG, "This is the pitch from Firebase: " + p);
                Log.d(TAG, "This is the speed from Firebase: " + s);
                Log.d(TAG, "Dark mode is set to: " + mode);
                mSeekBarPitch.setProgress(p);
                mSeekBarSpeed.setProgress(s);
                switchMode.setChecked(mode);
                if(mode){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                if(t == 0 || t == 1){
                    ((RadioButton)radioGroup.getChildAt(0)).setChecked(true);
                }else if(t==2 || t==3){
                    ((RadioButton)radioGroup.getChildAt(1)).setChecked(true);
                }
            }
        });
    }

    //call every time we need text to be spoken
    public static void speak(TextToSpeech mTTS, String word, int p, int s) {
        pitchVal = (float) p / 50;
        if (pitchVal < 0.1) pitchVal = 0.1f;
        speedVal = (float) s / 50;
        if (speedVal < 0.1) speedVal = 0.1f;
        mTTS.setPitch(pitchVal);
        mTTS.setSpeechRate(speedVal);
        mTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);
    }

}
