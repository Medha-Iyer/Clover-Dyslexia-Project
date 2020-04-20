package com.example.clover.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clover.R;
import com.example.clover.activities.Profile;
import com.example.clover.activities.ProfileProgress;
import com.example.clover.adapters.GameAdapter;
import com.example.clover.pojo.GameItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ProfileIncorrect extends Fragment {
    View view;

    String userId;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    DocumentReference progressRef;
    int code;

    ArrayList<GameItem> incorrectWords = new ArrayList<GameItem>();

    private RecyclerView mRecyclerView;
    private GameAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public ProfileIncorrect() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_incorrect, container, false);

        Log.d("incorrect","in the right activity");

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        code = ProfileProgress.CODE;

        readSpellingProgress(new ProfileIncorrect.ProgressCallback() {
            @Override
            public void onCallback(ArrayList<GameItem> spellingList) { //switches to correct spelling words
                buildRecyclerView(spellingList);
                if(spellingList.size()==1){
                    Log.d("oncallback","should be right");
                }
            }
        });
        return view;
    }

    public void buildRecyclerView(ArrayList<GameItem> savedList) {
        mRecyclerView = view.findViewById(R.id.incorrectRecycler);
        mRecyclerView.setHasFixedSize(true); //might need to change false
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new GameAdapter(savedList); //passes to adapter, then presents to viewholder

        mRecyclerView.setLayoutManager((mLayoutManager));
        mRecyclerView.setAdapter(mAdapter);
    }

    public void readSpellingProgress(final ProfileIncorrect.ProgressCallback vCallback){
        progressRef = fStore.collection("users")
                .document(userId);

        int icon = 0;
        if(code==0){
            icon = 2131165401;
        } else if (code == 1){
            icon = 2131165398;
        }

        progressRef.collection("spellingprogress")
                .whereEqualTo("itemIcon", icon)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                GameItem incorrect = document.toObject(GameItem.class);
                                incorrectWords.add(incorrect);
                            }
                            if(incorrectWords!=null) {
                                Log.d("Load incorrect words", "Success");
                                vCallback.onCallback(incorrectWords);
                            }
                        } else {
                            Log.d("Load incorrect words", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public interface ProgressCallback {
        void onCallback(ArrayList<GameItem> progressList);
    }
}
