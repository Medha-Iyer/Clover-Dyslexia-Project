package com.example.clover;

import java.io.Serializable;

public class LibraryCardItem implements Serializable {
    private String itemTitle;
    private String itemText;

    private static final long serialVersionUID = 1L;

    public LibraryCardItem(String title, String text){
        itemTitle = title;
        itemText = text;
    }

    public String getItemTitle(){
        return itemTitle;
    }

    public String getItemText(){
        return itemText;
    }

}
