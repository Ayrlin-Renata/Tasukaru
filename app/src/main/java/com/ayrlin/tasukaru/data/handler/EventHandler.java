package com.ayrlin.tasukaru.data.handler;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.ayrlin.sqlutil.SQLUtil;
import com.ayrlin.sqlutil.query.data.Param;
import com.ayrlin.tasukaru.Tasukaru;
import com.ayrlin.tasukaru.VBHandler;
import com.ayrlin.tasukaru.data.EventInfo;
import com.ayrlin.tasukaru.data.info.Info;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class EventHandler extends InfoObjectHandler<EventInfo> {
    @Override
    public long addToVB(EventInfo ei) {
        VBHandler vb = VBHandler.instance();
        Connection con = vb.getConnection();
        FastLogger log = Tasukaru.instance().getLogger();
        if (ei.get("timestamp") == null) {
            ei.set("timestamp", new java.sql.Timestamp(new java.util.Date().getTime()).toString());
        }
        log.debug("Adding history for event: \n" + ei);

        List<Param> params = new ArrayList<>();
        for(Info<?> i : ei.getData().values()) {
            params.add(i.getParam());
        }            

        long key = SQLUtil.insert(con, "history", params);
        if(key < 0) {
            log.severe("failed to addHistory() for event: \n" + ei);
        }
        return (int)key;
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