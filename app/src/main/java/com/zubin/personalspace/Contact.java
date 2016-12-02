package com.zubin.personalspace;

/**
 * Created by zubin on 11/17/2016.
 */

public class Contact {
    private String uid;
    private String name;
    private String last;
    private String lastUid;

    public Contact(String muid, String mname){
        this.uid = muid;
        this.name = mname;
        this.last = "";
        this.lastUid = "";
    }
    public Contact() {}

    public String getUid(){
        return uid;
    }
    public String getName(){
        return name;
    }
    public String getLast() { return last; }
    public String getLastUid() { return lastUid; }
    public void setLast(String last) {
        this.last = last;
    }
    public void setLastUid(String uid) {
        this.lastUid = uid;
    }
}
