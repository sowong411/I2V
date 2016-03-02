package com.example.onzzz.i2v;

/**
 * Created by hoyuichan on 1/21/2016.
 */

import java.util.ArrayList;


public class Event {

    //private data member
    private String id;
    private String name;
    private String location;
    private String festival;

    private ArrayList<String> userIds;
    private int numberOfPhoto;

    //public data member
    public static Event event = new Event();

    public Event(String name) {
        this.name = name;
    }

    public Event  getInstance() { return event; }

    //Constructor
    Event(){
        id = null;
        name = null;
        location = null;
        userIds= null;
        numberOfPhoto = 0;
    }


    //Set Functions
    public void setLocation(String location){this.location = location;}

    //Get Functions
    public String getId(){return id;}
    public String getName(){return name;}
    public String getLocation(){return location;}
    public String getFestival(){return festival;}
    public ArrayList<String> getUserIds(){return userIds ;}
}

