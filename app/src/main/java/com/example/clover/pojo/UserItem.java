package com.example.clover.pojo;

public class UserItem {

    private String name;
    private String email;
    private String age;
    private String pitch;
    private String speed;

    public UserItem(){
        // no arg constructor for FireStore
    }

    public UserItem(String n, String mail, String a, String p, String s){
        name = n;
        email = mail;
        age = a;
        pitch = p;
        speed = s;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public String getAge(){
        return age;
    }

    public String getPitch(){
        return pitch;
    }

    public String getSpeed(){
        return speed;
    }
}
