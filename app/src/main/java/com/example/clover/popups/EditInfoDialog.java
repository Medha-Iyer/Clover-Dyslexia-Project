package com.example.clover.popups;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.clover.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class EditInfoDialog extends DialogFragment implements View.OnClickListener {

    private FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userID = fAuth.getCurrentUser().getUid();
    private DocumentReference documentReference = fStore.collection("users").document(userID);

    private View view;
    private EditText editField;
    private TextView cancel, update;

    private String fieldTitle;
    public profileInput pInput;

    public EditInfoDialog(String f) {
        fieldTitle = f;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_edit_info, container, false);

        editField = view.findViewById(R.id.edit_name);
        editField.setHint(fieldTitle);

        cancel = view.findViewById(R.id.action_cancel);
        cancel.setOnClickListener(this);

        update = view.findViewById(R.id.action_update);
        update.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.action_update:
                Log.d(TAG, "onClick: capturing input.");
                String newField = editField.getText().toString();
                if (!newField.equals("")) {
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
                break;
            case R.id.action_cancel:
                Log.d(TAG, "onClick: closing dialog");
                getDialog().dismiss();
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            pInput = (profileInput) getTargetFragment();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException : " + e.getMessage());
        }
    }

    public interface profileInput {
        void applyText(String newField);
    }
}