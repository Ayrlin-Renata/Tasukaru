package com.ayrlin.tasukaru;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ayrlin.sqlutil.*;
import com.ayrlin.sqlutil.query.*;
import com.ayrlin.sqlutil.query.data.*;
import com.ayrlin.sqlutil.query.data.OpParam.Op;
import com.ayrlin.tasukaru.data.*;
import com.ayrlin.tasukaru.data.EventInfo.TAct;
import com.ayrlin.tasukaru.data.EventInfo.UpType;

import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import lombok.Getter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;


public class VBHandler {
    private static final @Getter String vbVersion = "1.0.0";
    private static final @Getter String vbFilename = "ViewerBase.db";
    private static VBHandler instance;
    
    private Connection con;
    private FastLogger log;
    private @Getter String tskrDir;

    private VBHandler() {
        this.log = Tasukaru.instance().getLogger();
        this.tskrDir = initPluginDir("tasukaru");
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

    public int addAccount(AccountInfo ai) {
        getConnection();
        log.debug("Adding Account: \n" + ai);

        List<Param> params = new ArrayList<>();
        params.add(new Param(DataType.INT, "vid", ai.vid)); 
        params.add(new Param(DataType.INT, "latestSnapshot", -1)); //default
        params.add(new Param(DataType.STRING, "userId", ai.userId));
        params.add(new Param(DataType.STRING, "channelId", ai.channelId));
        params.add(new Param(DataType.STRING, "platform", ai.getPlatform()));
        params.add(new Param(DataType.STRING, "UPID", ai.UPID));

        params.add(new Param(DataType.STRING, "roles", ai.getRoles()));
        params.add(new Param(DataType.STRING, "badges", ai.getBadges()));
        params.add(new Param(DataType.STRING, "color", ai.color));
        params.add(new Param(DataType.STRING, "username", ai.username));
        params.add(new Param(DataType.STRING, "displayname", ai.displayname));
        params.add(new Param(DataType.STRING, "bio", ai.bio));
        params.add(new Param(DataType.STRING, "link", ai.link));
        params.add(new Param(DataType.STRING, "imageLink", ai.imageLink));
        params.add(new Param(DataType.INT, "followersCount", ai.followersCount));
        params.add(new Param(DataType.INT, "subCount", ai.subCount));

        long key = SQLUtil.insert(con, "accounts", params);
        if(key < 0) {
            log.severe("failed to add Account: \n" + ai);
            return -1;
        } 

        //add snapshot after Account for foreign key
        ai.id((int)key);
        log.trace("added Account id: " + ai.id);

        if(!updateAccount(ai)) { //will also handle snapshot
            log.severe("failed to update Account latestSnapshot while adding Account: \n" + ai);
            return -2;
        }

        return ai.id;
    }

    public boolean updateAccount(AccountInfo ai) {
        getConnection();
        log.debug("Updating Account info: " + ai);
        
        addSnapshot(ai);
        log.trace("Inserting new latestSnapshot into update: " + ai.latestSnapshot);
        
        List<Param> setParams = new ArrayList<>();
        setParams.add(new Param(DataType.INT, "vid", ai.vid));
        setParams.add(new Param(DataType.INT, "latestSnapshot", ai.latestSnapshot));
        setParams.add(new Param(DataType.STRING, "userId", ai.userId));
        setParams.add(new Param(DataType.STRING, "channelId", ai.channelId));
        setParams.add(new Param(DataType.STRING, "platform", ai.getPlatform()));
        setParams.add(new Param(DataType.STRING, "UPID", ai.UPID));

        setParams.add(new Param(DataType.STRING, "roles", ai.getRoles()));
        setParams.add(new Param(DataType.STRING, "badges", ai.getBadges()));
        setParams.add(new Param(DataType.STRING, "color", ai.color));
        setParams.add(new Param(DataType.STRING, "username", ai.username));
        setParams.add(new Param(DataType.STRING, "displayname", ai.displayname));
        setParams.add(new Param(DataType.STRING, "bio", ai.bio));
        setParams.add(new Param(DataType.STRING, "link", ai.link));
        setParams.add(new Param(DataType.STRING, "imageLink", ai.imageLink));
        setParams.add(new Param(DataType.INT, "followersCount", ai.followersCount));
        setParams.add(new Param(DataType.INT, "subCount", ai.subCount));
        
        List<OpParam> whereParams = new ArrayList<>();
        whereParams.add(new OpParam(DataType.INT, "id", Op.EQUAL, ai.id));
        
        if(!SQLUtil.update(con, "accounts", setParams, whereParams)) {
            log.severe("failed to update Account: \n" + ai);
            return false;
        } 
        return true;
    }

    /**
     * Creates a latestSnapshot and adds it to vi
     * @param ai
     * @return the snapshot id, equal to vi.latestSnapshot
     */
    public int addSnapshot(AccountInfo ai) {
        getConnection();
        log.debug("Adding account snapshot for Account: \n" + ai);

        if (ai.id <= 0) {
            int aid = findAccountId(ai);
            if (aid < 0) {
                log.severe("Failed to add account snapshot due to lack of id for Account: \n" + ai);
                return -1;
            } else {
                ai.id = aid;
            }
        }

        List<Param> params = new ArrayList<>();
        params.add(new Param(DataType.INT, "aid", ai.id));
        params.add(new Param(DataType.INT, "vid", ai.vid));
        params.add(new Param(DataType.STRING, "userId", ai.userId));
        params.add(new Param(DataType.STRING, "channelId", ai.channelId));
        params.add(new Param(DataType.STRING, "platform", ai.getPlatform()));
        params.add(new Param(DataType.STRING, "UPID", ai.UPID));

        params.add(new Param(DataType.STRING, "roles", ai.getRoles()));
        params.add(new Param(DataType.STRING, "badges", ai.getBadges()));
        params.add(new Param(DataType.STRING, "color", ai.color));
        params.add(new Param(DataType.STRING, "username", ai.username));
        params.add(new Param(DataType.STRING, "displayname", ai.displayname));
        params.add(new Param(DataType.STRING, "bio", ai.bio));
        params.add(new Param(DataType.STRING, "link", ai.link));
        params.add(new Param(DataType.STRING, "imageLink", ai.imageLink));
        params.add(new Param(DataType.INT, "followersCount", ai.followersCount));
        params.add(new Param(DataType.INT, "subCount", ai.subCount));

        long key = SQLUtil.insert(con, "snapshots", params);
        if(key < 0) {
            log.severe("Failed to add account snapshot for Account: \n" + ai);
            return -1;
        }

        ai.latestSnapshot = (int)key;

        EventInfo ei = new EventInfo()
                .account(ai)
                .snapshotId(ai.latestSnapshot)
                .uptype(UpType.TECHNICAL)
                .action(TAct.SNAPSHOT);
        addHistory(ei);

        return ai.latestSnapshot;
    }

    public int addHistory(EventInfo ei) {
        getConnection();
        if (ei.timestamp == null) {
            ei.timestamp = new java.sql.Timestamp(new java.util.Date().getTime());
        }
        log.debug("Adding history for event: \n" + ei);

        List<Param> params = new ArrayList<>();
        params.add(new Param(DataType.INT, "aid", ei.account.id));
        params.add(new Param(DataType.INT, "sid", ei.snapshotId));
        params.add(new Param(DataType.STRING, "uptype", ei.uptype));
        params.add(new Param(DataType.STRING, "action", ei.action));
        params.add(new Param(DataType.INT, "value", ei.value));
        params.add(new Param(DataType.STRING, "event", Rson.DEFAULT.toJson(ei.event).toString()));
        params.add(new Param(DataType.TIMESTAMP, "timestamp", ei.timestamp));
        params.add(new Param(DataType.STRING, "streamState", ei.streamState));
        params.add(new Param(DataType.STRING, "processed", ei.processed));

        long key = SQLUtil.insert(con, "history", params);
        if(key < 0) {
            log.severe("failed to addHistory() for event: \n" + ei);
        }
        return (int)key;
    }

    public long addViewer(AccountInfo ai) {
        List<Long> accounts = new ArrayList<>();
        accounts.add((long) ai.id);
        ViewerInfo vi = new ViewerInfo()
                .fallbackName(ai.displayname)
                .points(0)
                .watchtime(0)
                .accountIds(accounts);
        long key = addViewer(vi);
        ai.vid(key);
        return key;
    }

    public long addViewer(ViewerInfo vi) {
        getConnection();
        log.trace("Adding viewer: \n" + vi);

        List<Param> params = new ArrayList<>();
        params.add(new Param(DataType.STRING,"clId",vi.clId));
        params.add(new Param(DataType.STRING,"clName",vi.clName));
        params.add(new Param(DataType.STRING,"fallbackName",vi.fallbackName));
        params.add(new Param(DataType.INT,"watchtime",vi.watchtime));
        params.add(new Param(DataType.INT,"points",vi.points));

        List<AccountInfo> accs = new ArrayList<>();
        for(Long aid : vi.accountIds) {
            UserPlatform accPlat = getAccountPlatform(aid);
            params.add(new Param(DataType.INT,accPlat.name(), aid));
            accs.add(retrieveCurrentAccountInfo(aid));
        }
        
        long key = new InsertIntoQuery().insertInto("viewers").values(params).execute(con);
        log.debug("added viewer: \n" + vi);
        log.trace("updating viewer accounts: \n" + accs);
        for(AccountInfo ai : accs) {
            ai.vid(key);
            updateAccount(ai);
        }
        return key;
    }

    public ViewerInfo findViewer(AccountInfo ai) {
        getConnection();
        ActiveResult ar = new SelectQuery()
                .select("id")
                .from("viewers")
                .where(SQLUtil.qol(DataType.STRING, ai.platform.name(), Op.EQUAL, ai.id))
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
        return retrieveViewer(vid);
    }

    public ViewerInfo retrieveViewer(long vid) {
        getConnection();
        log.trace("retrieving viewer with id: " + vid);
        ActiveResult ar = new SelectQuery()
                .select("*")
                .from("viewers")
                .where(SQLUtil.qol(DataType.STRING, "id", Op.EQUAL, vid))
                .execute(con);
        ViewerInfo vi = new ViewerInfo();
        try {
            if(ar.rs.next()) {
                vi.id(ar.rs.getLong("id"));
                vi.clId(ar.rs.getString("clId"));
                vi.clName(ar.rs.getString("clName"));
                vi.fallbackName(ar.rs.getString("fallbackName"));
                vi.watchtime(ar.rs.getLong("watchtime"));
                vi.points(ar.rs.getLong("points"));
                List<Long> aids = new ArrayList<>();
                for(String plat : TLogic.instance().getSupportedPlatforms()) {
                    aids.add(ar.rs.getLong(plat));
                }
            }
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e, "exception while retrieving viewer for account with id " + vid);
            return null;
        } finally {
            ar.close();
        }
        if(vi.id < 0) {
            log.warn("unable to retrieve viewer with id " + vid);
            return null;
        }
        log.debug("retrieved viewer: \n" + vi);
        return vi;
    } 

    public boolean updateViewer(ViewerInfo vi) {
        getConnection();
        log.trace("updating viewer: \n" + vi);

        List<Param> params = new ArrayList<>();
        params.add(new Param(DataType.STRING, "clId", vi.clId));
        params.add(new Param(DataType.STRING, "clName", vi.clName));
        params.add(new Param(DataType.STRING, "fallbackName", vi.fallbackName));
        params.add(new Param(DataType.INT, "watchtime", vi.watchtime));
        params.add(new Param(DataType.INT, "points", vi.points));
        for(Long aid : vi.accountIds) {
            params.add(new Param(DataType.INT,getAccountPlatform(aid).name(), aid));
        }

        boolean result = new UpdateQuery()
                .update("viewers")
                .set(params)
                .where(SQLUtil.qol(DataType.INT, "id", Op.EQUAL, vi.id))
                .execute(con);

        if(result) {
            log.debug("updated viewer: \n" + vi);
        } else {
            log.warn("unable to update viewer: \n" + vi);
        }
        return result;
    }

    private UserPlatform getAccountPlatform(Long aid) {
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
    public int findAccountId(AccountInfo ai) {
        getConnection();
        log.trace("searching for id of Account: \n" + ai);

        List<OpParam> whereList = new ArrayList<>();

        // determine most reliable info
        if (!ai.UPID.isEmpty()) {
            whereList.add(new OpParam(DataType.STRING, "UPID", Op.EQUAL, ai.UPID));
        } else if (ai.platform != null) {
            whereList.add(new OpParam(DataType.STRING, "platform", Op.EQUAL, ai.getPlatform()));
            if (!ai.userId.isEmpty()) {
                whereList.add(new OpParam(DataType.STRING, "userId", Op.EQUAL, ai.userId));
            } else if (!ai.username.isEmpty()) {
                whereList.add(new OpParam(DataType.STRING, "username", Op.EQUAL, ai.username));
            } else if (!ai.link.isEmpty()) {
                whereList.add(new OpParam(DataType.STRING, "link", Op.EQUAL, ai.link));
            } else if (!ai.displayname.isEmpty()) {
                whereList.add(new OpParam(DataType.STRING, "displayname", Op.EQUAL, ai.displayname));
            } else {
                //abort
                log.warn("abort finding Account: \n" + ai);
                return -2;
            }
        } else {
            log.warn("abort finding Account: \n" + ai);
            return -2;
        }

        int accountId;
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

        ai.id(accountId);
        log.debug("found Account id " + accountId + " for Account: \n" + ai);
        return accountId;
    }

    public AccountInfo retrieveCurrentAccountInfo(long id) {
        getConnection();
        log.trace("retrieving current Account info.");

        ActiveResult ar = new SelectQuery()
                .select("*")
                .from("accounts")
                .where(SQLUtil.qol(DataType.INT, "id", Op.EQUAL, id))
                .execute(con);
        ResultSet rs = ar.rs;
        AccountInfo ci = new AccountInfo();
        try {
            if (!rs.next()) {
                log.warn("unable to find Account with id: \n" + id);
                return null;
            }

            ci.id((int)rs.getLong("id"))
                    .vid(rs.getLong("vid"))
                    .latestSnapshot((int)rs.getLong("latestSnapshot"))
                    .userId(rs.getString("userId"))
                    .channelId(rs.getString("channelId"))
                    .platform(rs.getString("platform"))
                    .upid(rs.getString("upid"))
                    .roles(rs.getString("roles"))
                    .badges(rs.getString("badges"))
                    .color(rs.getString("color"))
                    .username(rs.getString("username"))
                    .displayname(rs.getString("displayname"))
                    .bio(rs.getString("bio"))
                    .link(rs.getString("link"))
                    .imageLink(rs.getString("imageLink"))
                    .followersCount((int)rs.getLong("followersCount"))
                    .subCount((int)rs.getLong("subCount"));
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e,"SQLException while retrieving Account info with id: \n" + id);
        } finally { ar.close(); }
        log.debug("retrieved current Account info: \n" + ci);
        return ci;
    }

    public boolean verifyCurrentAccountInfo(AccountInfo ai) {
        log.trace("verifying current Account info for Account: \n" + ai);
        AccountInfo ci = retrieveCurrentAccountInfo(ai.id);
        boolean result = ai.notContradictory(ci);
        log.debug("Account info is " + (result? "" : "not") + " current for Account: \n" + ai + " with current info: \n" + ci);
        return result;
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

    public int findLatestSnapshot(int aid) {
        getConnection();
        log.trace("searching for id of latest snapshot for Account with id: " + aid);

        int sid = -1;
        ActiveResult ar = new SelectQuery()
                .select("latestSnapshot")
                .from("accounts")
                .where(SQLUtil.qol(DataType.INT, "id", Op.EQUAL, aid))
                .execute(con);
        try {
            if (!ar.rs.next()) {
                log.warn("unable to find Account latestSnapshot for Account with id: " + aid);
                return -1;
            }
            sid = ar.rs.getInt("latestSnapshot");
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e,"SQLException while searching for Account latest snapshot: " + aid);
        } finally { ar.close(); }

        log.debug("id of latest snapshot is "+ sid + " for Account with id " + aid);
        return sid;
    }

    public Param findLastSnapshotValue(int aid, Param toFind) {
        getConnection();
        log.trace("finding last value of " + toFind + " for Account " + aid + " in snapshot records.");

        Object value = null;
        if(toFind.type == DataType.INT) value = AccountInfo.INT_DEFAULT;
        if(toFind.type == DataType.STRING) value = AccountInfo.STRING_DEFAULT;

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
                    if(result == AccountInfo.INT_DEFAULT) {
                        continue;
                    } else {
                        value = result;
                        break;
                    }
                } else if(toFind.type == DataType.STRING) {
                    String result = ar.rs.getString(toFind.column);
                    //log.debug("lastValue contender: ", result);
                    if(result == null || result.equals(AccountInfo.STRING_DEFAULT)) {
                        continue;
                    } else {
                        if(toFind.column == "roles") { //special case due to serialization, if this becomes regular need to add DataType.SERIALIZED
                            if(result.equals("[]")) continue;
                        } else if(toFind.column == "channelId") { //special case due to for some reason API returning int values
                            if(result.equals(String.valueOf(AccountInfo.INT_DEFAULT))) continue;
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
        log.debug("adding " + points + " points to viewer: " + ei.viewer.getName());
        
        ei.viewer.points += points;
        
        updateViewer(ei.viewer);
        addHistory(new EventInfo()
                .account(ei.account)
                .snapshotId(ei.snapshotId)
                .uptype(UpType.TECHNICAL)
                .action(TAct.POINTS)
                .value(points)
                .streamState(ei.streamState));
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