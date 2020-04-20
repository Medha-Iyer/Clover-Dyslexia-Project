package com.example.clover.pojo;

import java.io.Serializable;

public class PersonalInfoItem implements Serializable {
    private String itemTitle;
    private String itemText;
    private int itemIcon;

    private static final long serialVersionUID = 1L;

    public PersonalInfoItem(String title, String text, int icon){
        itemTitle = title;
        itemText = text;
        itemIcon = icon;
    }


    public String getItemTitle(){
        return itemTitle;
    }

    public String getItemText(){
        return itemText;
    }

    public int getItemIcon(){
        return itemIcon;
    }

}
