package com.example.clover.fragments;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clover.R;
import com.example.clover.activities.Profile;
import com.example.clover.activities.ProfileProgress;
import com.example.clover.activities.Results;
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

public class ProfileCorrect extends Fragment implements GameAdapter.OnItemClickListener{
    View view;

    String userId;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    DocumentReference progressRef;
    int code;


    private TextToSpeech mTTS;
    private int age, pitch, speed;
    ArrayList<GameItem> correctWords = new ArrayList<GameItem>();

    private RecyclerView mRecyclerView;
    private GameAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    DocumentReference documentReference;

    public ProfileCorrect() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_correct, container, false);

        Log.d("correct","in the right activity");
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        documentReference = fStore.collection("users").document(userId);
        code = ProfileProgress.CODE;

        readProgress(new ProfileCorrect.ProgressCallback() {
            @Override
            public void onCallback(ArrayList<GameItem> spellingList) { //switches to correct spelling words
                buildRecyclerView(spellingList);
            }
        });

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
        return view;
    }

    public void buildRecyclerView(ArrayList<GameItem> savedList) {
        mRecyclerView = view.findViewById(R.id.spellingProgressRecycler);
        mRecyclerView.setHasFixedSize(true); //might need to change false
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new GameAdapter(savedList); //passes to adapter, then presents to viewholder
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager((mLayoutManager));
        mRecyclerView.setAdapter(mAdapter);
    }

    public void readProgress(final ProfileCorrect.ProgressCallback vCallback){
        progressRef = fStore.collection("users")
                .document(userId);

        int icon = R.drawable.check;
        String path = "";
        if(code==1){
            path = "spellingprogress";
        } else if (code == 0){
            path = "voiceprogress";
        }

        correctWords = new ArrayList<GameItem>();
        progressRef.collection(path)
                .whereEqualTo("itemIcon", icon)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("correct words", document.getId() + " => " + document.getData());
                                GameItem correct = document.toObject(GameItem.class);
                                correctWords.add(correct);
                            }
                            if(correctWords!=null) {
                                Log.d("Load correct words", "Success");
                                vCallback.onCallback(correctWords);
                            }
                        } else {
                            Log.d("Load correct words", "Error getting documents: ", task.getException());
                        }
                    }
                });
        if(correctWords.size() == 0) {
            Log.d("correct", "icon id: "+R.drawable.check);
        } else {
            Log.d("correct", "size: "+correctWords.size());
        }
    }

    @Override
    public void onItemClick(int position) {
        final String currentWord = correctWords.get(position).getItemWord();
        readData(new ProfileCorrect.FirebaseCallback() {
            @Override
            public void onCallback(int a, int p, int s) {
                SettingsPreferences.speak(mTTS, currentWord, pitch, speed);
            }
        });
    }

    //get the right list depending on age
    private void readData(final ProfileCorrect.FirebaseCallback f){
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

    //for the speaker function
    @Override
    public void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }

        super.onDestroy();
    }

    public interface ProgressCallback {
        void onCallback(ArrayList<GameItem> progressList);
    }

    //allows access of variable age outside of the snapshotlistener
    private interface FirebaseCallback{
        void onCallback(int age, int pitch, int speed);
    }
}
