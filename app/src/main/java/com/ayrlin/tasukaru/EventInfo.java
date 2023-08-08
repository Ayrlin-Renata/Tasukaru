package com.ayrlin.tasukaru;

import java.sql.Timestamp;

public class EventInfo {

    public Timestamp timestamp;
    public ViewerInfo viewer;
    public String uptype;
    public String action;
    public int value;

    public EventInfo(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public EventInfo() {
        this(new java.sql.Timestamp(new java.util.Date().getTime()));
    }

    public EventInfo viewer(ViewerInfo viewer) {
        this.viewer = viewer;
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
}
