package com.example.clover.pojo;

import com.example.clover.R;

import java.io.Serializable;

public class GameItem implements Serializable {

    private String itemWord;
    private int itemIcon;

    private static final long serialVersionUID = 2L;

    public GameItem(String word, int icon){
        itemWord = word;
        itemIcon = icon;
    }

    public GameItem(String word){
        itemWord = word;
    }

    public GameItem(){
    }

    public String getItemWord(){
        return itemWord;
}

    public int getItemIcon(){
        return itemIcon;
    }

    public void setItemIcon(int icon) {
        itemIcon = icon;
        fixIcons();
    }

    public void fixIcons(){
        if (itemIcon == R.drawable.check-1 || itemIcon==R.drawable.cross-1){
            itemIcon++;
        }
    }
}
