package com.example.clover.pojo;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;

import com.example.clover.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Utils
{
    private static int sTheme;
    private static int index;
    public final static int THEME_DEFAULT = 0;
    public final static int DARK_THEME_DEFAULT = 1;
    public final static int THEME_PINK = 2;
    public final static int DARK_THEME_PINK = 3;
    public final static int THEME_GREEN = 4;
    public final static int DARK_THEME_GREEN = 5;

    //adding popup
    public final static int POPUP_LIGHT_ONE = 6;
    public final static int POPUP_DARK_ONE = 7;
    public final static int POPUP_LIGHT_TWO = 8;
    public final static int POPUP_DARK_TWO = 9;
    public final static int POPUP_LIGHT_THREE = 10;
    public final static int POPUP_DARK_THREE = 11;

    static FirebaseAuth fAuth = FirebaseAuth.getInstance();
    static FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private static String userId = fAuth.getCurrentUser().getUid();
    static DocumentReference documentReference = fStore.collection("users").document(userId);

    /**
     * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
     */
    public static void changeToTheme(Activity activity, int theme)
    {
        if(sTheme == theme){
            return;
        }
        sTheme = theme;
        saveData();
        Log.d("Utils", Integer.toString(sTheme));
        switch (sTheme) {
            default:
            case THEME_DEFAULT:
                index = 0;
                break;
            case DARK_THEME_DEFAULT:
                index = 0;
                break;
            case THEME_PINK:
                index = 1;
                break;
            case DARK_THEME_PINK:
                index = 1;
                break;
            case THEME_GREEN:
                index = 2;
                break;
            case DARK_THEME_GREEN:
                index = 2;
                break;
        }
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
    }

    public static void changeToPopup(Activity activity, Window window){
        switch (sTheme)
        {
            default:
            case THEME_DEFAULT:
                activity.setTheme(R.style.LightTheme1PopMe);
                break;
            case DARK_THEME_DEFAULT:
                activity.setTheme(R.style.DarkTheme1PopMe);
                break;
            case THEME_PINK:
                activity.setTheme(R.style.LightTheme2PopMe);
                break;
            case DARK_THEME_PINK:
                activity.setTheme(R.style.DarkTheme2PopMe);
                break;
            case THEME_GREEN:
                activity.setTheme(R.style.LightTheme3PopMe);
                break;
            case DARK_THEME_GREEN:
                activity.setTheme(R.style.DarkTheme3PopMe);
                break;
        }
    }

    /** Set the theme of the activity, according to the configuration. */
    public static void onActivityCreateSetTheme(Activity activity)
    {
        switch (sTheme)
        {
            default:
            case THEME_DEFAULT:
                activity.setTheme(R.style.LightTheme1);
                break;
            case DARK_THEME_DEFAULT:
                activity.setTheme(R.style.DarkTheme1);
                break;
            case THEME_PINK:
                activity.setTheme(R.style.LightTheme2);
                break;
            case DARK_THEME_PINK:
                activity.setTheme(R.style.DarkTheme2);
                break;
            case THEME_GREEN:
                activity.setTheme(R.style.LightTheme3);
                break;
            case DARK_THEME_GREEN:
                activity.setTheme(R.style.DarkTheme3);
                break;
        }
    }

    public static void checkRadio(RadioGroup radioGroup){
        ((RadioButton)radioGroup.getChildAt(index)).setChecked(true);
        Log.d("Utils", Integer.toString(index));
    }

    public static void changeToDark(Activity activity){
        if(sTheme%2 == 0){
            Log.d("Utils", "dark" + Integer.toString(sTheme+1));
            changeToTheme(activity, sTheme+1);
        }
    }

    public static void changeToLight(Activity activity){
        if(sTheme%2 != 0){
            Log.d("Utils", "light" + Integer.toString(sTheme-1));
            changeToTheme(activity, sTheme-1);
        }
    }

    public static void setTheme(int theme){
        sTheme = theme;
    }

    public static int getTheme(){
        Log.d("Utils", Integer.toString(sTheme));
        return sTheme;
    }

    public static void saveData(){
        documentReference.update(
                "theme", Integer.toString(sTheme))
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
}