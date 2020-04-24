package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.clover.R;
import com.example.clover.pojo.UserItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity implements View.OnClickListener {

    RelativeLayout mainRegister;
    ImageView splashlogo;
    private TextView mLoginHere;
    private CardView mRegisterButton;
    EditText mFullName, mEmail, mAge, mPassword;
    String userID;
    private ProgressBar mPbar;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    private Handler handler  = new Handler();
    private static final String TAG = MainActivity.class.getSimpleName();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(fAuth.getCurrentUser() != null) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }else {
                mainRegister.setVisibility(View.VISIBLE);
                splashlogo.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //initialize and assign variable, do this for every button or other interactive feature
        mainRegister = findViewById(R.id.mainregister);
        splashlogo = findViewById(R.id.splashlogo);
        mLoginHere = findViewById(R.id.loginHere);
        mLoginHere.setOnClickListener(this);
        mRegisterButton = findViewById(R.id.registerButton);
        mRegisterButton.setOnClickListener(this);
        mFullName = findViewById(R.id.fullname);
        mEmail = findViewById(R.id.email);
        mAge = findViewById(R.id.age);
        mPassword = findViewById(R.id.password);
        mPbar = findViewById(R.id.progressBar);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        handler.postDelayed(runnable, 2000); //2000 is the timeout for the splash
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginHere:
                startActivity(new Intent(getApplicationContext(), Login.class));
                break;
            case R.id.registerButton:
                final String fullname = mFullName.getText().toString();
                final String email = mEmail.getText().toString().trim();
                final String age = mAge.getText().toString();
                String password = mPassword.getText().toString().trim();

                //Check if user has entered valid information
                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is required.");
                    return;
                }

                if(TextUtils.isEmpty(age)){
                    mEmail.setError("Age is required.");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is required.");
                    return;
                }

                if(password.length() < 6){
                    mPassword.setError("Password must be at least 6 characters.");
                    return;
                }
                mPbar.setVisibility(View.VISIBLE);

                //Register user in Firebase console this or activity.this??
                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Register.this, "User successfully created an account", Toast.LENGTH_SHORT).show();
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userID);
                            UserItem user = new UserItem(fullname, email, age, "50", "50", false, "2131296529");
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "User profile is created for " + userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }else{
                            Toast.makeText(Register.this, "ERROR!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            mPbar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
        }
    }

}
