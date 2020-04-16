package com.example.clover.pojo;

import android.os.Parcelable;

import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;

public class GameItem implements Serializable {

    private String itemWord;
    private int itemIcon;

    private static final long serialVersionUID = 2L;

    public GameItem(String word, int icon){
        itemWord = word;
        itemIcon = icon;
    }

    public String getItemWord(){
        return itemWord;
    }

    public int getItemIcon(){
        return itemIcon;
    }

//    public GameItem(Parcel source) {
//        itemWord = source.readString();
//        itemIcon = source.readInt();
//    }
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(itemWord);
//        dest.writeInt(itemIcon);
//    }
//
//    public String getItemWord(){
//        return itemWord;
//    }
//
//    public int getItemIcon(){
//        return itemIcon;
//    }
//
//    public static final Creator<GameItem> CREATOR = new Creator<GameItem>() {
//        @Override
//        public GameItem[] newArray(int size) {
//            return new GameItem[size];
//        }
//
//        @Override
//        public GameItem createFromParcel(Parcel source) {
//            return new GameItem(source);
//        }
//    };
}

