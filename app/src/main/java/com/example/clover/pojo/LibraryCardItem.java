package com.example.clover.pojo;

import java.io.Serializable;

public class LibraryCardItem implements Serializable, Comparable <LibraryCardItem> {
    private String itemTitle;
    private String itemText;
    private int id;
    private boolean state; //true is library, false is archive
    private int position;

    private static final long serialVersionUID = 1L;

    public LibraryCardItem(String title, String text){
        itemTitle = title;
        itemText = text;
        state = true;
        position = 0;
    }

    public LibraryCardItem(){

    }

    public void setId(int ids){
        id = ids;
    }

    public String getItemTitle(){
        return itemTitle;
    }

    public String getItemText(){
        return itemText;
    }

    public int getId() { return id; }

    public void switchState() { state = !state; }

    public void setState(boolean newS) { state = newS; }


    public boolean getState() { return state; }

    public int getPosition() { return position; }

    public void setPosition(int i) { position = i; }

    @Override
    public int compareTo(LibraryCardItem o) {
        int comparePosition = ((LibraryCardItem)o).getPosition();
        return this.getPosition()-comparePosition;
    }
}
