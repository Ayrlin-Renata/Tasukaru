package com.ayrlin.tasukaru.data;

import java.sql.Timestamp;

import co.casterlabs.koi.api.types.events.KoiEvent;
import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
public class EventInfo {

    public Timestamp timestamp;
    public AccountInfo account;
    public ViewerInfo viewer;
    public KoiEvent event;
    public int snapshotId;
    public UpType uptype;
    public Action action;
    public long value;
    public String streamState;
    public boolean processed;

    @AllArgsConstructor
    public enum UpType {
        PRESENT("present"),
        ABSENT("absent"),
        TECHNICAL("technical");

        String str;

        @Override
        public String toString() {
            return str;
        }
    }

    public interface Action {
        // :3
    }

    /**
     * Present Action
     */
    @AllArgsConstructor
    public enum PAct implements Action {
        MESSAGE("message"),
        FOLLOW("follow"),
        SUBSCRIBE("subscribe"),
        DONATE("donate"),
        JOIN("join");

        String str;

        @Override
        public String toString() {
            return str;
        }
    }

    /**
     * Absent Action
     */
    @AllArgsConstructor
    public enum AAct implements Action {
        LEAVE("leave");

        String str;

        @Override
        public String toString() {
            return str;
        }
    }

    /**
     * Technical Action
     */
    @AllArgsConstructor
    public enum TAct implements Action {
        SNAPSHOT("snapshot"),
        POINTS("points");

        String str;

        @Override
        public String toString() {
            return str;
        }
    }

    public EventInfo(Timestamp timestamp) {
        this.timestamp = timestamp;
        // defaults
        this.event = null;
        this.snapshotId = -1;  
        this.value = -1; 
        this.streamState = "unrecorded";
        this.processed = false;
    }

    public EventInfo() {
        this(new java.sql.Timestamp(new java.util.Date().getTime()));
    }

    public EventInfo account(AccountInfo account) {
        this.account = account;
        return this;
    }

    public EventInfo viewer(ViewerInfo viewer) {
        this.viewer = viewer;
        return this;
    }

    public EventInfo event(KoiEvent event) {
        this.event = event;
        return this;
    }

    public EventInfo snapshotId(int snapshotId) {
        this.snapshotId = snapshotId;
        return this;
    }

    public EventInfo uptype(UpType uptype) {
        this.uptype = uptype;
        return this;
    }

    public EventInfo action(Action action) {
        this.action = action;
        return this;
    }

    public EventInfo value(long value) {
        this.value = value;
        return this;
    }

    public EventInfo streamState(String streamState) {
        this.streamState = streamState;
        return this;
    }

    public EventInfo processed() {
        processed = true;
        return this;
    }
}
