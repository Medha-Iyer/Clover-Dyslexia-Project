package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.SparseArray;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.clover.R;
import com.example.clover.fragments.SettingsPreferences;
import com.example.clover.pojo.LibraryCardItem;
import com.example.clover.popups.CameraNameDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.example.clover.pojo.Utils;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import java.io.IOException;
import java.util.Locale;

public class Camera extends AppCompatActivity implements CameraNameDialog.ExampleDialogListener, View.OnClickListener {

    private final String TAG = "Camera";
    private static final int SELECT_PICTURE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();
    private DocumentReference documentReference = fStore.collection("users").document(userId);

    private TextToSpeech mTTS;
    private int pitch, speed;
    private boolean darkmode;

    private CardView convertTextBtn, saveLibraryBtn, takePhotoBtn, fromGalleryBtn;
    private ImageView imageView, hearBtn;
    private TextView tv;
    private Bitmap imageBitmap;
    private AdView mAdView;

    public static LibraryCardItem newCard = null;
    private String fileName, fileTextComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This has to be implemented in every screen to update mode and theme.
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(Camera.this, "Error while loading!", Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_camera);

        // set up text to speech
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

        //read speed and pitch from firebase
        readData(new Camera.FirebaseCallback() {
            @Override
            public void onCallback(int p, int s) {
            }
        });

        /*set up layout*/
        imageView = findViewById(R.id.image_view);

        tv = (TextView) findViewById(R.id.text_view);
        tv.setMovementMethod(new ScrollingMovementMethod());

        takePhotoBtn = findViewById(R.id.take_photo);
        takePhotoBtn.setOnClickListener(this);

        fromGalleryBtn = findViewById(R.id.from_gallery);
        fromGalleryBtn.setOnClickListener(this);

        convertTextBtn = findViewById(R.id.convert_text);
        convertTextBtn.setOnClickListener(this);

        saveLibraryBtn = findViewById(R.id.save_card);
        saveLibraryBtn.setOnClickListener(this);

        hearBtn = (ImageView) findViewById(R.id.audio_icon);
        hearBtn.setVisibility(View.GONE);
        hearBtn.setOnClickListener(this);

        //load ad
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("9F59EB48A48DC1D3C05FCBCA3FBAC1F9").build();
        mAdView = findViewById(R.id.adView);
        mAdView.loadAd(adRequest);

        //set up bottom nav bar
        BottomNavigationView navView = findViewById(R.id.nav_bar);
        navView.setSelectedItemId(R.id.camera);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.camera:
                        return true;
                    case R.id.library:
                        startActivity(new Intent(getApplicationContext(), Library.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(), Profile.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), Settings.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.audio_icon:
                SettingsPreferences.speak(mTTS, fileTextComplete, pitch,speed);
                break;
            case R.id.take_photo:
                dispatchTakePictureIntent();
                tv.setText("Displaying text...");
                break;
            case R.id.from_gallery:
                dispatchFromGalleryIntent();
                tv.setText("Displaying text...");
                break;
            case R.id.convert_text:
                if (imageBitmap != null) {
                    detectTextFromImage();
                    hearBtn.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getApplicationContext(), "Please upload photo.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.save_card:
                if (imageBitmap != null && tv !=  null) {
                    saveToLibrary();
                    tv.setText("Displaying text...");
                    imageView.setImageResource(R.drawable.ic_insertphoto);
                    imageBitmap = null;
                    hearBtn.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getApplicationContext(), "Please upload photo.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap); //displaying image
        } else if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageView.setImageBitmap(imageBitmap);
                Log.d(TAG, "image set");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    private void dispatchFromGalleryIntent() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent,
                "Select Picture"), SELECT_PICTURE);
    }
    private void detectTextFromImage(){
            TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
            if(!textRecognizer.isOperational()){
                Toast.makeText(getApplicationContext(), "Could not get the text", Toast.LENGTH_SHORT).show();
            }else{
                Frame frame = new Frame.Builder().setBitmap(imageBitmap).build();
                SparseArray<TextBlock> items = textRecognizer.detect(frame);
                StringBuilder sb = new StringBuilder();
                for(int i=0; i<items.size(); i++){
                    TextBlock myItem = items.valueAt(i);
                    sb.append(myItem.getValue());
                    sb.append("\n");
                }
                displayTextFromImage(sb);
            }
        }
    private void displayTextFromImage(StringBuilder sb) {
        if (sb.toString().length() == 0) {
            Toast.makeText(this, "No Text Found in image.", Toast.LENGTH_SHORT).show();
        } else {
            tv.setText(sb.toString());
            fileTextComplete = tv.getText().toString();
        }
    }

    //name file popup
    private void saveToLibrary() {
        //save file name pop-up
        CameraNameDialog exampleDialog = new CameraNameDialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }

    @Override //after pop-up, creates card and goes to Library activity
    public void applyTexts(String name) {
        fileName = name;
        newCard = new LibraryCardItem(fileName, fileTextComplete);

        Intent i = new Intent(Camera.this, Library.class);
        startActivity(i);
        overridePendingTransition(0, 0);
    }

    //allows access of variable pitch and speed outside of the snapshot listener
    private interface FirebaseCallback{
        void onCallback(int pitch, int speed);
    }

    //get the right pitch and speed
    private void readData(final Camera.FirebaseCallback f){
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(Camera.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d("read data", e.toString());
                    return;
                }

                if (documentSnapshot.exists()) {
                    pitch = Integer.parseInt(documentSnapshot.getString("pitch"));
                    speed = Integer.parseInt(documentSnapshot.getString("speed"));
                    f.onCallback(pitch, speed);
                }
            }
        });
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
}