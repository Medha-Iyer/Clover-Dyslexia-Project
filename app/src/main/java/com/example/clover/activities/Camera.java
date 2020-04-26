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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clover.R;
import com.example.clover.pojo.LibraryCardItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.util.List;

public class Camera extends AppCompatActivity implements CameraNameDialog.ExampleDialogListener, View.OnClickListener {

    //layout things
    CardView convertTextBtn, saveLibraryBtn, takePhotoBtn, fromGalleryBtn;
    private ImageView imageView;
    private TextView tv;
    private Bitmap imageBitmap;

    //saving to library
    private String fileName, fileText;
    public static LibraryCardItem newCard;

    //constants for taking photos
    private static final int SELECT_PICTURE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();
    DocumentReference documentReference = fStore.collection("users").document(userId);
    private  final String TAG = "Spelling";
    private boolean darkmode;

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
                    darkmode = documentSnapshot.getBoolean("darkmode");
                    if(darkmode){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }else{
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                }
            }
        });

        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.DarkTheme1);
        }else{
            setTheme(R.style.AppTheme);
        }
        setContentView(R.layout.activity_camera);

        //reset card item value
        LibraryCardItem newCard = null;

        /*set up layout*/
        imageView = findViewById(R.id.image_view);

        //make text view scrollable
        tv = (TextView) findViewById(R.id.text_view);
        tv.setMovementMethod(new ScrollingMovementMethod());

        //take photo function
        takePhotoBtn = findViewById(R.id.take_photo);
        takePhotoBtn.setOnClickListener(this);

        //photo from gallery function
        fromGalleryBtn = findViewById(R.id.from_gallery);
        fromGalleryBtn.setOnClickListener(this);

        convertTextBtn = findViewById(R.id.convert_text);
        convertTextBtn.setOnClickListener(this);

        saveLibraryBtn = findViewById(R.id.save_card);
        saveLibraryBtn.setOnClickListener(this);

        //for bottom navigation bar
        BottomNavigationView navView = findViewById(R.id.nav_bar);
        navView.setSelectedItemId(R.id.camera); //set camera as selected
        //perform item selected listener
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
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
                    saveLibraryBtn.setVisibility(View.VISIBLE);
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
                ImageView imageView = findViewById(R.id.image_view);
                imageView.setImageBitmap(imageBitmap);
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
    private void detectTextFromImage() {
        final FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        firebaseVisionTextRecognizer.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayTextFromImage(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Camera.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void displayTextFromImage(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blockList = firebaseVisionText.getTextBlocks();
        if (blockList.size() == 0) {
            Toast.makeText(this, "No Text Found in image.", Toast.LENGTH_SHORT).show();
        } else {
            for (FirebaseVisionText.TextBlock block : blockList) {
                String text = block.getText();
                tv.setText(text);
                fileText = text;
            }
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
        newCard = new LibraryCardItem(fileName, fileText);

        Intent i = new Intent(Camera.this, Library.class);
        startActivity(i);
        overridePendingTransition(0, 0);
    }
}