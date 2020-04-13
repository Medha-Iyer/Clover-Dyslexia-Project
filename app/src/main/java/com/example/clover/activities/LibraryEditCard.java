package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.clover.R;

public class LibraryEditCard extends AppCompatActivity {

    public static final String EXTRA_TITLE =
            "com.example.clover.EXTRA_TITLE";
    public static final String EXTRA_TEXT =
            "com.example.clover.EXTRA_TEXT";
    public static final String EXTRA_ID =
            "com.example.clover.EXTRA_ID";

    private EditText editTitle;
    private EditText editText;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}
