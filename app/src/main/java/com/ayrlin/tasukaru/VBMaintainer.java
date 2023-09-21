package com.ayrlin.tasukaru;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.ayrlin.sqlutil.ActiveResult;
import com.ayrlin.sqlutil.SQLUtil;
import com.ayrlin.sqlutil.query.InsertIntoQuery;
import com.ayrlin.sqlutil.query.SelectQuery;
import com.ayrlin.sqlutil.query.data.Col;
import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.OpParam;
import com.ayrlin.sqlutil.query.data.Table;
import com.ayrlin.sqlutil.query.data.OpParam.Op;
import com.ayrlin.sqlutil.query.data.Param;
import com.ayrlin.tasukaru.data.AccountInfo;
import com.ayrlin.tasukaru.vbdefs.VBTables;

import co.casterlabs.koi.api.types.user.UserPlatform;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class VBMaintainer {
    private static VBMaintainer instance;

    private FastLogger log;
    private Connection con;

    private VBMaintainer(FastLogger log) {
        this.log = log;
        this.con = VBHandler.instance().getConnection();
    }

    /**
     * singleton pattern
     * @return THE VBMaintainer
     */
    public static VBMaintainer instance(FastLogger log) {
        if(instance == null) {
            instance = new VBMaintainer(log);
        } 
        return instance;
    }

    /**
     * Starts the maintenance cycle
     */
    public boolean begin() {
        if(!backupVB()) {
            log.severe("Could not backup VB!! aborting maintenance cycle!");
            return false;
        }
        String ver = getVersion();
        if(ver.equals("__ERROR__")) {
            log.severe("unable to determine VB version!");
            //return false;
            updateVersionNumber(); //temp 
            //TODO move existing VB, create new
        }
        if(!ver.equals(VBHandler.getVbVersion())) {
            log.warn("Mismatched versions! Database reports as version " + ver + ", plugin version is " + VBHandler.getVbVersion() + "!");
        }
        if (!updateTables()) {
            log.severe("unable to update tables!!");
            return false;
        }
        updatePlatforms();
        fillAccountTableHoles();
        return true;
    }

    public boolean backupVB() {
        //TODO Backups
        return true; //pass check for now temp
    }

    public String getVersion() {
        log.trace("getVersion()");
        log.trace("getVersion():" + VBTables.meta.name);
        if(!SQLUtil.existsCheck(con, VBTables.meta.name)) {
            log.debug("no meta table found, creating new table based on: \n" + VBTables.meta);
            if(!VBTables.meta.create()) {
                log.severe("Could not create meta table! Version check failed!");
                return "__ERROR__";
            }
        }
        
        //check version
        List<OpParam> match = SQLUtil.qol(DataType.STRING, "property", Op.EQUAL, "version");
        ActiveResult ar = new SelectQuery()
                .select("value")
                .from(VBTables.meta.name)
                .where(match)
                .execute(con);

        String readVersion = "ERROR";
        try {
            if(!ar.rs.next()) {
                log.warn("malformed meta table has no version!");
                return "__ERROR__";
            } else {
                readVersion = ar.rs.getString("value"); 
            }
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e, "exception while checking VB version");
        }
        log.debug("VERSION CHECK: Database reports as version " + readVersion);
        return readVersion;
    }

    /**
     * checks for current tables and updates them if necessary
     * @return
     */
    public boolean updateTables() {
        log.trace("updating tables");
        List<Field> fs = Arrays.asList(VBTables.class.getDeclaredFields());
        for(Field f : fs) {
            //f.setAccessible(true);
            Table t = null;
            try {
                t = (Table) f.get(null);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.severe("cannot retrieve table definitions");
                e.printStackTrace();
            }
            if(t == null) {
                log.severe("there was a problem retrieving a table definition!");
                return false;
            }
            
            if(!SQLUtil.existsCheck(con, t.name)) {
                log.debug("cound not find table "+ t.name + "attempting to create from definition: \n" + t);
                if(!t.create()) {
                    log.severe("Could not create " + t.name + " table! Table update failed!");
                    return false;
                }
            }
            updateCols(t);
        }
        updateVersionNumber();
        return true;
    }

    
    private void updateCols(Table t) {
        List<String> actualCols = SQLUtil.retrieveColumnNames(con, t.name).stream()
        .map(col -> col.column)
        .collect(Collectors.toList());
        List<String> defCols = t.getColDefs().stream()
        .map(col -> col.column)
        .collect(Collectors.toList());
        
        if(actualCols.containsAll(defCols)) {
            log.debug("all columns up to date in table " + t.name);
            return;
        } else {
            for (Col def : t.getColDefs()) {
                if(actualCols.contains(def.column)) continue;
                log.debug("table " + t.name + " found to not contain column " + def.column + ", altering into table.");
                def.alterInto(t);
            }
        }
    } 

    /**
     * 
     * @return true if update actions were taken, false if already current
     */
    private boolean updateVersionNumber() {
        if(getVersion().equals(VBHandler.getVbVersion())) return false;
        log.trace("updating version number to " + VBHandler.getVbVersion());
        List<Param> version = new ArrayList<>();
        version.add(new Param(DataType.STRING, "property", "version"));
        version.add(new Param(DataType.STRING, "value", VBHandler.getVbVersion()));
        new InsertIntoQuery().insertInto("meta").values(version).execute(con);
        return true;
    }

    public void updatePlatforms() {
        log.trace("Updating platforms for viewers table.");
        List<String> cols = new ArrayList<>();
        for (UserPlatform plat : UserPlatform.values()) {
            if(plat == UserPlatform.CASTERLABS_SYSTEM || plat == UserPlatform.CUSTOM_INTEGRATION) {
                continue; //these are for system messages and integrations like Ko-Fi
            }
            cols.add(plat.name());
        }
        log.debug("Found supported CL platforms: \n" + cols);
        VBHandler.instance().checkAddCols(log, cols);
    }

    public void fillAccountTableHoles() {
        VBHandler vb = VBHandler.instance();
        //find holes
        List<Long> aids = vb.listAllAccounts();
        for (long aid : aids) {
            log.trace("filling holes for viewer " + aid);
            AccountInfo vi = vb.retrieveCurrentAccountInfo(aid);
            List<Param> missingList = vi.listUnfilledValues();
            if(missingList.isEmpty()) continue;
            for (Param p : missingList) {
                Param sp = vb.findLastSnapshotValue(vi.id, p);
                if(sp.value != null) {
                    vi.modifyFromParameter(sp);
                }
            }
            vb.updateAccount(vi);
        }
        log.debug("hole filling complete.");
    }
}
