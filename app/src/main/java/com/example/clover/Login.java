package com.example.clover;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout mainLogin;
    private ImageView splashlogo;
    private TextView mcreateAccount;
    private CardView mloginButton;
    EditText mEmail, mPassword;
    private ProgressBar mPbar;
    FirebaseAuth fAuth;
    private Handler handler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(fAuth.getCurrentUser() != null) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }else {
                mainLogin.setVisibility(View.VISIBLE);
                splashlogo.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //initialize and assign variable, do this for every button or other interactive feature
        mainLogin = findViewById(R.id.mainlogin);
        splashlogo = findViewById(R.id.splashlogo);
        mcreateAccount = findViewById(R.id.createAccount);
        mcreateAccount.setOnClickListener(this);
        mloginButton = findViewById(R.id.loginButton);
        mloginButton.setOnClickListener(this);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPbar = findViewById(R.id.progressBar);
        fAuth = FirebaseAuth.getInstance();

        handler.postDelayed(runnable, 3000); //3000 is the timeout for the splash

    }

    @Override
    protected void onPause(){
        super.onPause();
        if(fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.loginButton:
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                //Check if user has entered valid information
                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is required.");
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

                //authenticate user
                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Login.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }else{
                            Toast.makeText(Login.this, "ERROR!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            case R.id.createAccount:
                startActivity(new Intent(getApplicationContext(), Register.class));
        }
    }
}
