package com.example.clover.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clover.R;
import com.example.clover.pojo.LightbulbAnimation;
import com.example.clover.pojo.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;


public class Login extends AppCompatActivity implements View.OnClickListener {

    AnimationDrawable lightbulb;

    private RelativeLayout mainLogin, mainLayout;
    private ImageView splashlogo;
    private TextView mcreateAccount, mforgotPassword, splashwords;
    private CardView mloginButton;
    EditText mEmail, mPassword;
    private ProgressBar mPbar;
    private Handler handler = new Handler();

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
//    private String userId= fAuth.getCurrentUser().getUid();
    private String userId;
    DocumentReference documentReference;
//    DocumentReference documentReference = fStore.collection("users").document(userId);
    private final String TAG = "Login";
    private boolean darkmode;

    Runnable hideViews = new Runnable() {
        @Override
        public void run() {
            splashlogo.setVisibility(View.GONE);
            splashwords.setVisibility(View.GONE);
            mainLayout.setBackgroundColor(getResources().getColor(R.color.mainBlue));
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() { //TODO splash screen shows up after logout but we only want it when the app starts
            if(fAuth.getCurrentUser() != null) {
                userId= fAuth.getCurrentUser().getUid();
                documentReference = fStore.collection("users").document(userId);
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }else {
                mainLogin.setBackgroundColor(getResources().getColor(R.color.mainBlue));
                splashwords.animate().alpha(0f).setDuration(1000);
                splashlogo.animate().alpha(0f).setDuration(1000);
                mainLogin.animate().alpha(1f).setDuration(1000);
            }
            handler.postDelayed(hideViews, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This has to be implemented in every screen to update mode and theme.
        if(fAuth.getCurrentUser() != null) {
            userId = fAuth.getCurrentUser().getUid();
            documentReference = fStore.collection("users").document(userId);

            documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if (e != null) {
                        Toast.makeText(Login.this, "Error while loading!", Toast.LENGTH_SHORT).show();
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
//                        darkmode = documentSnapshot.getBoolean("darkmode");
//                        Utils.setTheme(Integer.parseInt(documentSnapshot.getString("theme")));
                        if (darkmode) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        }
                    }
                }
            });
            Utils.onActivityCreateSetTheme(this);

            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                Utils.changeToDark(this);
            } else {
                Utils.changeToLight(this);
            }
        }
        setContentView(R.layout.activity_login);

    //initialize and assign variable, do this for every button or other interactive feature
    mainLogin = findViewById(R.id.mainlogin);
    mainLayout = findViewById(R.id.mainLayout);
    splashwords = findViewById(R.id.splashwords);
    splashlogo = findViewById(R.id.splashlogo);
    splashlogo.setImageResource(R.drawable.lightbulb_animation);
    lightbulb = (AnimationDrawable) splashlogo.getDrawable();
    mcreateAccount = findViewById(R.id.createAccount);
    mcreateAccount.setOnClickListener(this);
    mforgotPassword = findViewById(R.id.forgotPassword);
    mforgotPassword.setOnClickListener(this);
    mloginButton = findViewById(R.id.loginButton);
    mloginButton.setOnClickListener(this);
    mEmail = findViewById(R.id.email);
    mPassword = findViewById(R.id.password);
    mPbar = findViewById(R.id.progressBar);
    fAuth = FirebaseAuth.getInstance();

    handler.postDelayed(runnable, 4000); //4000 is the timeout for the splash

}

//    @Override
//    protected void onStart() {
//        super.onStart();
//        final LightbulbAnimation anim = new LightbulbAnimation(lightbulb) {
//            @Override
//            public void onAnimationFinish() {
//                splashwords.animate().alpha(1f).setDuration(200);
//                handler.postDelayed(runnable, 1000); //3000 is the timeout for the splash
//            }
//
//            @Override
//            public void onAnimationStart() {
//            }
//        };
//
//        //splashlogo.setBackground(anim);
//        anim.start();
//    }

    @Override
    protected void onStart() {
        super.onStart();
        lightbulb.start();
        splashwords.animate().alpha(1f).setDuration(2050);
    }


    //    @Override
//    protected void onPause(){
//        super.onPause();
//        if(fAuth.getCurrentUser() != null) {
//            startActivity(new Intent(getApplicationContext(), MainActivity.class));
//            finish();
//        }
//    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.createAccount:
                startActivity(new Intent(getApplicationContext(), Register.class));
                break;
            case R.id.forgotPassword:
                final EditText reset = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset password?");
                passwordResetDialog.setMessage("Enter your email to receive a link to reset your password.");
                passwordResetDialog.setView(reset);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // retrieve email and send reset link
                        String mail = reset.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Login.this, "Reset link sent to your email.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Login.this, "Error! Link has not been sent. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //return to login screen and close dialog
                    }
                });

                passwordResetDialog.create().show();
                break;
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
                            mPbar.setVisibility(View.GONE);
                        }
                    }
                });
                break;
        }
    }

}
