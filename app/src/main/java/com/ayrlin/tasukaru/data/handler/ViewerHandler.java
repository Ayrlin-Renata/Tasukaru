package com.ayrlin.tasukaru.data.handler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ayrlin.sqlutil.ActiveResult;
import com.ayrlin.sqlutil.SQLUtil;
import com.ayrlin.sqlutil.query.InsertIntoQuery;
import com.ayrlin.sqlutil.query.SelectQuery;
import com.ayrlin.sqlutil.query.UpdateQuery;
import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.Param;
import com.ayrlin.sqlutil.query.data.OpParam.Op;
import com.ayrlin.tasukaru.TLogic;
import com.ayrlin.tasukaru.Tasukaru;
import com.ayrlin.tasukaru.VBHandler;
import com.ayrlin.tasukaru.data.AccountInfo;
import com.ayrlin.tasukaru.data.ViewerInfo;
import com.ayrlin.tasukaru.data.info.Info;

import co.casterlabs.koi.api.types.user.UserPlatform;
import lombok.NoArgsConstructor;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@NoArgsConstructor
public class ViewerHandler extends InfoObjectHandler<ViewerInfo> {

    @Override
    public long addToVB(ViewerInfo vi) {
        VBHandler vb = VBHandler.instance();
        Connection con = vb.getConnection();
        FastLogger log = Tasukaru.instance().getLogger();
        log.trace("Adding viewer: \n" + vi);

        List<Param> params = new ArrayList<>();
        for(Info<?> i : vi.getData().values()) {
            params.add(i.getParam());
        }
        
        long key = new InsertIntoQuery().insertInto("viewers").values(params).execute(con);
        log.debug("added viewer: \n" + vi);
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
        VBHandler vb = VBHandler.instance();
        Connection con = vb.getConnection();
        FastLogger log = Tasukaru.instance().getLogger();
        log.trace("retrieving viewer with id: " + vid);
        ActiveResult ar = new SelectQuery()
                .select("*")
                .from("viewers")
                .where(SQLUtil.qol(DataType.STRING, "id", Op.EQUAL, vid))
                .execute(con);
        ViewerInfo vi = new ViewerInfo();
        try {
            if(ar.rs.next()) {
                vi.set("id",ar.rs.getLong("id"));
                vi.set("clId", ar.rs.getString("clId"));
                vi.set("clName", ar.rs.getString("clName"));
                vi.set("fallbackName", ar.rs.getString("fallbackName"));
                vi.set("watchtime", ar.rs.getLong("watchtime"));
                vi.set("points", ar.rs.getLong("points"));
                List<Long> aids = new ArrayList<>();
                for(UserPlatform plat : TLogic.instance().getSupportedPlatforms()) {
                    aids.add(ar.rs.getLong(plat.name()));
                }
            }
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e, "exception while retrieving viewer for account with id " + vid);
            return null;
        } finally {
            ar.close();
        }
        if((Long) vi.get("id") < 0) {
            log.warn("unable to retrieve viewer with id " + vid);
            return null;
        }
        log.debug("retrieved viewer: \n" + vi);
        return vi;
    }

    @Override
    public boolean updateToVB(ViewerInfo vi) {
        VBHandler vb = VBHandler.instance();
        Connection con = vb.getConnection();
        FastLogger log = Tasukaru.instance().getLogger();
        log.trace("updating viewer: \n" + vi);

        List<Param> params = new ArrayList<>();
        params.add(new Param(DataType.STRING, "clId", vi.get("clId")));
        params.add(new Param(DataType.STRING, "clName", vi.get("clName")));
        params.add(new Param(DataType.STRING, "fallbackName", vi.get("fallbackName")));
        params.add(new Param(DataType.INT, "watchtime", vi.get("watchtime")));
        params.add(new Param(DataType.INT, "points", vi.get("points")));
        for(Long aid : vi.getAccountIds()) {
            params.add(new Param(DataType.INT, vb.getAccountPlatform(aid).name(), aid));
        }

        boolean result = new UpdateQuery()
                .update("viewers")
                .set(params)
                .where(SQLUtil.qol(DataType.INT, "id", Op.EQUAL, vi.get("id")))
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
}