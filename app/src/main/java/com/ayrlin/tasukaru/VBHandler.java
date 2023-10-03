package com.ayrlin.tasukaru;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ayrlin.sqlutil.ActiveResult;
import com.ayrlin.sqlutil.SQLUtil;
import com.ayrlin.sqlutil.query.AlterTableQuery;
import com.ayrlin.sqlutil.query.SelectQuery;
import com.ayrlin.sqlutil.query.data.Col;
import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.OpParam;
import com.ayrlin.sqlutil.query.data.OpParamList;
import com.ayrlin.sqlutil.query.data.OpParam.Op;
import com.ayrlin.sqlutil.query.data.OpParamList.Cnj;
import com.ayrlin.sqlutil.query.data.Param;
import com.ayrlin.tasukaru.data.EventInfo;
import com.ayrlin.tasukaru.data.handler.AccountHandler;
import com.ayrlin.tasukaru.data.handler.EventHandler;
import com.ayrlin.tasukaru.data.handler.ViewerHandler;
import com.ayrlin.tasukaru.data.ViewerInfo;
import com.ayrlin.tasukaru.data.info.LongInfo;
import com.ayrlin.tasukaru.data.info.StringInfo;

import lombok.Getter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;


public class VBHandler {
    private static final @Getter String vbVersion = "1.0.0";
    private static final @Getter String vbFilename = "ViewerBase.db";
    
    public Connection con;
    public FastLogger log;
    private @Getter String tskrDir;
    @Getter
    public ViewerHandler viewerHandler;
    private @Getter AccountHandler accountHandler;
    @Getter
    public EventHandler eventHandler;

    public VBHandler() {
        this.log = Tasukaru.instance().getLogger();
        log.trace("constructing VBHandler!");
        this.tskrDir = initPluginDir("tasukaru");
    }

    /**
     * singleton pattern
     * @return THE VBHandler
     */
    public static VBHandler instance() {
        Tasukaru tskr = Tasukaru.instance();
        if(tskr.getVbHandler() == null) {
            Tasukaru.instance().getLogger().severe("VBHandler instance is null!");
        } 
        return tskr.getVbHandler();
    }

    public void begin() {
        log.trace("VBHandler begin()");
        viewerHandler = new ViewerHandler();
        accountHandler = new AccountHandler();
        eventHandler = new EventHandler();
        getConnection();
        log.trace("VBHandler is initializing.");
        VBMaintainer vbm = VBMaintainer.instance(log);
        vbm.begin();
        //initialize()
        log.trace("VBHandler begin() is done!");
    }

    public Connection getConnection() {
        if(con != null) return con;
        // sqlite driver
        try {
            Class.forName("org.sqlite.JDBC");
            String conPath = "jdbc:sqlite://" + tskrDir + vbFilename;
            log.debug("Attempting to connect to DB at: " + conPath);
            con = DriverManager.getConnection(conPath);
            con.setAutoCommit(true);
        } catch (ClassNotFoundException e) {
            log.severe("couldnt find JDBC");
            e.printStackTrace();
        } catch (SQLException e) {
            log.warn("couldnt connect to the viewerbase DB");
            SQLUtil.SQLExHandle(e,"exception while connecting to DB");
        }
        return con;
    }


    private String initPluginDir(String plugin) {
        String wDir = System.getProperty("user.dir");
        log.debug("Working Directory reported as: " + wDir);
        Path wPath = Paths.get(wDir);
        Path cPath = wPath.getParent();
        String cDir = "ERROR";
        try {
            cDir = cPath.toRealPath().toString();
        } catch (IOException e) {
            log.severe("Plugin unable to initialize properly due to inability to: find casterlabs base path.");
        }
        log.debug("Casterlabs-Caffeinated directory reported as: " + cDir);
        String pDir = cDir + "\\plugins";
        Path pPath = Paths.get(pDir);
        String tDir = "ERROR";
        try {
            tDir = pPath.toRealPath().toString() + "\\" + plugin;
        } catch (IOException e) {
            log.severe("Plugin unable to initialize properly due to inability to: find plugin directory path.");
        }
        Path tPath = Paths.get(tDir);

        if (!Files.isDirectory(tPath)) {
            log.warn("Unable to find " + plugin + " plugin directory: " + tDir);
            // create folder
            log.info("Creating " + plugin + " plugin directory: " + tDir);
            try {
                Files.createDirectories(tPath);
            } catch (IOException e) {
                log.severe("Plugin unable to initialize properly due to inability to: create " + plugin + " directory: " + tDir);
            }
        }
        return tDir + "\\";
    }

    ///////////////// VB ACTIONS //////////////////

    public List<Long> listAllAccounts() {
        List<Long> ids = new ArrayList<>();
        getConnection();
        log.trace("listing all Accounts");

        ActiveResult ar = new SelectQuery().select("id").from("accounts").execute(con);
        try {
            while(ar.rs.next()) {
                ids.add(ar.rs.getLong("id"));
            }
        } catch(SQLException e) {
            SQLUtil.SQLExHandle(e,"SQLException while selecting all Account ids");
        } finally { ar.close(); }

        log.debug("all Accounts list: " + ids);
        return ids;
    }

    public long findLatestSnapshot(long aid) {
        getConnection();
        log.trace("searching for id of latest snapshot for Account with id: " + aid);

        long sid = -1;
        ActiveResult ar = new SelectQuery()
                .select("latestsnapshot")
                .from("accounts")
                .where(SQLUtil.qol(DataType.LONG, "id", Op.EQUAL, aid))
                .execute(con);
        try {
            if (!ar.rs.next()) {
                log.warn("unable to find Account latestsnapshot for Account with id: " + aid);
                return -1;
            }
            sid = ar.rs.getLong("latestsnapshot");
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e,"SQLException while searching for Account latest snapshot: " + aid);
        } finally { ar.close(); }

        log.debug("id of latest snapshot is "+ sid + " for Account with id " + aid);
        return sid;
    }

    public Param findLastSnapshotValue(long aid, Param toFind) {
        getConnection();
        log.trace("finding last value of " + toFind + " for Account " + aid + " in snapshot records.");

        Object value = null;
        // if(toFind.type == DataType.INT) value = NumInfo.NUM_DEFAULT;
        // if(toFind.type == DataType.STRING) value = StringInfo.STRING_DEFAULT;

        ActiveResult ar = new SelectQuery()
                .select(SQLUtil.qpl(toFind))
                .from("snapshots")
                .where(SQLUtil.qol(DataType.LONG,"aid", Op.EQUAL, aid))
                .orderBy("id").desc()
                .execute(con);
        try {
            while(ar.rs.next()) {
                if(toFind.type == DataType.LONG) {
                    long result = ar.rs.getLong(toFind.column);
                    if(result == LongInfo.LONG_DEFAULT) {
                        continue;
                    } else {
                        value = result;
                        break;
                    }
                } else if(toFind.type == DataType.STRING) {
                    String result = ar.rs.getString(toFind.column);
                    //log.debug("lastValue contender: ", result);
                    if(result == null || result.equals(StringInfo.STRING_DEFAULT)) {
                        continue;
                    } else {
                        if(toFind.column == "roles") { //special case due to serialization, if this becomes regular need to add DataType.SERIALIZED
                            if(result.equals("[]")) continue;
                        } else if(toFind.column == "channelId") { //special case due to for some reason API returning int values
                            if(result.equals(String.valueOf(LongInfo.LONG_DEFAULT))) continue;
                        }
                        value = result;
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e, "SQLException while finding last value of " + toFind + " for Account " + aid + " in snapshot records.");
        } finally { ar.close(); }

        Param result = new Param(toFind.type, toFind.column, value);
        log.debug("last value of " + toFind + " for Account " + aid + " in snapshot records: \n" + result);
        return result;
    }

    public void checkAddCols(FastLogger log, List<String> cols) {
        if(cols.isEmpty()) {
            log.severe("no supported platforms specified while updating viewer columns!");
            return;
        }
        List<String> vtcols = SQLUtil.retrieveColumnNames(con,"viewers").stream()
                .map(col -> col.column)
                .collect(Collectors.toList());
        if(vtcols.containsAll(cols)) {
            log.debug("all platforms already available.");
            return;
        } else {
            for (String cname : cols) {
                if(vtcols.contains(cname)) continue;
                boolean executed = new AlterTableQuery()
                        .alterTable("viewers")
                        .addColumn(new Col(cname, DataType.LONG).references("accounts(id)"))
                        .execute(con);
                if(!executed) { 
                    log.warn("while adding platform column " + cname + ", AlterTableQuery claims to have not altered the table. such claims may be greatly exaggerated."); 
                }
            }
        }
    }

    /**
     * 
     * @param viewer
     * @param count
     * @return a list where the FIRST element is the LAST history event to have ocurred
     */
    public List<EventInfo> retrieveLastViewerInteractions(ViewerInfo viewer, long count) {
        OpParamList params = new OpParamList().setDefaultCnj(Cnj.OR);
        for(long acc : viewer.getAccountIds()) {
            params.add(new OpParam(DataType.LONG, "aid", Op.EQUAL, acc));
        }
        List<Long> eids = new ArrayList<>();
        ActiveResult ar = new SelectQuery()
                .select("id")
                .from("history")
                .where(params)
                .orderBy("timestamp").desc()
                .limit(count)
                .execute(con);
              
        try {
            while(ar.rs.next()) {
                eids.add(ar.rs.getLong("id"));
            }
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e, "sqlexception while retrieving ids for last viewer interactions for viewer: \n" + viewer);
        }

        List<EventInfo> eis = new ArrayList<>();
        for(long eid : eids) {
            eis.add(eventHandler.getFromVB(eid));
        }

        return eis;
    }

}