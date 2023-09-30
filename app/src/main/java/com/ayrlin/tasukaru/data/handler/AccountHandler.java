package com.ayrlin.tasukaru.data.handler;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.ayrlin.sqlutil.SQLUtil;
import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.OpParam;
import com.ayrlin.sqlutil.query.data.Param;
import com.ayrlin.sqlutil.query.data.OpParam.Op;
import com.ayrlin.tasukaru.Tasukaru;
import com.ayrlin.tasukaru.VBHandler;
import com.ayrlin.tasukaru.data.AccountInfo;
import com.ayrlin.tasukaru.data.EventInfo;
import com.ayrlin.tasukaru.data.EventInfo.TAct;
import com.ayrlin.tasukaru.data.EventInfo.UpType;
import com.ayrlin.tasukaru.data.info.Info;

import lombok.NoArgsConstructor;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@NoArgsConstructor
public class AccountHandler extends InfoObjectHandler<AccountInfo> {

    @Override
    public long addToVB(AccountInfo ai) {
        VBHandler vb = VBHandler.instance();
        Connection con = vb.getConnection();
        FastLogger log = Tasukaru.instance().getLogger();
        log.debug("Adding Account: \n" + ai);

        List<Param> params = new ArrayList<>();
        for(Info<?> i : ai.getData().values()) {
            params.add(i.getParam());
        }

        long key = SQLUtil.insert(con, "accounts", params);
        if(key < 0) {
            log.severe("failed to add Account: \n" + ai);
            return -1;
        } 

        //add snapshot after Account for foreign key
        ai.set("id", key);
        log.trace("added Account id: " + ai.get("id"));

        if(!updateToVB(ai)) { //will also handle snapshot
            log.severe("failed to update Account latestsnapshot while adding Account: \n" + ai);
            return -2;
        }

        return (long) ai.get("id");
    }

    @Override
    public AccountInfo getFromVB(long id) {
        return getFromVBHelper("accounts", id, new AccountInfo());
    }

    @Override
    public boolean updateToVB(AccountInfo ai) {
        VBHandler vb = VBHandler.instance();
        Connection con = vb.getConnection();
        FastLogger log = Tasukaru.instance().getLogger();
        log.debug("Updating Account info: " + ai);
        
        addSnapshot(ai);
        log.trace("Inserting new latestsnapshot into update: " + ai.get("latestsnapshot"));
        
        List<Param> setParams = new ArrayList<>();
        for(Info<?> i : ai.getData().values()) {
            setParams.add(i.getParam());
        }
        
        List<OpParam> whereParams = new ArrayList<>();
        whereParams.add(new OpParam(DataType.INT, "id", Op.EQUAL, ai.get("id")));
        
        if(!SQLUtil.update(con, "accounts", setParams, whereParams)) {
            log.severe("failed to update Account: \n" + ai);
            return false;
        } 
        return true;
    }

        /**
     * Creates a latestsnapshot and adds it to vi
     * @param ai
     * @return the snapshot id, equal to vi.latestsnapshot
     */
    public long addSnapshot(AccountInfo ai) {
        VBHandler vb = VBHandler.instance();
        Connection con = vb.getConnection();
        FastLogger log = Tasukaru.instance().getLogger();
        log.debug("Adding account snapshot for Account: \n" + ai);

        if ((long) ai.get("id") <= 0) {
            long aid = vb.findAccountId(ai);
            if (aid < 0) {
                log.severe("Failed to add account snapshot due to lack of id for Account: \n" + ai);
                return -1;
            } else {
                ai.set("id", aid);
            }
        }

        List<Param> params = new ArrayList<>();
        for(Info<?> i : ai.getData().values()) {
            if(!i.getName().equals("latestsnapshot")) { //exclude
                Param toAdd = i.getParam();
                if(i.getName().equals("id")) {
                    toAdd.column = "aid";
                }
                params.add(toAdd);
            }
        }

        long key = SQLUtil.insert(con, "snapshots", params);
        if(key < 0) {
            log.severe("Failed to add account snapshot for Account: \n" + ai);
            return -1;
        }

        ai.set("latestsnapshot", key);

        EventInfo ei = new EventInfo()
                .setAccount(ai)
                .set("sid", (long) ai.get("latestsnapshot"))
                .set("uptype", UpType.TECHNICAL.toString())
                .set("action", TAct.SNAPSHOT.toString());
        vb.getEventHandler().addToVB(ei);

        return (long) ai.get("latestsnapshot");
    }

    public boolean isCurrent(AccountInfo ai) {
        FastLogger log = Tasukaru.instance().getLogger();
        log.trace("verifying current Account info for Account: \n" + ai);
        AccountInfo ci = getFromVB((long) ai.get("id"));
        boolean result = ai.notContradictory(ci);
        log.debug("Account info is " + (result? "" : "not") + " current for Account: \n" + ai + " with current info: \n" + ci);
        return result;
    }
}