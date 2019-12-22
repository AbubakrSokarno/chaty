package com.example.abubakrsokarno.chaty;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Abubakr Sokarno on 1/20/2018.
 */

public class Messages {
    private String message,type , from ;
    private boolean seen ;
    private long timestamp ;
    private FirebaseAuth mAuth ;
    private int its_me ;

    public Messages()
    {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser().getUid().equals(from))
        {
            this.its_me = 1 ;
        }
        else{
            this.its_me = 2 ;
        }
    }

    public Messages(String type,boolean seen, long timestamp, String from,String message) {
        this.seen = seen;
        this.type = type ;
        this.message = message ;
        this.timestamp = timestamp;
        this.from = from;
        setItsMe(from);
    }

    public int getItsMe(String from)
    {
        setItsMe(from);
        return  this.its_me ;
    }

    public void setItsMe(String userID)
    {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser().getUid().equals(userID))
        {
            this.its_me = 1 ;
        }
        else{
            this.its_me = 2 ;
        }
    }

    public String getMessage() {return message;}

    public void setMessage(String message) {this.message = message;}

    public String getFrom() {return from;}

    public void setFrom(String from) {this.from = from;}

    public String getType() {return type;}

    public void setType(String type) {this.type = type;}

    public long getTimestamp() {return timestamp;}

    public void setTimestamp(long timestamp) {this.timestamp = timestamp;}

    public boolean getSeen() {return seen;}

    public void setSeen(boolean seen) {this.seen = seen;}
}
