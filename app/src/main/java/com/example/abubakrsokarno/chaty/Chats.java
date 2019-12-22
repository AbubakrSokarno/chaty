package com.example.abubakrsokarno.chaty;

/**
 * Created by Abubakr Sokarno on 1/30/2018.
 */

public class Chats {
    private boolean seen ;
    private long timestamp ;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
