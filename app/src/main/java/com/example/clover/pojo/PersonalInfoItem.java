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

    public PersonalInfoItem(){
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

    public void setItemTitle(String t){
        itemTitle = t;
    }

    public void setItemText(String t){
        itemText = t;
    }

    public void setItemIcon(int i){
        itemIcon = i;
    }

}
