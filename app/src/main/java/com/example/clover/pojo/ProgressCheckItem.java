package com.example.clover.pojo;

import java.io.Serializable;

public class ProgressCheckItem implements Serializable {
    private String itemTitle;
    private int itemIcon;

    private static final long serialVersionUID = 1L;

    public ProgressCheckItem(String title, int icon){
        itemTitle = title;
        itemIcon = icon;
    }


    public String getItemTitle(){
        return itemTitle;
    }

    public int getItemIcon(){
        return itemIcon;
    }

    public void setItemTitle(String t){
        itemTitle = t;
    }

    public void setItemIcon(int i){
        itemIcon = i;
    }
}
