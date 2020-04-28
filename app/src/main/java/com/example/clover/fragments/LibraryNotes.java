package com.example.clover.fragments;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;

import com.example.clover.R;
import com.example.clover.activities.Camera;
import com.example.clover.activities.LibraryPopActivity;
import com.example.clover.adapters.LibraryAdapter;
import com.example.clover.pojo.LibraryCardItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import android.graphics.Canvas;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.clover.activities.LibraryEditCard;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1.WriteResult;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import java.util.Collections;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static android.app.Activity.RESULT_OK;

public class LibraryNotes extends Fragment implements LibraryAdapter.OnItemClickListener {
    View view;
    private FloatingActionButton addNoteBtn;
    private MenuItem showArchive;
    private Toolbar toolbar;

    //for recycler view format
    private RecyclerView mRecyclerView;
    private LibraryAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<LibraryCardItem> libraryItems = new ArrayList<LibraryCardItem>();
    private ArrayList<LibraryCardItem> archivedItems = new ArrayList<>();
    private ArrayList<LibraryCardItem> completeList = new ArrayList<>();
    private LibraryCardItem newCard;
    private int currentPos, cpList;

    //for firebase
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private DocumentReference documentReference;
    private String userID;
    private ArrayList<LibraryCardItem> firebaseList = new ArrayList<LibraryCardItem>();

    //for editing or adding note
    public static final int ADD_NOTE_REQUEST = 1;
    public static final int EDIT_NOTE_REQUEST = 2;

    public LibraryNotes() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notes, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        addNoteBtn = view.findViewById(R.id.add_button);
        addNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), LibraryEditCard.class);
                startActivityForResult(intent, ADD_NOTE_REQUEST);
            }
        });

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        saveToFirebase();
        readLibraryData(new LibraryNotes.LibraryCallback() {
            @Override
            public void onCallback(ArrayList<LibraryCardItem> firebaseList) { //loads firebase library if it exists
                Log.d("Library", "Inside callback");
                completeList = firebaseList;
                separateLists(firebaseList);

                //only run if coming from Camera after saving items
                if (Camera.newCard != null) {
                    addCard(Camera.newCard);
                }

                buildRecyclerView(getListToUse());
                setUpSearch();
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                itemTouchHelper.attachToRecyclerView(mRecyclerView);
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.example_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        MaterialSearchView searchView = (MaterialSearchView) view.findViewById(R.id.search_view);
        searchView.setMenuItem(searchItem);

        showArchive = menu.findItem(R.id.show_archive);
        showArchive.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                TypedValue typedValue = new TypedValue();

                if (toolbar.getTitle().equals("Library")) { //if you're going to Archive
                    showArchive.setIcon(R.drawable.ic_library_white);
                    addNoteBtn.setVisibility(View.GONE);
                    getContext().getTheme().resolveAttribute(R.attr.baseDark, typedValue, true);
                    toolbar.setTitle("Archives");
                } else { //if you're switching back to Library
                    showArchive.setIcon(R.drawable.ic_archive_white);
                    addNoteBtn.setVisibility(View.VISIBLE);
                    getContext().getTheme().resolveAttribute(R.attr.accentSelect, typedValue, true);
                    toolbar.setTitle("Library");
                }
                buildRecyclerView(getListToUse());

                // it's probably a good idea to check if the color wasn't specified as a resource
                if (typedValue.resourceId != 0) {
                    toolbar.setBackgroundResource(typedValue.resourceId);
                } else {
                    // this should work whether there was a resource id or not
                    toolbar.setBackgroundColor(typedValue.data);
                }

                return false;
            }
        });
    }

    //when calling firebase for data
    public interface LibraryCallback {
        void onCallback(ArrayList<LibraryCardItem> progressList);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_NOTE_REQUEST && resultCode == RESULT_OK) {
            String title = data.getStringExtra(LibraryEditCard.EXTRA_TITLE);
            String text = data.getStringExtra(LibraryEditCard.EXTRA_TEXT);
            LibraryCardItem newCard = new LibraryCardItem(title, text);
            addCard(newCard);
            buildRecyclerView(libraryItems);
            Toast.makeText(getContext(), "New card saved", Toast.LENGTH_SHORT).show();

        } else if (requestCode == EDIT_NOTE_REQUEST && resultCode == RESULT_OK) {
            int id = data.getIntExtra(LibraryEditCard.EXTRA_ID, -1);

            //only if something went wrong
            if (id == -1) {
                Toast.makeText(getContext(), "Note can't be updated", Toast.LENGTH_SHORT).show();
                return;
            }

            String title = data.getStringExtra(LibraryEditCard.EXTRA_TITLE);
            String text = data.getStringExtra(LibraryEditCard.EXTRA_TEXT);
            LibraryCardItem newCard = new LibraryCardItem(title, text);
            newCard.setId(id);

            update(newCard, currentPos, 1);
            Toast.makeText(getContext(), "Card updated", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getContext(), "Card not saved", Toast.LENGTH_SHORT).show();
        }
    }

    @Override //open card in pop-up view
    public void onItemClick(int position) {
        Intent detailIntent = new Intent(getContext(), LibraryPopActivity.class);
        LibraryCardItem clickedItem = getListToUse().get(position);

        detailIntent.putExtra(Intent.EXTRA_TITLE, clickedItem.getItemTitle());
        detailIntent.putExtra(Intent.EXTRA_TEXT, clickedItem.getItemText());

        startActivity(detailIntent);
    }

    @Override //edit card
    public void onItemClick1(LibraryCardItem item, int position) {
        currentPos = position;
        cpList = completeList.indexOf(item);
        Intent intent = new Intent(getContext(), LibraryEditCard.class);
        intent.putExtra(LibraryEditCard.EXTRA_ID, item.getId());
        intent.putExtra(LibraryEditCard.EXTRA_TITLE, item.getItemTitle());
        intent.putExtra(LibraryEditCard.EXTRA_TEXT, item.getItemText());
        startActivityForResult(intent, EDIT_NOTE_REQUEST);
    }

    //save completeList to firebase
    public void saveToFirebase() {
        String title;
        ArrayList<LibraryCardItem> listToUse = getListToUse();
        for (int i = 0; i < listToUse.size(); i++) {
            title = listToUse.get(i).getItemTitle();
            documentReference = fStore.collection("users").document(userID)
                    .collection("library").document(title);
            //TODO make it so that they can't name two things the same title
            documentReference.set(listToUse.get(i)).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Library", "Document saved to library collection");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Library", "onFailure: " + e.toString());
                }
            });
        }
    }

    //read data from firebase
    public void readLibraryData(final LibraryNotes.LibraryCallback lCallback) {
        firebaseList = new ArrayList<>();
        documentReference = fStore.collection("users").document(userID);
        documentReference.collection("library")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                LibraryCardItem loadItem = document.toObject(LibraryCardItem.class);
                                firebaseList.add(loadItem);
                            }
                            if (!firebaseList.isEmpty()) {
                                Log.d("Load library", "Success");
                                lCallback.onCallback(firebaseList);
                            }
                        } else {
                            Log.d("Load library", "Error getting documents: ", task.getException());
                            firebaseList = libraryItems; //If firebase library doesn't exist it is set to savedList
                        }
                    }
                });
    }
    public void readData(){
        readLibraryData(new LibraryNotes.LibraryCallback() {
            @Override
            public void onCallback(ArrayList<LibraryCardItem> firebaseList) { //loads firebase library if it exists
                Log.d("Library", "Inside callback");
                completeList = firebaseList;
                separateLists(firebaseList);
                buildRecyclerView(getListToUse());
            }
        });
    }

    //to add a new card
    public void addCard(LibraryCardItem item) {
        shiftPositionValues(1, 0, getListToUse());
        newCard = item;
        completeList.add(newCard);
        separateLists(completeList);
        saveToFirebase();
    }

    //to update an existing card
    public void update(LibraryCardItem newCard, int position, int code) {
        ArrayList<LibraryCardItem> listToUse = getListToUse();
        String oldCard;

        separateLists(completeList);
        if (code == 0) { //if archived, set position to last of opposite list
            shiftPositionValues(-1, position, getOpposite());
            newCard.setPosition(completeList.size() - getListToUse().size() - 1);
            oldCard=listToUse.get(position).getItemTitle();
        } else { //if archived, then undo, reset to original position
            oldCard = newCard.getItemTitle();
            newCard.setPosition(position);
        }

        completeList.set(cpList, newCard); //cp is the index of the card in completed list

        //delete existing card from firebase
        documentReference = fStore.collection("users").document(userID)
                .collection("library").document(oldCard);
        documentReference.delete();
//        completeList.remove(cpList+1);
        //upload updated version to firebase
        documentReference = fStore.collection("users").document(userID)
                .collection("library").document(newCard.getItemTitle());
        documentReference.set(newCard).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Library", "Document added to library collection");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Library", "onFailure: " + e.toString());
            }
        });

        readData();
    }

    //shift position values if new card is added or moved around
    public void shiftPositionValues(int shift, int start, ArrayList<LibraryCardItem> list) {
        for (int i = start; i < list.size(); i++) {
            int index = list.get(i).getPosition();
            list.get(i).setPosition(index + shift);
        }
    }

    //separate lists into archive or library based on state attribute
    public void separateLists(ArrayList<LibraryCardItem> list) {
        libraryItems = new ArrayList<>();
        archivedItems = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getState()) {
                libraryItems.add(list.get(i));
            } else {
                archivedItems.add(list.get(i));
            }
        }
        Collections.sort(libraryItems);
        Collections.sort(archivedItems);
    }

    public ArrayList<LibraryCardItem> getListToUse() {
        ArrayList<LibraryCardItem> listToUse = new ArrayList<>();
        if (toolbar.getTitle().equals("Archives")) {
            listToUse = archivedItems;
        } else {
            listToUse = libraryItems;
        }
        return listToUse;
    }
    public ArrayList<LibraryCardItem> getOpposite() {
        ArrayList<LibraryCardItem> listToUse = getListToUse();
        if (listToUse == libraryItems) {
            return archivedItems;
        } else {
            return libraryItems;
        }
    }

    //deleting function
    LibraryCardItem deletedItem = null;
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END , ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            ArrayList<LibraryCardItem> listToUse = getListToUse();

            int i = completeList.indexOf(listToUse.get(fromPosition));
            int j = completeList.indexOf(listToUse.get(toPosition));
            completeList.get(i).setPosition(toPosition);
            completeList.get(j).setPosition(fromPosition);

            separateLists(completeList);
            saveToFirebase();

            recyclerView.getAdapter().notifyItemMoved(fromPosition,toPosition);

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            final ArrayList<LibraryCardItem> listToUse = getListToUse();

            switch(direction){
                case ItemTouchHelper.LEFT: //right to left, delete
                    deletedItem = listToUse.get(position);

                    documentReference = fStore.collection("users").document(userID)
                            .collection("library").document(listToUse.get(position).getItemTitle());
                    documentReference.delete();

                    completeList.remove(deletedItem);
                    separateLists(completeList);
                    Log.d("shift","pos: "+position);
                    shiftPositionValues(-1,position, getListToUse());
                    buildRecyclerView(getListToUse());
                    saveToFirebase();

                    Snackbar snack;
                    snack = Snackbar.make(mRecyclerView, deletedItem.getItemTitle() + " deleted.", Snackbar.LENGTH_LONG);
                    snack.setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            shiftPositionValues(+1,position, getListToUse());
                            completeList.add(deletedItem);
                            separateLists(completeList);
                            buildRecyclerView(listToUse);
                            saveToFirebase();
                        }
                    });

                    snack.setAnchorView(R.id.nav_bar);
                    snack.show();
                    break;

                case ItemTouchHelper.RIGHT: //left to right, archive
                    final LibraryCardItem cardItem = getListToUse().get(position);
                    cardItem.switchState();
                    shiftPositionValues(-1, position, getListToUse());
                    saveToFirebase();

                    update(cardItem, position, 0);

                    String message;
                    if(toolbar.getTitle().equals("Library")){
                        message = " archived.";
                    } else {
                        message = " sent to library.";
                    }

                    snack = Snackbar.make(mRecyclerView, cardItem.getItemTitle() + message, Snackbar.LENGTH_LONG);
                    snack.setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cardItem.switchState();
                            shiftPositionValues(+1,position, getListToUse());
                            saveToFirebase();

                            update(cardItem, position, 2);
                        }
                    });

                    snack.setAnchorView(R.id.nav_bar);
                    snack.show();
                    break;
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if(showArchive.getTitle().equals("archive")) {
                new RecyclerViewSwipeDecorator.Builder(getContext(), c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(getContext(), R.color.design_default_color_error))
                        .addSwipeLeftActionIcon(R.drawable.ic_delete)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(getContext(), R.color.search_layover_bg))
                        .addSwipeRightActionIcon(R.drawable.ic_archive)
                        .create()
                        .decorate();
            } else {
                new RecyclerViewSwipeDecorator.Builder(getContext(), c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(getContext(), R.color.design_default_color_error))
                        .addSwipeLeftActionIcon(R.drawable.ic_delete)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(getContext(), R.color.search_layover_bg))
                        .addSwipeRightActionIcon(R.drawable.ic_unarchive)
                        .create()
                        .decorate();
            }


            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    //for searching, set up recycler view and cards
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
                if (toolbar.getTitle().equals("Archives")) {
                    buildRecyclerView(archivedItems);
                } else {
                    buildRecyclerView(libraryItems);
                }
            }
        });
    }
    private void processQuery(String query) {
        ArrayList<LibraryCardItem> result = new ArrayList<>();

        ArrayList<LibraryCardItem> listToUse;
        if (toolbar.getTitle().equals("Archives")) {
            listToUse = archivedItems;
        } else {
            listToUse = libraryItems;
        }

        for (LibraryCardItem item : listToUse) {
            if (item.getItemTitle().toLowerCase().contains(query.toLowerCase())) {
                result.add(item);
            }
        }

        mAdapter.setLibraryItems(result);
    }
    public void buildRecyclerView(ArrayList<LibraryCardItem> savedList) {
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new LibraryAdapter(savedList); //passes to adapter, then presents to viewholder
        mRecyclerView.setLayoutManager((mLayoutManager));
        mRecyclerView.setAdapter(mAdapter);

        //if individual card items get click (see onItemClick methods)
        mAdapter.setOnItemClickListener(LibraryNotes.this);

        //let add button disappear on scroll down
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0)
                    addNoteBtn.hide();
                else if (dy < 0)
                    addNoteBtn.show();
            }
        });

        Log.d("update after","library: "+libraryItems.size());
        Log.d("update after","archive: "+archivedItems.size());
        Log.d("update after","complete: "+completeList.size());
    }
}
