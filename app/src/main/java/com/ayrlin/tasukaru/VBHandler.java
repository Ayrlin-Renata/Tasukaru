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
import com.ayrlin.sqlutil.query.data.OpParam.Op;
import com.ayrlin.sqlutil.query.data.Param;
import com.ayrlin.tasukaru.data.AccountInfo;
import com.ayrlin.tasukaru.data.EventInfo;
import com.ayrlin.tasukaru.data.EventInfo.TAct;
import com.ayrlin.tasukaru.data.EventInfo.UpType;
import com.ayrlin.tasukaru.data.handler.AccountHandler;
import com.ayrlin.tasukaru.data.handler.EventHandler;
import com.ayrlin.tasukaru.data.handler.ViewerHandler;
import com.ayrlin.tasukaru.data.ViewerInfo;
import com.ayrlin.tasukaru.data.info.NumInfo;
import com.ayrlin.tasukaru.data.info.StringInfo;

import co.casterlabs.koi.api.types.user.UserPlatform;
import lombok.Getter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;


public class VBHandler {
    private static final @Getter String vbVersion = "1.0.0";
    private static final @Getter String vbFilename = "ViewerBase.db";
    private static VBHandler instance;
    
    private Connection con;
    private FastLogger log;
    private @Getter String tskrDir;
    private @Getter ViewerHandler viewerHandler;
    private @Getter AccountHandler accountHandler;
    private @Getter EventHandler eventHandler;

    private VBHandler() {
        this.log = Tasukaru.instance().getLogger();
        this.tskrDir = initPluginDir("tasukaru");
        viewerHandler = new ViewerHandler();
        accountHandler = new AccountHandler();
        eventHandler = new EventHandler();
    }

    /**
     * singleton pattern
     * @return THE VBHandler
     */
    public static VBHandler instance() {
        if(instance == null) {
            instance = new VBHandler();
        } 
        return instance;
    }

    public void begin() {
        getConnection();
        log.trace("VBHandler is initializing.");
        VBMaintainer vbm = VBMaintainer.instance(log);
        vbm.begin();
        //initialize()
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

    public ViewerInfo findViewer(AccountInfo ai) {
        getConnection();
        ActiveResult ar = new SelectQuery()
                .select("id")
                .from("viewers")
                .where(SQLUtil.qol(DataType.STRING, ai.get("platform").toString(), Op.EQUAL, ai.get("id")))
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
        return viewerHandler.getFromVB(vid);
    }

    public UserPlatform getAccountPlatform(Long aid) {
        ActiveResult ar = new SelectQuery()
                .select("platform")
                .from("accounts")
                .where(SQLUtil.qol(DataType.INT, "id", Op.EQUAL, aid))
                .execute(con);
        String platString = "";
        try {
            if(ar.rs.next()) {
                platString = ar.rs.getString("platform");
            }
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e, "exception while retrieving platform for account " + aid);
        } finally {
            ar.close();
        }
        if(platString.isEmpty()) {
            log.warn("unable to retrieve platform for account " + aid);
            return null;
        }
        log.debug("retrieved platform " + platString + " for account " + aid);
        return UserPlatform.valueOf(platString);
    }

    /**
     * @return AccountId if Account exists,
     *         -1 if successfully found no Account,
     *         -2 if errored
     */
    public long findAccountId(AccountInfo ai) {
        getConnection();
        log.trace("searching for id of Account: \n" + ai);

        List<OpParam> whereList = new ArrayList<>();

        // determine most reliable info
        if (!((String) ai.get("UPID")).isEmpty()) {
            whereList.add(new OpParam(DataType.STRING, "UPID", Op.EQUAL, ai.get("UPID")));
        } else if (ai.get("platform") != null) {
            whereList.add(new OpParam(DataType.STRING, "platform", Op.EQUAL, ai.get("platform")));
            if (!((String) ai.get("userId")).isEmpty()) {
                whereList.add(new OpParam(DataType.STRING, "userId", Op.EQUAL, ai.get("userId")));
            } else if (!((String) ai.get("username")).isEmpty()) {
                whereList.add(new OpParam(DataType.STRING, "username", Op.EQUAL, ai.get("username")));
            } else if (!((String) ai.get("link")).isEmpty()) {
                whereList.add(new OpParam(DataType.STRING, "link", Op.EQUAL, ai.get("link")));
            } else if (!((String) ai.get("displayname")).isEmpty()) {
                whereList.add(new OpParam(DataType.STRING, "displayname", Op.EQUAL, ai.get("displayname")));
            } else {
                //abort
                log.warn("abort finding Account: \n" + ai);
                return -2;
            }
        } else {
            log.warn("abort finding Account: \n" + ai);
            return -2;
        }

        long accountId;
        ActiveResult ar = new SelectQuery().select("id").from("accounts").where(whereList).execute(con);
        try {
            if (!ar.rs.next() || ar.rs.getInt("id") <= 0 ) {
                log.warn("unable to find Account: \n" + ai);
                return -1;
            }
            accountId = ar.rs.getInt("id");
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e,"SQLException while finding Account: " + ai);
            return -2;
        } finally { ar.close(); }

        ai.set("id", accountId);
        log.debug("found Account id " + accountId + " for Account: \n" + ai);
        return accountId;
    }

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
                .where(SQLUtil.qol(DataType.INT, "id", Op.EQUAL, aid))
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
                .where(SQLUtil.qol(DataType.INT,"aid", Op.EQUAL, aid))
                .orderBy("id").desc()
                .execute(con);
        try {
            while(ar.rs.next()) {
                if(toFind.type == DataType.INT) {
                    long result = ar.rs.getLong(toFind.column);
                    if(result == NumInfo.NUM_DEFAULT) {
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
                            if(result.equals(String.valueOf(NumInfo.NUM_DEFAULT))) continue;
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
                        .addColumn(new Col(cname, DataType.INT).references("accounts(id)"))
                        .execute(con);
                if(!executed) { 
                    log.warn("while adding platform column " + cname + ", AlterTableQuery claims to have not altered the table. such claims may be greatly exaggerated."); 
                }
            }
        }
    }

    public void addPoints(EventInfo ei, Long points) {
        log.debug("adding " + points + " points to viewer: " + ei.getViewer().getName());
        
        ei.getViewer().set("points", (Long) ei.getViewer().get("points") + points);
        
        viewerHandler.updateToVB(ei.getViewer());
        eventHandler.addToVB(new EventInfo()
                .set("account", ei.get("account"))
                .set("sid", ei.get("sid"))
                .set("uptype", UpType.TECHNICAL.toString())
                .set("action", TAct.POINTS.toString())
                .set("value", points)
                .set("streamState", ei.get("streamState")));
    }

    /**
     * 
     * @param viewer
     * @param count
     * @return a list where the FIRST element is the LAST history event to have ocurred
     */
    // public List<EventInfo> retrieveLastViewerInteractions(ViewerInfo viewer, int count) {
    //     // TODO
    //     if(viewer.accountIds == null || viewer.accountIds.isEmpty()) {
    //         getViewerAccounts(viewer);
    //     }
    //     List<OpParam> params = new ArrayList<>();
    //     for(long acc : viewer.accountIds) {
    //         params.add(new OpParam(DataType.INT, "aid", Op.EQUAL, acc));
    //     }
    //     //TODO implement OR in select
    //     List<Long> eids = new ArrayList<>();
    //     ActiveResult ar = new SelectQuery()
    //             .select("id")
    //             .from("history")
    //             .where(params)
    //             .orderBy("timestamp").desc()
    //             .limit(count)
    //             .execute(con);
    //     //ar.rs.next()

    //     List<EventInfo> eis = new ArrayList<>();
    //     for(long eid : eids) {
    //         eis.add(retrieveHistory(eid));
    //     }

    //     return eis;
    // }

    // public void getViewerAccounts(ViewerInfo viewer) {
    //     //TODO
    //     //viewer.accountIds = 
    // }
}