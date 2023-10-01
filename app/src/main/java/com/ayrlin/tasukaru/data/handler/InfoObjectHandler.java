package com.ayrlin.tasukaru.data.handler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ayrlin.sqlutil.ActiveResult;
import com.ayrlin.sqlutil.SQLUtil;
import com.ayrlin.sqlutil.query.InsertIntoQuery;
import com.ayrlin.sqlutil.query.SelectQuery;
import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.OpParam.Op;
import com.ayrlin.sqlutil.query.data.Param;
import com.ayrlin.tasukaru.Tasukaru;
import com.ayrlin.tasukaru.VBHandler;
import com.ayrlin.tasukaru.data.InfoObject;
import com.ayrlin.tasukaru.data.info.Info;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public abstract class InfoObjectHandler<T extends InfoObject<T>> {

    protected FastLogger log;
    protected VBHandler vb;
    protected Connection con;

    public InfoObjectHandler() {
        log = Tasukaru.instance().getLogger();
        vb = VBHandler.instance();
        con = vb.getConnection();
    }

    /**
     * adds infoObject to Viewerbase via SQL
     * @param infoObject
     * @return key id of new infoObject in the relevent table.
     */
    public abstract long addToVB(T infoObject);

    public abstract T getFromVB(long id);

    public abstract boolean updateToVB(T infoObject);

    public List<T> getFromVB(List<Long> ids) {
        List<T> ts = new ArrayList<>();
        for(long id : ids) {
            ts.add(this.getFromVB(id));
        }
        return ts;
    }

    protected long addToVBHelper(String table, T info) {
        log.trace("Adding to " + table + " table: \n" + info);

        List<Param> params = new ArrayList<>();
        for(Info<?> i : info.getData().values()) {
            if(i.getName().equals("id") && i.atDefault()) {
                continue;
            }
            params.add(i.getParam());
        }
        long key = new InsertIntoQuery().insertInto(table).values(params).execute(con);
        
        log.debug("added to table " + table + ": \n" + info);

        if(key < 0) {
            log.severe("failed to add to " + table + " table: \n" + info);
            return -1;
        } 
        return key;
    }

    protected T getFromVBHelper(String table, long id, T info) {
        VBHandler vb = VBHandler.instance();
        Connection con = vb.getConnection();
        FastLogger log = Tasukaru.instance().getLogger();
        log.trace("retrieving current info from " + table + " table.");

        ActiveResult ar = new SelectQuery()
                .select("*")
                .from(table)
                .where(SQLUtil.qol(DataType.INT, "id", Op.EQUAL, id))
                .execute(con);
        try {
            if (!ar.rs.next()) {
                log.warn("unable to find info from " + table + " table, with id: \n" + id);
                return null;
            }
            for(Info<?> i : info.getData().values()) {
                log.trace("assigning object: \n" + i);
                i.assign(ar.rs);
            }
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e,"SQLException while retrieving info from " + table + " table, with id: \n" + id);
            return null;
        } finally { ar.close(); }

        if((Long) info.get("id") < 0) {
            log.warn("unable to retrieve info from " + table + " table, with id " + info);
            return null;
        }
        log.debug("retrieved current info from " + table + " table: \n" + info);
        return info;
    }
}
