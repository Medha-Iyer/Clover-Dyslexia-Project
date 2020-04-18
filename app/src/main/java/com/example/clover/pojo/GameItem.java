package com.example.clover.pojo;

import java.io.Serializable;

public class GameItem implements Serializable {

    private String itemWord;
    private int itemIcon;

    private static final long serialVersionUID = 2L;

    public GameItem(){
        //no arg constructor for FireStore
    }

    public GameItem(String word, int icon){
        itemWord = word;
        itemIcon = icon;
    }

    public GameItem(String word){
        itemWord = word;
    }

    public String getItemWord(){
        return itemWord;
    }

    public int getItemIcon(){
        return itemIcon;
    }

    public void setItemIcon(int icon) { itemIcon = icon; }
}
