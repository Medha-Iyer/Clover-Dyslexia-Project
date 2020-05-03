package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.clover.R;
import com.example.clover.pojo.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class LibraryEditCard extends AppCompatActivity {

    public static final String EXTRA_TITLE =
            "com.example.clover.EXTRA_TITLE";
    public static final String EXTRA_TEXT =
            "com.example.clover.EXTRA_TEXT";
    public static final String EXTRA_ID =
            "com.example.clover.EXTRA_ID";
    private boolean darkmode;

    private FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();
    private DocumentReference documentReference = fStore.collection("users").document(userId);

    private EditText editTitle;
    private EditText editText;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(LibraryEditCard.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d("EditCard", e.toString());
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
        Utils.changeToPopup(this, getWindow());
        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            Utils.changeToDark(this);
        }else{
            Utils.changeToLight(this);
        }
        setContentView(R.layout.activity_library_edit_card);

        editTitle = findViewById(R.id.edit_title);
        editText = findViewById(R.id.edit_text);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)){
            setTitle("Edit Note");
            editTitle.setText(intent.getStringExtra(EXTRA_TITLE));
            editText.setText(intent.getStringExtra(EXTRA_TEXT));
        } else {
            setTitle("Add Note");
        }

        //set it as popup
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .8), (int) (height * .7));
        //overlap onto Library activity
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //set up menu
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.edit_card_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) { //if save button clicked
            case R.id.save_card:
                saveCard();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveCard() {
        String title = editTitle.getText().toString();
        String text = editText.getText().toString();

        //put warning if nothing is entered for either of columns
        if (title.trim().isEmpty() || text.trim().isEmpty()){
            Toast.makeText(this, "Please insert title and text.", Toast.LENGTH_SHORT).show();
            return;
        }

        //send data to Library activity
        Intent data = new Intent();
        data.putExtra(EXTRA_TITLE, title);
        data.putExtra(EXTRA_TEXT, text);

        int id = getIntent().getIntExtra(EXTRA_ID,-1);
        if (id != -1){
            data.putExtra(EXTRA_ID, id);
        }
        setResult(RESULT_OK, data);
        finish();
    }
}
