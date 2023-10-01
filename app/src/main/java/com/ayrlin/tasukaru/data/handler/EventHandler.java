package com.ayrlin.tasukaru.data.handler;

import com.ayrlin.tasukaru.Tasukaru;
import com.ayrlin.tasukaru.data.EventInfo;

public class EventHandler extends InfoObjectHandler<EventInfo> {
    @Override
    public long addToVB(EventInfo ei) {
        if (ei.get("timestamp") == null) {
            ei.set("timestamp", new java.sql.Timestamp(new java.util.Date().getTime()).toString());
        }
        log.debug("Adding history for event: \n" + ei);

        long key = addToVBHelper("history", ei);
        return key;
    }

    @Override
    public EventInfo getFromVB(long id) {
        return getFromVBHelper("history", id, new EventInfo());
    }

    @Override
    public boolean updateToVB(EventInfo infoObject) {
        Tasukaru.instance().getLogger().warn("attempt to update EventInfo to VB! History should not be changed!");
        throw new UnsupportedOperationException("PURPOSEFULLY Unimplemented method 'updateToVB'. DO NOT UPDATE HISTORICAL EVENTS. YOU DONT KNOW WHAT THIS COULD DO TO THE GLOBAL TIMELINE EVEN IF THE BUTTERFLY EFFECT IS DEAD");
    }

}