package com.example.clover.fragments;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clover.R;
import com.example.clover.popups.LibraryPop;
import com.example.clover.adapters.GameAdapter;
import com.example.clover.pojo.GameItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Locale;

public class LibraryPopFull extends Fragment implements View.OnClickListener, GameAdapter.OnItemClickListener{

    private FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();
    private DocumentReference documentReference = fStore.collection("users").document(userId);

    private TextToSpeech mTTS;
    private int age, pitch, speed;

    private View view;
    private ImageView mHearIcon;
    private TextView fullText;

    private ArrayList<GameItem> correctWords = new ArrayList<GameItem>();
    private String text;

    public LibraryPopFull() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_pop_full, container, false);

        // declare if text to speech is being used
        mTTS = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
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

        readData(new LibraryPopFull.FirebaseCallback() {
            @Override
            public void onCallback(int a, int p, int s) {
                fullText = view.findViewById(R.id.full_text);
                text = LibraryPop.content;
                fullText.setText(text);
            }
        });

        mHearIcon = view.findViewById(R.id.hear_icon);
        mHearIcon.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.hear_icon:
                SettingsPreferences.speak(mTTS, text, pitch, speed);
        }
    }

    @Override
    public void onItemClick(int position) {
        final String currentWord = correctWords.get(position).getItemWord();
        readData(new LibraryPopFull.FirebaseCallback() {
            @Override
            public void onCallback(int a, int p, int s) {
                SettingsPreferences.speak(mTTS, currentWord, pitch, speed);
            }
        });
    }

    //get the right age, pitch, and speed from firebase
    private void readData(final LibraryPopFull.FirebaseCallback f){
        documentReference.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getContext(), "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d("read data", e.toString());
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

    //allows access of variable age outside of the snapshotlistener
    private interface FirebaseCallback{
        void onCallback(int age, int pitch, int speed);
    }

    @Override //for the speaker function
    public void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }

        super.onDestroy();
    }
}
