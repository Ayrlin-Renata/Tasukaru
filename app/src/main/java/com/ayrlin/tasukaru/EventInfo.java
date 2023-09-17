package com.ayrlin.tasukaru;

import java.sql.Timestamp;

import lombok.ToString;

@ToString
public class EventInfo {

    public Timestamp timestamp;
    public AccountInfo account;
    public int snapshotId;
    public String uptype;
    public String action;
    public int value;
    public String streamState;
    //TODO also add rson event storage 

    public EventInfo(Timestamp timestamp) {
        this.timestamp = timestamp;
        // defaults
        this.snapshotId = -1;  
        this.value = -1; 
        this.streamState = "unrecorded";
    }

    public EventInfo() {
        this(new java.sql.Timestamp(new java.util.Date().getTime()));
    }

    public EventInfo account(AccountInfo viewer) {
        this.account = viewer;
        return this;
    }

    public EventInfo snapshotId(int snapshotId) {
        this.snapshotId = snapshotId;
        return this;
    }

    public EventInfo uptype(String uptype) {
        this.uptype = uptype;
        return this;
    }

    public EventInfo action(String action) {
        this.action = action;
        return this;
    }

    public EventInfo value(int value) {
        this.value = value;
        return this;
    }

    public EventInfo streamState(String streamState) {
        this.streamState = streamState;
        return this;
    }
}
