package com.example.clover.pojo;

import java.io.Serializable;

public class GameItem implements Serializable {

    private String itemWord;
    private int itemIcon;

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
}
