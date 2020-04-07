package com.example.clover;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.SearchView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Library extends AppCompatActivity implements LibraryAdapter.OnItemClickListener {
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_TEXT = "content";

    //for recycler view format
    private RecyclerView mRecyclerView;
    private LibraryAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<LibraryCardItem> savedList = new ArrayList<LibraryCardItem>();

    private Button loadItemBtn;
    //constants to save UI states
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SAVED_LIST = "savedList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        loadData();
        saveData();
        Bundle bundleObject = getIntent().getExtras();
        if (bundleObject != null) {
            savedList = (ArrayList<LibraryCardItem>) bundleObject.getSerializable("library list");
        }

        buildRecyclerView();

        //perform item selected listener
        BottomNavigationView navView = findViewById(R.id.nav_bar); //initialize and assign variable, do this for every
        navView.setSelectedItemId(R.id.library); //set library as selected

        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.camera:
                        startActivity(new Intent(getApplicationContext(), Camera.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.library:
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

    public void buildRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true); //might need to change false
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new LibraryAdapter(savedList); //passes to adapter, then presents to viewholder

        mRecyclerView.setLayoutManager((mLayoutManager));
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(Library.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });


        return true;
    }

    @Override
    public void onItemClick(int position) {
        Intent detailIntent = new Intent(this, LibraryPopActivity.class);
        LibraryCardItem clickedItem = savedList.get(position);

        detailIntent.putExtra(Intent.EXTRA_TITLE, clickedItem.getItemTitle());
        detailIntent.putExtra(Intent.EXTRA_TEXT, clickedItem.getItemText());

        startActivity(detailIntent);
    }


    //to save UI states
    private void saveData(){
        //no other app can change our shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(savedList);
        editor.putString(SAVED_LIST, json);
        editor.apply();
    }

    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(SAVED_LIST, null);
        Type type = new TypeToken<ArrayList<LibraryCardItem>>() {}.getType();
        savedList = gson.fromJson(json, type);

        if (savedList == null){
            savedList = new ArrayList<LibraryCardItem>();
        }
    }
}
