package com.ayrlin.tasukaru.data.handler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ayrlin.sqlutil.ActiveResult;
import com.ayrlin.sqlutil.SQLUtil;
import com.ayrlin.sqlutil.query.SelectQuery;
import com.ayrlin.sqlutil.query.UpdateQuery;
import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.OpParam.Op;
import com.ayrlin.sqlutil.query.data.Param;
import com.ayrlin.tasukaru.data.AccountInfo;
import com.ayrlin.tasukaru.data.EventInfo;
import com.ayrlin.tasukaru.data.EventInfo.Origin;
import com.ayrlin.tasukaru.data.EventInfo.TAct;
import com.ayrlin.tasukaru.data.EventInfo.UpType;
import com.ayrlin.tasukaru.data.ViewerInfo;

public class ViewerHandler extends InfoObjectHandler<ViewerInfo> {

    @Override
    public long addToVB(ViewerInfo vi) {
        long key = addToVBHelper("viewers", vi);
        if(key < 0) return -1;

        log.trace("updating viewer accounts: \n" + vi.getAccountIds());
        AccountHandler aih = vb.getAccountHandler();
        for(AccountInfo ai : aih.getFromVB(vi.getAccountIds())) {
            ai.set("vid", key);
            aih.updateToVB(ai);
        }
        return key;
    }

    @Override
    public ViewerInfo getFromVB(long vid) {
        return getFromVBHelper("viewers", vid, new ViewerInfo());

        // VBHandler vb = VBHandler.instance();
        // Connection con = vb.getConnection();
        // FastLogger log = Tasukaru.instance().getLogger();
        // log.trace("retrieving viewer with id: " + vid);

        // ActiveResult ar = new SelectQuery()
        //         .select("*")
        //         .from("viewers")
        //         .where(SQLUtil.qol(DataType.STRING, "id", Op.EQUAL, vid))
        //         .execute(con);
        // ViewerInfo vi = new ViewerInfo();
        // try {
        //     // if(ar.rs.next()) {
        //     //     vi.set("id",ar.rs.getLong("id"));
        //     //     vi.set("clid", ar.rs.getString("clid"));
        //     //     vi.set("clname", ar.rs.getString("clname"));
        //     //     vi.set("fallbackname", ar.rs.getString("fallbackname"));
        //     //     vi.set("watchtime", ar.rs.getLong("watchtime"));
        //     //     vi.set("points", ar.rs.getLong("points"));
        //     //     List<Long> aids = new ArrayList<>();
        //     //     for(UserPlatform plat : TLogic.instance().getSupportedPlatforms()) {
        //     //         aids.add(ar.rs.getLong(plat.name().toLowerCase()));
        //     //     }
        //     // }
        //     if (!ar.rs.next()) {
        //         log.warn("unable to find Account with id: \n" + id);
        //         return null;
        //     }
        //     for(Info<?> i : vi.getData().values()) {
        //         log.trace("assigning object: \n" + i);
        //         i.assign(ar.rs);
        //     }
        // } catch (SQLException e) {
        //     SQLUtil.SQLExHandle(e, "exception while retrieving viewer for account with id " + vid);
        //     return null;
        // } finally { ar.close(); }

        // if((Long) vi.get("id") < 0) {
        //     log.warn("unable to retrieve viewer with id " + vid);
        //     return null;
        // }
        // log.debug("retrieved viewer: \n" + vi);
        // return vi;
    }

    @Override
    public boolean updateToVB(ViewerInfo vi) {
        log.trace("updating viewer: \n" + vi);

        List<Param> params = new ArrayList<>();
        params.add(new Param(DataType.STRING, "clid", vi.get("clid")));
        params.add(new Param(DataType.STRING, "clname", vi.get("clname")));
        params.add(new Param(DataType.STRING, "fallbackname", vi.get("fallbackname")));
        params.add(new Param(DataType.LONG, "watchtime", vi.get("watchtime")));
        params.add(new Param(DataType.LONG, "points", vi.get("points")));
        for(Long aid : vi.getAccountIds()) {
            params.add(new Param(DataType.LONG, vb.getAccountHandler().getAccountPlatform(aid).name(), aid));
        }

        boolean result = new UpdateQuery()
                .update("viewers")
                .set(params)
                .where(SQLUtil.qol(DataType.LONG, "id", Op.EQUAL, vi.get("id")))
                .execute(con);

        if(result) {
            log.debug("updated viewer: \n" + vi);
        } else {
            log.warn("unable to update viewer: \n" + vi);
        }
        return result;
    }

    public long addViewer(AccountInfo ai) {
        Map<String, Long> accounts = new HashMap<>();
        accounts.put((String) ai.get("platform"), (long) ai.get("id"));
        ViewerInfo vi = new ViewerInfo()
                .set("fallbackName", ai.get("displayname"))
                .set("points", 0)
                .set("watchtime", 0)
                .setMultiple(accounts);
        long key = addToVB(vi);
        ai.set("vid", key);
        return key;
    }

    /**
     * gets viewer based on account
     * @param ai account assumed filled
     * @return ViewerInfo
     */
    public ViewerInfo findViewer(AccountInfo ai) {
        ActiveResult ar = new SelectQuery()
                .select("id")
                .from("viewers")
                .where(SQLUtil.qol(DataType.LONG, ai.get("platform").toString(), Op.EQUAL, (long) ai.get("id")))
                .execute(con);
        long vid = -1; 
        try {
            if(ar.rs.next()) {
                vid = ar.rs.getLong("id");
            }
        } catch(SQLException e) {
            SQLUtil.SQLExHandle(e, "exception while finding viewer for account: \n" + ai);
        } finally {
            ar.close();
        }
        if(vid < 0) {
            log.warn("unable to find viewer for account: \n" + ai);
            return null;
        }
        return getFromVB(vid);
    }

    public void addPoints(EventInfo ei, Long points, Origin origin) {
        log.debug("adding " + points + " points to viewer: " + ei.getViewer().getName());
        
        ei.getViewer().set("points", (Long) ei.getViewer().get("points") + points);
        
        updateToVB(ei.getViewer());
        vb.eventHandler.addToVB(new EventInfo()
                .set("aid", ei.getAccount().get("id"))
                .set("sid", ei.get("sid"))
                .set("uptype", UpType.TECHNICAL.toString())
                .set("action", TAct.POINTS.toString())
                .set("value", points)
                .set("origin", origin.toString())
                .set("processed", true)
                .set("streamState", ei.get("streamState")));
    }
}