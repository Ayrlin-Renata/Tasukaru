package com.ayrlin.tasukaru;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.ayrlin.sqlutil.*;
import com.ayrlin.sqlutil.query.*;
import com.ayrlin.sqlutil.query.data.*;
import com.ayrlin.sqlutil.query.data.OpParam.Op;
import com.ayrlin.tasukaru.data.AccountInfo;
import com.ayrlin.tasukaru.vbdefs.VBTables;

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
        if(!backupVB()) { // TODO exists check
            log.severe("Could not backup VB!! aborting maintenance cycle!");
            return false;
        }
        String ver = getVersion();
        if(ver.equals("__ERROR__")) {
            log.severe("unable to determine VB version!");
            //return false;
            updateVersionNumber(); //temp 
            //TODO restore from backup, if fail create new
            //return false;
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
        log.trace("backing up VB...");
        //String errMsg = "Plugin Tasukaru unable to backup Viewerbase. Don't worry about it, except if this keeps happenning consistantly, then please contact ayrlin on discord to get it fixed. You can find the specific error by searching for the most recent `unable to backup VB!` in `app.log`.";

        String dateString = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH_mm_ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());

        String dirString = VBHandler.instance().getTskrDir() + "VBBackups";
        String vbname = VBHandler.getVbFilename().substring(0, VBHandler.getVbFilename().length()-3);
        Path dirPath = Paths.get(dirString);
        
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                log.warn("unable to backup VB!"); 
                e.printStackTrace();
                //TODO Caffeinated.notify(errMsg);
                return false;
            }
        }
        
        //make sure file is unique
        String backupString = VBHandler.instance().getTskrDir() + "VBBackups\\" + vbname + "." + dateString;
        long index = 0;
        String ts = backupString + ".bak.db";
        Path tp = Paths.get(ts);
        while (Files.exists(tp)) {
            ts = backupString + "." + ++index + ".bak.db";
            tp = Paths.get(ts);
        }

        //copy file
        Path backupPath = tp;
        String origString = VBHandler.instance().getTskrDir() + VBHandler.getVbFilename();
        Path origPath = Paths.get(origString);
        boolean retry = true;
        try {
            Files.copy(origPath, backupPath, StandardCopyOption.ATOMIC_MOVE);
            retry = false;
        } catch (IOException | UnsupportedOperationException e) {
            log.debug("unable to complete atomic copy for VB backup!"); 
            e.printStackTrace();
        }
        try {
            if(retry) Files.copy(origPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e1) {
            log.severe("unable to complete copy for VB backup!"); 
            e1.printStackTrace();
            //Caffeinated.notify(errMsg);
            return false;
        }

        //TODO backup max size and cull backups
        log.debug("backed up! Copied " + origString + " to " + backupString);
        return true; 
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
        List<String> cols = TLogic.instance().getSupportedPlatforms();
        VBHandler.instance().checkAddCols(log, cols);
    }

    public void fillAccountTableHoles() {
        VBHandler vb = VBHandler.instance();
        //find holes
        List<Long> aids = vb.listAllAccounts();
        for (long aid : aids) {
            log.trace("\nfilling holes for account " + aid);
            AccountInfo vi = vb.retrieveCurrentAccountInfo(aid);
            AccountInfo origVi = new AccountInfo(vi);
            List<Param> missingList = vi.listUnfilledValues();
            if(missingList.isEmpty()) continue;
            for (Param p : missingList) {
                Param sp = vb.findLastSnapshotValue(vi.id, p);
                if(sp.value != null) {
                    vi.modifyFromParameter(sp);
                }
            }
            if(!vi.equals(origVi)) {
                log.debug("account info was found to need updating: \nOLD: \n" + origVi + "\nNEW: \n" + vi);
                vb.updateAccount(vi);
            } else {
                log.trace("account info was found to NOT need updating: \nOLD: \n" + origVi + "\nNEW: \n" + vi);
            }
        }
        log.debug("hole filling complete.");
    }
}
