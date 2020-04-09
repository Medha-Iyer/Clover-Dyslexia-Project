package com.example.clover;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;

public class Register extends AppCompatActivity implements View.OnClickListener {

    private TextView mLoginHere;
    private CardView mRegisterButton;
    EditText mFullName, mEmail, mAge, mPassword;
    private ProgressBar mPbar;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //initialize and assign variable, do this for every button or other interactive feature
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

        if(fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registerButton:
                String email = mEmail.getText().toString().trim();
                String age = mAge.getText().toString().trim();
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
                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Register.this, "User successfully created an account", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }else{
                            Toast.makeText(Register.this, "ERROR!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            case R.id.loginHere:
                startActivity(new Intent(getApplicationContext(), Login.class));
        }
    }

}
