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

        bookList.add(0, new PersonalInfoItem("My Name is Brain Brian", "This \"outstanding\" book for children is the sensitive portrayal of a boy who struggles to hide his dyslexia from his friends. Based on the author's personal experience as a dyslexic, this novel is \"drawn from real insight\".", R.drawable.briancover));
        bookList.add(1, new PersonalInfoItem("Thank You, Mr. Falker", "Patricia Polacco is now one of America's most loved children's book creators, but once upon a time, she was a little girl named Trisha starting school. Trisha could paint and draw beautifully, but when she looked at words on a page, all she could see was jumble. It took a very special teacher to recognize little Trisha's dyslexia: Mr. Falker, who encouraged her to overcome her reading disability.", R.drawable.falkercover));
        bookList.add(2, new PersonalInfoItem("The Alphabet War: A Story about Dyslexia", "When Adam started kindergarten, the teacher wanted him to learn about letters. But -p- looked like -q, - and -b- looked like -d.- In first grade, he had to put the letters into words so he could read. That was the beginning of the Alphabet War.", R.drawable.alphabetcover));
        bookList.add(3, new PersonalInfoItem("Fish in a Tree", "Ally has been smart enough to fool a lot of smart people. Every time she lands in a new school, she is able to hide her inability to read by creating clever yet disruptive distractions. She is afraid to ask for help; after all, how can you cure dumb? However, her newest teacher Mr. Daniels sees the bright, creative kid underneath the trouble maker. With his help, Ally learns not to be so hard on herself and that dyslexia is nothing to be ashamed of. As her confidence grows, Ally feels free to be herself and the world starts opening up with possibilities. She discovers that there’s a lot more to her—and to everyone—than a label, and that great minds don’t always think alike.", R.drawable.fishcover));
        bookList.add(4, new PersonalInfoItem("Knees: The Mixed-Up World of a Boy with Dyslexia", "Knees shows the ups and downs of life with dyslexia. We have done this book in the style and size of a chapter book so that younger children and older children at low reading levels can read what seems to be an older child's book. We cover dyslexia’s symptoms and the reasons school can be hard for dyslexics. We talk about some famous people who had or have dyslexia.", R.drawable.kneescover));
        bookList.add(5, new PersonalInfoItem("Close to Famous", "When twelve-year-old Foster and her mother land in the tiny town of Culpepper, they don't know what to expect. But folks quickly warm to the woman with the great voice and the girl who can bake like nobody's business. Soon Foster - who dreams of having her own cooking show one day - lands herself a gig baking for the local coffee shop, and gets herself some much-needed help in overcoming her biggest challenge - learning to read . . . just as Foster and Mama start to feel at ease, their past catches up to them. Thanks to the folks in Culpepper, though Foster and her mama find the strength to put their troubles behind them for good.", R.drawable.cupcakecover));
        bookList.add(6, new PersonalInfoItem("Sam Is Stuck: Decodable Chapter Book", "For Tim and his sister, Kim, a relaxing boat ride with their parents turns into a rescue mission. Sam, a talking fish, is stuck between the rocks and his best pal cannot get him out… Can Tim and Kim help Sam? Will Sam the Cod Fish ever be free?Simple Words Books help children with dyslexia to become better readers, without tears. Our decodable chapter books support improving their reading fluency, comprehension and reading confidence.", R.drawable.samcover));
        bookList.add(7, new PersonalInfoItem("Tom's Special Talent", "Children with Dyslexia or a learning difficulty often find school a daunting and sometimes terrifying daily task. In an environment where certain skills, like writing and reading, are praised and highlighted more than others, it is important for kids to recognise that everyone has a ‘special talent’ of their own. It encourages other kids to be mindful of the differences that exist between their friends and classmates and to be aware that all children, regardless of their talents, learn differently.", R.drawable.tomcover));
        bookList.add(8, new PersonalInfoItem("Sixth Grade Can Really Kill You", "Helen fears that lack of improvement in her reading may leave her stuck in the sixth grade forever, until a good teacher recognizes her reading problem.", R.drawable.sixthcover));
        bookList.add(9, new PersonalInfoItem("Six Days at Camp with Jack and Max: Decodable Chapter Book", "Jack Mills and Max Finn are excited to spend six days at camp. Except, they cannot stand each other. And this could turn out to be a disaster. When things get out of control, Jack and Max are forced to work things out on their own. Can these boys be able to overcome the past and become friends? Simple Words Books help struggling readers to become better readers, without tears. Our decodable books support improving their reading fluency, comprehension and confidence.", R.drawable.camp));
        bookList.add(10, new PersonalInfoItem("Six Days at Camp with Lin and Jill: Decodable Chapter Book", "Lin Mills and Jill Finn are excited to spend six days at camp. Except, they cannot stand each other. And this could turn out to be a disaster. When things get out of control, Jill and Lin are forced to work things out on their own. Can these girls be able to overcome the past and become friends? Simple Words Books help struggling readers to become better readers, without tears. Our decodable books support improving their reading fluency, comprehension and confidence.", R.drawable.girlcamp));
        bookList.add(11, new PersonalInfoItem("The Higgledy-Piggledy Pigeon", "The Higgledy-Piggledy Pigeon is about a young homing pigeon named Hank who is an eager new student in flight school. He does an outstanding job in school until the day of his first practice delivery, when he unexpectedly discovers that he has no sense of direction and that he will get lost every time he is sent on a mission. He is devastated...is this the end of his dreams? Nobody else in class has this problem! It's so easy for them not to get lost. Maybe he should just quit. But a kind teacher shows him how he can compensate for his problem and still succeed. This story is about how everyone learns in different ways, and how anyone can succeed ­- even despite a learning problem - with the right kind of help and effort!", R.drawable.pigeon));
        bookList.add(12, new PersonalInfoItem("The Dog on a Log", "Jan and Tup hop to the dam, but Tup does not like to get wet. Finally, a delightful book series that helps kids learn phonics rules step by step. Fun books designed for anyone learning to read with phonics, especially learners with dyslexia. Start anywhere in the series, according to your child's reading level.", R.drawable.dog));
        bookList.add(13, new PersonalInfoItem("Five Chapter Books 2", "The DOG ON A LOG Book series helps kids, including kids with dyslexia, learn to read. They are sound out books that start with just a few phonics rules.  Each following Step of books adds a few more phonics rules and sight words. This gradual progression lets kids learn to read without feeling so overwhelmed.", R.drawable.five2));
        bookList.add(14, new PersonalInfoItem("Five Chapter Books 3", "The DOG ON A LOG Book series helps kids, including kids with dyslexia, learn to read. They are sound out books that start with just a few phonics rules.  Each following Step of books adds a few more phonics rules and sight words. This gradual progression lets kids learn to read without feeling so overwhelmed.", R.drawable.five3));
        bookList.add(15, new PersonalInfoItem("Five Chapter Books 4", "The DOG ON A LOG Book series helps kids, including kids with dyslexia, learn to read. They are sound out books that start with just a few phonics rules.  Each following Step of books adds a few more phonics rules and sight words. This gradual progression lets kids learn to read without feeling so overwhelmed.", R.drawable.five4));
        bookList.add(16, new PersonalInfoItem("Five Chapter Books 5", "The DOG ON A LOG Book series helps kids, including kids with dyslexia, learn to read. They are sound out books that start with just a few phonics rules.  Each following Step of books adds a few more phonics rules and sight words. This gradual progression lets kids learn to read without feeling so overwhelmed.", R.drawable.five5));
        bookList.add(17, new PersonalInfoItem("Five Chapter Books 6", "The DOG ON A LOG Book series helps kids, including kids with dyslexia, learn to read. They are sound out books that start with just a few phonics rules.  Each following Step of books adds a few more phonics rules and sight words. This gradual progression lets kids learn to read without feeling so overwhelmed.", R.drawable.five6));
        bookList.add(18, new PersonalInfoItem("Five Chapter Books 7", "The DOG ON A LOG Book series helps kids, including kids with dyslexia, learn to read. They are sound out books that start with just a few phonics rules.  Each following Step of books adds a few more phonics rules and sight words. This gradual progression lets kids learn to read without feeling so overwhelmed.", R.drawable.five7));
        bookList.add(19, new PersonalInfoItem("Five Chapter Books 8", "The DOG ON A LOG Book series helps kids, including kids with dyslexia, learn to read. They are sound out books that start with just a few phonics rules.  Each following Step of books adds a few more phonics rules and sight words. This gradual progression lets kids learn to read without feeling so overwhelmed.", R.drawable.five8));
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


