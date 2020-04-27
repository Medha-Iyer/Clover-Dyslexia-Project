package com.example.clover.pojo;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.clover.R;

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

    /**
     * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
     */
    public static void changeToTheme(Activity activity, int theme)
    {
        if(sTheme == theme){
            return;
        }
        sTheme = theme;
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
        }
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
    }
    /** Set the theme of the activity, according to the configuration. */
    public static void onActivityCreateSetTheme(Activity activity)
    {
        switch (sTheme)
        {
            default:
            case THEME_DEFAULT:
                activity.setTheme(R.style.AppTheme);
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
        return sTheme;
    }
}
