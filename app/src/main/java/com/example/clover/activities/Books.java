package com.example.clover.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clover.R;
import com.example.clover.adapters.BookAdapter;
import com.example.clover.fragments.SettingsPreferences;
import com.example.clover.pojo.PersonalInfoItem;
import com.example.clover.pojo.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.Locale;

public class Books extends AppCompatActivity implements View.OnClickListener, BookAdapter.OnItemClickListener {
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();
    DocumentReference documentReference = fStore.collection("users").document(userId);

    private RecyclerView mRecyclerView;
    private BookAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<PersonalInfoItem> bookList = new ArrayList<PersonalInfoItem>();
    private final String TAG = "Books";

    private TextToSpeech mTTS;
    private int pitch, speed;
    private boolean darkmode;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This has to be implemented in every screen to update mode and theme.
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(Books.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                    return;
                }

                if (documentSnapshot.exists()) {
                    if(documentSnapshot.getBoolean("darkmode") != null){
                        darkmode = documentSnapshot.getBoolean("darkmode");
                    } else {
                        darkmode = false;
                    }
                    if(documentSnapshot.getString("theme") != null){
                        Utils.setTheme(Integer.parseInt(documentSnapshot.getString("theme")));
                    } else {
                        Utils.setTheme(0);
                    }
                    if(darkmode){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }else{
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                }
            }
        });
        Utils.onActivityCreateSetTheme(this);

        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            Utils.changeToDark(this);
        }else{
            Utils.changeToLight(this);
        }
        setContentView(R.layout.activity_books);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setUpSearch();

        // declare if text to speech is being used
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
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

        readData();

        BottomNavigationView navView = findViewById(R.id.nav_bar);
        //set home as selected
        navView.setSelectedItemId(R.id.home);
        //perform item selected listener
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.camera:
                        startActivity(new Intent(getApplicationContext(), Camera.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.library:
                        startActivity(new Intent(getApplicationContext(), Library.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0,0);
                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(), Profile.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), Settings.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        bookList.add(0, new PersonalInfoItem("My Name is Brain Brian", "This is a book about a kids who is dyslexic", R.drawable.briancover));
        bookList.add(1, new PersonalInfoItem("The Alphabet War: A Story about Dyslexia", "When Adam started kindergarten, the teacher wanted him to learn about letters. But -p- looked like -q, - and -b- looked like -d.- In first grade, he had to put the letters into words so he could read. That was the beginning of the Alphabet War.", R.drawable.alphabetcover));
        buildRecyclerView(bookList);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.book_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        MaterialSearchView searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setMenuItem(searchItem);
        return true;
    }

    private void setUpSearch() {
        MaterialSearchView searchView = findViewById(R.id.search_view);
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
                buildRecyclerView(bookList);
            }
        });
    }
    private void processQuery(String query) {
        ArrayList<PersonalInfoItem> result = new ArrayList<>();

        for (PersonalInfoItem item : bookList) {
            if (item.getItemTitle().toLowerCase().contains(query.toLowerCase()) ||
                    item.getItemText().toLowerCase().contains(query.toLowerCase())) {
                result.add(item);
            }
        }
        mAdapter.setBookItems(result);
    }

    public void buildRecyclerView(ArrayList<PersonalInfoItem> books) {
        mRecyclerView = findViewById(R.id.bookRecycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new BookAdapter(books); //passes to adapter, then presents to viewholder

        mRecyclerView.setLayoutManager((mLayoutManager));
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(Books.this);
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
//            case R.id.brian:
//                i=new Intent(Intent.ACTION_VIEW, Uri.parse("https://openlibrary.org/works/OL3163138W/My_Name_Is_Brain_Brian"));
//                startActivity(i);
        }
    }

    private void readData(){
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(Books.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d("read data", e.toString());
                    return;
                }

                if (documentSnapshot.exists()) {
                    pitch = Integer.parseInt(documentSnapshot.getString("pitch"));
                    speed = Integer.parseInt(documentSnapshot.getString("speed"));
                    //f.onCallback(pitch, speed);
                }
            }
        });
    }

    @Override
    public void onSaveClick(PersonalInfoItem item, int position) {
        Log.d(TAG, "saved");
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

    @Override
    public void onUnsaveClick(PersonalInfoItem item, int position) {
        Log.d(TAG, "unsaved");
        String title = item.getItemTitle();
        documentReference = fStore.collection("users").document(userId)
                .collection("books").document(title);
        //TODO make it so that they can't name two things the same title
        documentReference.delete();
    }

    @Override
    public void onSpeakClick(PersonalInfoItem item, int position) {
        SettingsPreferences.speak(mTTS, item.getItemTitle() + item.getItemText(), pitch,speed);
    }

    //allows access of variables outside of the snapshotlistener
    private interface FirebaseCallback{
        void onCallback(int pitch, int speed);
    }

    //for the speaker function
    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }

        super.onDestroy();
    }

    //save completeList to firebase
    public void saveToFirebase(PersonalInfoItem item) {
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
}


