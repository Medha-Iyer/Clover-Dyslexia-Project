package com.example.clover.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.cardview.widget.CardView;

import com.example.clover.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class ProfilePicDialog extends AppCompatDialogFragment implements View.OnClickListener {
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    DocumentReference documentReference;
    StorageReference storageReference;

    private Uri imageUri;
    private Bitmap imageBitmap;

    private String userID;

    private CardView openCam, openGallery;
    private ImageView close;

    //constants for taking photos
    private static final int SELECT_PICTURE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    public interface PictureDialogListener{
        void uploadPicture(Bitmap b, Uri u);
    }

    public PictureDialogListener pUpdate;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_profile_pic, container, false);

        close = view.findViewById(R.id.action_cancel);
        close.setOnClickListener(this);
        openCam = view.findViewById(R.id.camera_pfp);
        openCam.setOnClickListener(this);
        openGallery = view.findViewById(R.id.gallery_pfp);
        openGallery.setOnClickListener(this);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        documentReference = fStore.collection("users").document(userID);
        storageReference = FirebaseStorage.getInstance().getReference();

        return view;
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.camera_pfp:
                dispatchTakePictureIntent();
                break;
            case R.id.gallery_pfp:
                dispatchFromGalleryIntent();
                break;
            case R.id.action_cancel:
                Log.d(TAG, "onClick: closing dialog");
                getDialog().dismiss();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageUri = data.getData();
            pUpdate.uploadPicture(imageBitmap, imageUri);
            getDialog().dismiss();
        } else if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                pUpdate.uploadPicture(imageBitmap, imageUri);
                getDialog().dismiss();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            pUpdate = (PictureDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement PictureDialogListener");
        }
    }

}

