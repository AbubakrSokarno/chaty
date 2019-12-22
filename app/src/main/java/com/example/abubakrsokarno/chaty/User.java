package com.example.abubakrsokarno.chaty;

/**
 * Created by Abubakr Sokarno on 12/26/2017.
 */

public class User {
    public String name ;
    public String email ;
    public String image ;
    public String status ;
    public String key ;
    public String thumb_image ;

    public User(){

    }

    public User(String key ,String username,String email,String status,String image)
    {
        this.key = key ;
        this.name = username ;
        this.email = email ;
        this.status = status ;
        this.image = image;
    }

    public User(User input)
    {
        this.key = input.key ;
        this.name = input.name ;
        this.email = input.email ;
        this.status = input.status ;
        this.image = input.image;
    }
}
