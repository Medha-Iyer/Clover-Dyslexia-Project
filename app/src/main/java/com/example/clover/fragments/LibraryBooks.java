package com.example.clover.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clover.R;
import com.example.clover.popups.BookPop;
import com.example.clover.adapters.BookAdapter;
import com.example.clover.pojo.PersonalInfoItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import java.util.ArrayList;
import java.util.Locale;

public class LibraryBooks extends Fragment implements BookAdapter.OnItemClickListener{

    private FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();
    private DocumentReference documentReference = fStore.collection("users").document(userId);

    private TextToSpeech mTTS;
    private int age, pitch, speed;

    private RecyclerView mRecyclerView;
    private BookAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private View view;
    private Toolbar toolbar;

    private ArrayList<PersonalInfoItem> firebaseList = new ArrayList<PersonalInfoItem>();
    private ArrayList<PersonalInfoItem> savedList = new ArrayList<PersonalInfoItem>();

    public LibraryBooks() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_books, container, false);

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

        //gets list from firebase
        readLibraryData(new LibraryBooks.LibraryCallback() {
            @Override
            public void onCallback(ArrayList<PersonalInfoItem> firebaseList) {
                savedList = firebaseList;
                buildRecyclerView(savedList);
                setUpSearch();
            }
        });

        toolbar = view.findViewById(R.id.toolbarBooks);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        setUpSearch();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.example_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        MaterialSearchView searchView = (MaterialSearchView) view.findViewById(R.id.search_view);
        searchView.setMenuItem(searchItem);

        MenuItem saveItem = menu.findItem(R.id.show_archive);
        saveItem.setVisible(false);
    }

    @Override //to save card, item will be saved to firebase
    public void onSaveClick(PersonalInfoItem item, int position) {
        Log.d("LibraryBooks", "saved");
        String title = item.getItemTitle();
        documentReference = fStore.collection("users").document(userId)
                .collection("books").document(title);
        //TODO make it so that they can't name two things the same title
        documentReference.set(item).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Books", "Document saved to library collection");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Books", "onFailure: " + e.toString());
            }
        });
    }

    @Override //to unsave card, item will be deleted from firebase
    public void onUnsaveClick(PersonalInfoItem item, int position) {
        Log.d("unsave","in on item click");
        String title = item.getItemTitle();
        documentReference = fStore.collection("users").document(userId)
                .collection("books").document(title);
        //TODO make it so that they can't name two things the same title
        documentReference.delete();
        savedList.remove(item);
        buildRecyclerView(savedList);
    }

    @Override //if speaker on card clicked, item title will be spoken
    public void onSpeakClick(final PersonalInfoItem item, int position) {
        Log.d("speak","in on item click");
        readData(new LibraryBooks.FirebaseCallback() {
            @Override
            public void onCallback(int a, int p, int s) {
                String currentWord = item.getItemTitle();
                SettingsPreferences.speak(mTTS, currentWord, pitch, speed);
            }
        });
    }

    @Override //if card is clicked, pop-up will open
    public void onItemClick(PersonalInfoItem item) {
        Intent detailIntent = new Intent(getContext(), BookPop.class);

        detailIntent.putExtra(Intent.EXTRA_TITLE, item.getItemTitle());
        detailIntent.putExtra(Intent.EXTRA_TEXT, item.getItemText());

        startActivity(detailIntent);
    }

    public void buildRecyclerView(ArrayList<PersonalInfoItem> savedList) {
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new BookAdapter(savedList); //passes to adapter, then presents to viewholder
        mRecyclerView.setLayoutManager((mLayoutManager));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
    }

    private void setUpSearch() {
        MaterialSearchView searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                processQuery(newText);
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
            }

            @Override
            public void onSearchViewClosed() {
                buildRecyclerView(savedList);
            }
        });
    }
    private void processQuery(String query) {
        ArrayList<PersonalInfoItem> result = new ArrayList<>();

        for (PersonalInfoItem item : savedList) {
            if (item.getItemTitle().toLowerCase().contains(query.toLowerCase()) ||
                    item.getItemText().toLowerCase().contains(query.toLowerCase())) {
                result.add(item);
            }
        }
        if(savedList.size()!=0){
            mAdapter.setBookItems(result);
        }
    }

    //read data from firebase
    public void readLibraryData(final LibraryBooks.LibraryCallback lCallback) {
        firebaseList = new ArrayList<>();
        documentReference = fStore.collection("users").document(userId);
        documentReference.collection("books")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                PersonalInfoItem loadItem = document.toObject(PersonalInfoItem.class);
                                firebaseList.add(loadItem);
                            }
                            if (!firebaseList.isEmpty()) {
                                Log.d("Load library", "Success");
                                lCallback.onCallback(firebaseList);
                            }
                        } else {
                            Log.d("Load library", "Error getting documents: ", task.getException());
                            firebaseList = savedList; //If firebase library doesn't exist it is set to savedList
                        }
                    }
                });
    }

    //when calling firebase for data
    public interface LibraryCallback {
        void onCallback(ArrayList<PersonalInfoItem> progressList);
    }

    //get the age, pitch, and speed from data
    private void readData(final LibraryBooks.FirebaseCallback f){
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

    //allows access of variable age, pitch, and speed outside of the snapshotListener
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
