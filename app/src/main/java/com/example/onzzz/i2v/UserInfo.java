package com.example.onzzz.i2v;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by onzzz on 8/1/2016.
 */
public class UserInfo {

    //private data member
    private String id;
    private String name;
    private boolean authenticated = false;
    private ArrayList<String> eventIds;
    //public data member
    public static UserInfo userInfo = new UserInfo();
    public static UserInfo getInstance() { return userInfo; }

    //Constructor
    UserInfo(){
        id = null;
        name = null;
        authenticated = false;
    }

    UserInfo fromJSON(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);
        name = (obj.has("name")) ? obj.getString("name") : null;
        id = (obj.has("id")) ? obj.getString("id") : null;
        authenticated = false;
        return this;
    }

    //Set Functions
    public void setAuthenticated(boolean x){
        authenticated = x;
    }


    //Get Functions
    public boolean getAuthenticated(){
        return authenticated;
    }
    public String getId(){return id;}
    public String getName(){return name;}
    public ArrayList<String> getEventIds(){return eventIds ;}
}
