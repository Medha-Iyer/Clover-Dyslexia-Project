package com.example.clover;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class Library extends AppCompatActivity implements LibraryAdapter.OnItemClickListener {
    //for recycler view format
    private RecyclerView mRecyclerView;
    private LibraryAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<LibraryCardItem> savedList = new ArrayList<LibraryCardItem>();

    private FloatingActionButton addNoteBtn;
    private MenuItem showArchive;
    private Toolbar toolbar;

    //constants to save UI states
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SAVED_LIST = "savedList";

    //for editing or adding note
    public static final int ADD_NOTE_REQUEST = 1;
    public static final int EDIT_NOTE_REQUEST = 2;
    private int currentPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        addNoteBtn = findViewById(R.id.add_button);
        addNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Library.this, LibraryEditCard.class);
                startActivityForResult(intent, ADD_NOTE_REQUEST);
            }
        });

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadData();
        saveData();
        Bundle bundleObject = getIntent().getExtras();
        if (bundleObject != null) {
            savedList = (ArrayList<LibraryCardItem>) bundleObject.getSerializable("library list");
        }

        buildRecyclerView(savedList);
        setUpSearch();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

////        mAdapter.setOnItemClickListener(new LibraryAdapter.OnItemClickListener() {
////            @Override
//            public void onItemClick(int position) {
//            }
//
//            @Override
//            public void onItemClick1(LibraryCardItem item) {
//                Intent intent = new Intent(Library.this, LibraryEditCard.class);
//            }
//        });

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ADD_NOTE_REQUEST && resultCode == RESULT_OK){
            String title = data.getStringExtra(LibraryEditCard.EXTRA_TITLE);
            String text = data.getStringExtra(LibraryEditCard.EXTRA_TEXT);

            LibraryCardItem newCard = new LibraryCardItem(title, text);
            //newCard.setId(0);
            savedList.add(newCard);
            mAdapter.notifyItemInserted(savedList.size()-1);
            saveData();
            loadData();

            Toast.makeText(this, "New card saved", Toast.LENGTH_SHORT).show();
        } else if(requestCode == EDIT_NOTE_REQUEST && resultCode == RESULT_OK){
            int id = data.getIntExtra(LibraryEditCard.EXTRA_ID, -1);

            //only if something went wrong
            if (id == -1){
                Toast.makeText(this, "Note can't be updated", Toast.LENGTH_SHORT).show();
                return;
            }

            String title = data.getStringExtra(LibraryEditCard.EXTRA_TITLE);
            String text = data.getStringExtra(LibraryEditCard.EXTRA_TEXT);
            LibraryCardItem newCard = new LibraryCardItem(title, text);
            newCard.setId(id);
            update(newCard, currentPos);
            buildRecyclerView(savedList);

            Toast.makeText(this, "Card updated", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Card not saved", Toast.LENGTH_SHORT).show();
        }
    }

    public void buildRecyclerView(ArrayList<LibraryCardItem> savedList) {
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
        MaterialSearchView searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setMenuItem(searchItem);

        showArchive = menu.findItem(R.id.show_archive);
        showArchive.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(showArchive.getTitle().equals("archive")) {
                    showArchive.setIcon(R.drawable.ic_library_white);
                    showArchive.setTitle("library");
                    mAdapter.setLibraryItems(archivedItems);
                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorLight));
                } else {
                    showArchive.setIcon(R.drawable.ic_archive_white);
                    showArchive.setTitle("archive");
                    mAdapter.setLibraryItems(savedList);
                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                return false;
            }
        });

        return true;
    }

    public void update(LibraryCardItem newCard, int position){
        savedList.add(position, newCard);
        mAdapter.notifyItemInserted(position);
        savedList.remove(position+1);
        mAdapter.notifyItemRemoved(position);
        saveData();
        loadData();
    }

    private void setUpSearch(){
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
                mAdapter.setLibraryItems(savedList);
            }
        });
    }

    private void processQuery(String query){
        ArrayList<LibraryCardItem> result = new ArrayList<>();

        for (LibraryCardItem item : savedList){
            if (item.getItemTitle().toLowerCase().contains(query.toLowerCase())){
                result.add(item);
            }
        }

        mAdapter.setLibraryItems(result);
    }

    @Override
    public void onItemClick(int position) {
        Intent detailIntent = new Intent(this, LibraryPopActivity.class);
        LibraryCardItem clickedItem = savedList.get(position);

        detailIntent.putExtra(Intent.EXTRA_TITLE, clickedItem.getItemTitle());
        detailIntent.putExtra(Intent.EXTRA_TEXT, clickedItem.getItemText());

        startActivity(detailIntent);
    }

    @Override
    public void onItemClick1(LibraryCardItem item, int position) {
        currentPos = position;
        Intent intent = new Intent(Library.this,LibraryEditCard.class);
        intent.putExtra(LibraryEditCard.EXTRA_ID, item.getId());
        intent.putExtra(LibraryEditCard.EXTRA_TITLE, item.getItemTitle());
        intent.putExtra(LibraryEditCard.EXTRA_TEXT, item.getItemText());
        startActivityForResult(intent, EDIT_NOTE_REQUEST);
    }

    //deleting function
    LibraryCardItem deletedItem = null;
    ArrayList<LibraryCardItem> archivedItems = new ArrayList<>();
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END , ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            Collections.swap(savedList, fromPosition, toPosition);

            recyclerView.getAdapter().notifyItemMoved(fromPosition,toPosition);

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            switch(direction){
                case ItemTouchHelper.LEFT: //right to left
                    Snackbar snack;
                    if(showArchive.getTitle().equals("archive")) {
                        deletedItem = savedList.get(position);
                        savedList.remove(position);
                        mAdapter.notifyItemRemoved(position);

                        snack = Snackbar.make(mRecyclerView, deletedItem.getItemTitle() + " deleted.", Snackbar.LENGTH_LONG);
                        snack.setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                savedList.add(position, deletedItem);
                                mAdapter.notifyItemInserted(position);
                                saveData();
                            }
                        });
                    } else {
                        deletedItem = archivedItems.get(position);
                        archivedItems.remove(position);
                        mAdapter.notifyItemRemoved(position);

                        snack = Snackbar.make(mRecyclerView, deletedItem.getItemTitle() + " deleted.", Snackbar.LENGTH_LONG);
                        snack.setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                archivedItems.add(position, deletedItem);
                                mAdapter.notifyItemInserted(position);
                                saveData();
                            }
                        });
                    }

                    snack.setAnchorView(R.id.nav_bar);
                    snack.show();

                    saveData();
                    break;
                case ItemTouchHelper.RIGHT: //left to right
                    if(showArchive.getTitle().equals("archive")) {
                        final LibraryCardItem cardItem = savedList.get(position);
                        archivedItems.add(cardItem);
                        savedList.remove(position);
                        mAdapter.notifyItemRemoved(position);

                        snack = Snackbar.make(mRecyclerView, cardItem.getItemTitle() + " archived.", Snackbar.LENGTH_LONG);
                        snack.setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                archivedItems.remove(archivedItems.lastIndexOf(cardItem));
                                savedList.add(position, cardItem);
                                mAdapter.notifyItemInserted(position);
                                saveData();
                            }
                        });
                    } else {
                        final LibraryCardItem cardItem = archivedItems.get(position);
                        archivedItems.remove(position);
                        savedList.add(cardItem);
                        mRecyclerView.setItemAnimator(null);
                        mAdapter.notifyItemInserted(position);

                        snack = Snackbar.make(mRecyclerView, cardItem.getItemTitle() + " sent to library.", Snackbar.LENGTH_LONG);
                        snack.setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                archivedItems.add(position, cardItem);
                                savedList.remove(position);
                                mAdapter.notifyItemRemoved(position);
                                saveData();
                            }
                        });
                    }

                    snack.setAnchorView(R.id.nav_bar);
                    snack.show();
                    saveData();
                    break;
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if(showArchive.getTitle().equals("archive")) {
                new RecyclerViewSwipeDecorator.Builder(Library.this, c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(Library.this, R.color.design_default_color_error))
                        .addSwipeLeftActionIcon(R.drawable.ic_delete)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(Library.this, R.color.search_layover_bg))
                        .addSwipeRightActionIcon(R.drawable.ic_archive)
                        .create()
                        .decorate();
            } else {
                new RecyclerViewSwipeDecorator.Builder(Library.this, c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(Library.this, R.color.design_default_color_error))
                        .addSwipeLeftActionIcon(R.drawable.ic_delete)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(Library.this, R.color.search_layover_bg))
                        .addSwipeRightActionIcon(R.drawable.ic_unarchive)
                        .create()
                        .decorate();
            }


            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    //to save UI states
    private void saveData(){
        //no other app can change our shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(savedList);
        String json2 = gson.toJson(archivedItems);
        editor.putString(SAVED_LIST, json);
        editor.putString("ARCHIVED", json2);
        editor.apply();
    }

    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(SAVED_LIST, null);
        String json2 = sharedPreferences.getString("ARCHIVED", null);
        Type type = new TypeToken<ArrayList<LibraryCardItem>>() {}.getType();
        Type type2 = new TypeToken<ArrayList<LibraryCardItem>>() {}.getType();
        savedList = gson.fromJson(json, type);
        archivedItems = gson.fromJson(json2, type2);

        if (savedList == null){
            savedList = new ArrayList<LibraryCardItem>();
        }
        if (archivedItems == null){
            archivedItems = new ArrayList<LibraryCardItem>();
        }
    }
}
