package com.example.clover;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Camera extends AppCompatActivity implements CameraNameDialog.ExampleDialogListener{

    //take photo function
    private Button takePhotoBtn, fromGalleryBtn, convertTextBtn, saveLibraryBtn;
    private ImageView imageView;
    private TextView tv;
    private static Bitmap imageBitmap;

    //saving to library
    private String fileName, fileText, bitmapString;
    private ArrayList<LibraryCardItem> libraryList = new ArrayList<>();

    //constants to save UI states
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SAVED_LIST = "savedList";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        loadData();
        saveData();

        //make text view scrollable
        tv = (TextView) findViewById(R.id.text_view);
        tv.setMovementMethod(new ScrollingMovementMethod());

        imageView = findViewById(R.id.image_view);

        //take photo function
        takePhotoBtn = findViewById(R.id.take_photo);
        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
                tv.setText("Displaying text...");
            }
        });

        //photo from gallery function
        fromGalleryBtn = findViewById(R.id.from_gallery);
        fromGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchFromGalleryIntent();
                tv.setText("Displaying text...");
            }
        });

        convertTextBtn = findViewById(R.id.convert_text);
        convertTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageBitmap != null) {
                    detectTextFromImage();
                    saveLibraryBtn.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(getApplicationContext(), "Please upload photo.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        saveLibraryBtn = findViewById(R.id.save_to_library);
        saveLibraryBtn.setVisibility(View.GONE);
        saveLibraryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToLibrary();
                saveLibraryBtn.setVisibility(View.GONE);
                tv.setText("Displaying text...");
                imageView.setImageResource(R.drawable.ic_insertphoto);
                imageBitmap = null;
            }
        });

        //for bottom navigation bar
        BottomNavigationView navView = findViewById(R.id.nav_bar);
        navView.setSelectedItemId(R.id.camera); //set camera as selected
        //perform item selected listener
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.camera:
                        return true;
                    case R.id.library:
                        sendListToLibrary();
                        return true;
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
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
    }

    //from android studio website for taking photo function
    static final int SELECT_PICTURE = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchFromGalleryIntent(){
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent,
                "Select Picture"), SELECT_PICTURE);
    }

    //for saving to library!!!
    private void saveToLibrary(){
        //save file name pop-up
        CameraNameDialog exampleDialog = new CameraNameDialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }

    @Override //after pop-up, this method does something with the name
    public void applyTexts(String name) {
        fileName = name;
        libraryList.add(new LibraryCardItem(fileName, fileText, bitmapString));
        saveData();
        sendListToLibrary();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap); //displaying image
            bitmapString = convertBitmapToString(imageBitmap);
        } else if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                ImageView imageView = findViewById(R.id.image_view);
                bitmapString = convertBitmapToString(imageBitmap);
                imageView.setImageBitmap(imageBitmap);
                //imageView.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String convertBitmapToString(Bitmap imageBitmap){
        ByteArrayOutputStream baos = new  ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    private void detectTextFromImage(){
        final FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();
        firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayTextFromImage(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Camera.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayTextFromImage(FirebaseVisionText firebaseVisionText){
        List<FirebaseVisionText.Block> blockList = firebaseVisionText.getBlocks();
        if (blockList.size() == 0){
            Toast.makeText(this, "No Text Found in image.", Toast.LENGTH_SHORT).show();
        } else {
            for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()){
                String text = block.getText();
                tv.setText(text);
                fileText = text;
            }
        }
    }

    //to save UI states
    private void saveData(){
        //no other app can change our shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(libraryList);
        editor.putString(SAVED_LIST, json);
        editor.apply();
    }

    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(SAVED_LIST, null);
        Type type = new TypeToken<ArrayList<LibraryCardItem>>() {}.getType();
        libraryList = gson.fromJson(json, type);

        if (libraryList == null){
            libraryList = new ArrayList<LibraryCardItem>();
        }
    }

    public void sendListToLibrary(){
        loadData();
        saveData();
        Intent i = new Intent(Camera.this, Library.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("library list", libraryList);
        i.putExtras(bundle);
        startActivity(i);
        overridePendingTransition(0,0);
    }
}
