package com.example.clover.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;

import com.example.clover.R;
import com.example.clover.pojo.PersonalInfoItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ProfileNameDialog extends DialogFragment {
    private View view;

    private EditText editField;
    private TextView cancel, update;
    private String fieldTitle;
    //private ProfileDialogListener listener;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    DocumentReference documentReference;
    private String userID;

    public ProfileNameDialog(String f){
        fieldTitle = f;
    }

    public interface profileInput{
        void applyText(String newField);
    }

    public profileInput pInput;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_profile, container, false);
        editField = view.findViewById(R.id.edit_name);
        editField.setHint(fieldTitle);
        cancel = view.findViewById(R.id.action_cancel);
        update = view.findViewById(R.id.action_update);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        documentReference = fStore.collection("users").document(userID);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing dialog");
                getDialog().dismiss();
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: capturing input.");

                String newField = editField.getText().toString();
                if(!newField.equals("")){
                    Log.d("field", newField);
                    pInput.applyText(newField);
                    documentReference.update(fieldTitle.toLowerCase(), newField)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Data saved to Firestore");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating document", e);
                            }
                        });
                }
                getDialog().dismiss();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            pInput = (profileInput) getTargetFragment();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException : " + e.getMessage() );
        }
    }

//    @NonNull
//    @Override
//    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        LayoutInflater inflater = getTargetFragment().getLayoutInflater();
//        View view = inflater.inflate(R.layout.dialog_profile, null);
//
//        fAuth = FirebaseAuth.getInstance();
//        fStore = FirebaseFirestore.getInstance();
//        userID = fAuth.getCurrentUser().getUid();
//        documentReference = fStore.collection("users").document(userID);
//
//        builder.setView(view)
//                .setTitle("Reset " + fieldTitle)
//                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                })
//                .setPositiveButton("update", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        String newField = editField.getText().toString();
//                        listener.applyTexts(newField);
//                        documentReference.update(fieldTitle, newField)
//                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void aVoid) {
//                                        Log.d(TAG, "Data saved to Firestore");
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Log.w(TAG, "Error updating document", e);
//                                    }
//                                });
//                    }
//                });
//
//        editField = view.findViewById(R.id.edit_name);
//        editField.setHint(fieldTitle);
//
//        return builder.create();
//    }
//
//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//
//        try {
//            listener = (ProfileDialogListener) context;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(context.toString() +
//                    "must implement ProfileDialogListener");
//        }
//    }
//
//    public interface ProfileDialogListener{
//        void applyTexts(String newField);
//    }
}
