package com.zubin.personalspace;


import android.widget.TextView;

import java.util.Date;

public class ChatMessage {

    private String messageText;
    private String messageUser;
    private String recepientUid;
    private String senderUid;
    private long messageTime;
    private boolean notified;


    public ChatMessage(String messageText, String messageUser, String recepientUid, String senderUid) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.senderUid = senderUid;
        this.recepientUid = recepientUid;

        // Initialize to current time
        messageTime = new Date().getTime();
        this.notified = false;
    }

    public ChatMessage(){
    }

    public boolean getNotified() {return notified;}

    public void setNotified(boolean notified) {this.notified = notified;}

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getRecepientUid() {
        return this.recepientUid;
    }

    public String getSenderUid() {
        return this.senderUid;
    }

}