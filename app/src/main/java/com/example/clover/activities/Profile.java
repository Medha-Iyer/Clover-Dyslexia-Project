package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clover.R;
import com.example.clover.adapters.FragmentAdapter;
import com.example.clover.adapters.ProgressCheckAdapter;
import com.example.clover.fragments.ProfileProgressCheck;
import com.example.clover.fragments.SettingsPersonalInfo;
import com.example.clover.pojo.LibraryCardItem;
import com.example.clover.pojo.ProgressCheckItem;
import com.example.clover.pojo.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.example.clover.pojo.GameItem;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.security.AccessController.getContext;

public class Profile extends AppCompatActivity implements View.OnClickListener, ProgressCheckAdapter.OnItemClickListener, ProfilePicDialog.PictureDialogListener {
    TextView fullName;
    public static int picCode;

    private AdView mAdView;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private CardView profile_card;
    private ImageView pfp_temp, pfp_background;
    private CircleImageView pfp;

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    StorageReference profileRef = storageReference.child("users/" + userId + "/profile.jpg");
    DocumentReference documentReference = fStore.collection("users").document(userId);
    private final String TAG = "Profile";
    private boolean darkmode;

    private RecyclerView mRecyclerView;
    private ProgressCheckAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This has to be implemented in every screen to update mode and theme.
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(Profile.this, "Error while loading!", Toast.LENGTH_SHORT).show();
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

        setContentView(R.layout.activity_profile);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("9F59EB48A48DC1D3C05FCBCA3FBAC1F9").build();
        mAdView.loadAd(adRequest);

        pfp_temp = findViewById(R.id.profile_temp);
        pfp = findViewById(R.id.profile_photo);
        profile_card = findViewById(R.id.profile_card);
        profile_card.setOnClickListener(this);
        pfp_background = findViewById(R.id.profile_background);
        pfp_background.setOnClickListener(this);
        fullName = findViewById(R.id.prof_name);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference profileRef = storageReference.child("users/" + userId + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                pfp_temp.setVisibility(View.INVISIBLE);
                pfp.setVisibility(View.VISIBLE);
                Picasso.get().load(uri).into(pfp);
            }
        });

        //for profile background
        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference profileBackRef = storageReference.child("users/" + userId + "/profileback.jpg");
        profileBackRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(pfp_background);
            }
        });

        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(Profile.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                fullName.setText(documentSnapshot.getString("name"));
            }
        });

        buildRecyclerView();

        //set profile as selected
        BottomNavigationView navView = findViewById(R.id.nav_bar);
        //set home as selected
        navView.setSelectedItemId(R.id.profile);
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
                        return true;
                    case R.id.profile:
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
    public void onClick(View v){
        switch(v.getId()){
            case R.id.profile_card:
                picCode = 0;
                ProfilePicDialog uploadPfp = new ProfilePicDialog();
                uploadPfp.show(getSupportFragmentManager(), "upload profile");
                break;
            case R.id.profile_background:
                picCode = 1;
                ProfilePicDialog uploadPfpBack = new ProfilePicDialog();
                uploadPfpBack.show(getSupportFragmentManager(), "upload profile background");
                break;
        }
    }

    public void uploadPicture(Bitmap b, Uri u){
        //pfp.setImageBitmap(b);
        pfp_temp.setVisibility(View.INVISIBLE);
        pfp.setVisibility(View.VISIBLE);
        uploadImageToFirebase(u);
    }

    private void uploadImageToFirebase(Uri uri){
        final StorageReference fileRef = storageReference.child("users/" + userId + "/profile.jpg");
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(pfp);
                    }
                });
                Toast.makeText(Profile.this, "Image uploaded", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Profile.this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void uploadBackgroundPicture(Bitmap b, Uri u){
        //pfp.setImageBitmap(b);
        pfp_temp.setVisibility(View.INVISIBLE);
        pfp.setVisibility(View.VISIBLE);
        uploadBackgroundImageToFirebase(u);
    }

    private void uploadBackgroundImageToFirebase(Uri uri){
        //background
        final StorageReference fileBackRef = storageReference.child("users/" + userId + "/profileback.jpg");
        fileBackRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileBackRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(pfp_background);
                    }
                });
                Toast.makeText(Profile.this, "Image uploaded", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Profile.this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void buildRecyclerView() {
        ArrayList<ProgressCheckItem> savedList = new ArrayList<>();
        savedList.add(new ProgressCheckItem("SPELLING", R.drawable.abc));
        savedList.add(new ProgressCheckItem("VOICE", R.drawable.voice_game));
        savedList.add(new ProgressCheckItem("MATCH", R.drawable.lock));
        savedList.add(new ProgressCheckItem("BLENDS", R.drawable.lock));
        mRecyclerView = findViewById(R.id.progressRecycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mAdapter = new ProgressCheckAdapter(savedList); //passes to adapter, then presents to viewholder

        mRecyclerView.setLayoutManager((mLayoutManager));
        mRecyclerView.setAdapter(mAdapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerView);

        mAdapter.setOnItemClickListener((ProgressCheckAdapter.OnItemClickListener) this);
    }

    @Override
    public void onItemClick(int position){
        Intent i;
        switch (position) {
            case 0:
                i = new Intent(this, ProfileProgress.class);
                i.putExtra(ProfileProgress.EXTRA_ID, 1);
                startActivity(i);
                break;
            case 1:
                i = new Intent(this, ProfileProgress.class);
                i.putExtra(ProfileProgress.EXTRA_ID, 0);
                startActivity(i);
                break;
            case 2:
                Toast.makeText(this, "Get Clover Pro to unlock this game.", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(this, "Get Clover Pro to unlock this game.", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}