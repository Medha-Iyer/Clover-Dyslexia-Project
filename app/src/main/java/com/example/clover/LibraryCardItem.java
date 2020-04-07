package com.example.clover;

import java.io.Serializable;

public class LibraryCardItem implements Serializable {
    private String itemTitle;
    private String itemText;
    private String imageString;

    private static final long serialVersionUID = 1L;

    public LibraryCardItem(String title, String text, String bitstring){
        itemTitle = title;
        itemText = text;
        imageString = bitstring;
    }

    public String getItemTitle(){
        return itemTitle;
    }

    public String getItemText(){
        return itemText;
    }

    public String getImageString() { return imageString; }
}
