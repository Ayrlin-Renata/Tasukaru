package com.ayrlin.tasukaru;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.ayrlin.sqlutil.ActiveResult;
import com.ayrlin.sqlutil.SQLUtil;
import com.ayrlin.sqlutil.query.Parameter;
import com.ayrlin.sqlutil.query.SelectQuery;

import com.ayrlin.sqlutil.query.Parameter.DataType;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class VBHandler {
    public static final String vbVersion = "1.0.0";

    private static Connection con;
    private static boolean initd = false;
    
    private FastLogger log;
    private String tDir;

    public VBHandler(FastLogger fl, String td) {
        log = fl;
        tDir = td;
    }

    public void run() {
        try {
            getConnection();
            initialise();
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e,"exception during run init");
        }
    }

    private void getConnection() {
        // sqlite driver
        try {
            Class.forName("org.sqlite.JDBC");
            String conPath = "jdbc:sqlite://" + tDir + "ViewerBase.db";
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
    }

    private void initialise() throws SQLException {
        if (!initd) {
            initd = true;

            String checkSQLBegin = "SELECT name FROM sqlite_master WHERE type='table' AND name='";
            String checkSQLEnd = "'";

            // META
            if (!con.createStatement()
                    .executeQuery(checkSQLBegin + "meta" + checkSQLEnd)
                    .next()) {
                log.warn("No Meta table found, creating new Meta table.");
                createMetaTable();
            }
            String checkVersion = "SELECT value FROM `meta` WHERE property = \"version\";";
            ResultSet versionCheckResult = con.createStatement().executeQuery(checkVersion);
            if(!versionCheckResult.next()) {
                log.severe("malformed meta table has no version");
            } else {
                String readVersion = versionCheckResult.getString("property"); 
                if(!readVersion.equals(vbVersion)) {
                    log.warn("Mismatched versions! Database reports as version " + readVersion + ", plugin version is " + vbVersion + "!");
                    //TODO update DB to current version, including backup
                }
            }

            // VIEWER SNAPSHOT
            if (!con.createStatement()
                    .executeQuery(checkSQLBegin + "vsnapshots" + checkSQLEnd)
                    .next()) {
                log.warn("No Viewer Snapshot table found, creating new Viewer Snapshot table.");
                createViewerSnapshotTable();
            }

            // VIEWERS
            if (!con.createStatement()
                    .executeQuery(checkSQLBegin + "viewers" + checkSQLEnd)
                    .next()) {
                log.warn("No Viewer table found, creating new Viewer table.");
                createViewerTable();
            }

            // HISTORY
            if (!con.createStatement()
                    .executeQuery(checkSQLBegin + "history" + checkSQLEnd)
                    .next()) {
                log.warn("No History table found, creating new History table.");
                createHistoryTable();
            }
        }
    }

    private void createMetaTable() throws SQLException {
        // meta table definition
        con.createStatement().executeUpdate("CREATE TABLE `meta` ("
                + "property TEXT PRIMARY KEY,"
                + "value TEXT"
                + ");");
        con.createStatement().executeUpdate("insert into meta values(\"version\",\"" + vbVersion + "\");");
    }

    private void createViewerSnapshotTable() throws SQLException {
        // vsnapshots table definition
        con.createStatement().executeUpdate("CREATE TABLE `vsnapshots` ("
                + "id INTEGER PRIMARY KEY,"
                + "vid INTEGER NOT NULL,"
                + "userId TEXT," // koi.api.types.user.User.id
                + "channelId TEXT,"
                + "platform TEXT," // koi.api.types.user.User.platform.name
                + "UPID TEXT,"
                + "roles TEXT," // list UserRoles
                + "badges TEXT," // list String
                + "color TEXT,"
                + "username TEXT,"
                + "displayname TEXT,"
                + "bio TEXT,"
                + "link TEXT,"
                + "imageLink TEXT,"
                + "followersCount INTEGER,"
                + "subCount INTEGER,"
                + "FOREIGN KEY (vid) REFERENCES `viewers` (id)"
                + ");");
    }

    private void createViewerTable() throws SQLException {
        // viewers table definition
        con.createStatement().executeUpdate("CREATE TABLE `viewers` (" 
                + "id INTEGER PRIMARY KEY,"
                + "latestSnapshot INTEGER,"
                + "userId TEXT," // koi.api.types.user.User.id
                + "channelId TEXT,"
                + "platform TEXT," // koi.api.types.user.User.platform.name
                + "UPID TEXT,"
                + "roles TEXT," // list UserRoles
                + "badges TEXT," // list String
                + "color TEXT,"
                + "username TEXT,"
                + "displayname TEXT,"
                + "bio TEXT,"
                + "link TEXT,"
                + "imageLink TEXT,"
                + "followersCount INTEGER,"
                + "subCount INTEGER,"
                + "watchtime INTEGER," // in seconds, would take ~130 years to go over 32 bit
                + "tskrpoints INTEGER,"
                + "FOREIGN KEY (latestSnapshot) REFERENCES `vsnapshots` (id)"
                + ");");
    }

    private void createHistoryTable() throws SQLException {
        // history table definition
        con.createStatement().executeUpdate("CREATE TABLE `history` ("
                + "id INTEGER PRIMARY KEY,"
                + "vid INTEGER NOT NULL," // foreign key viewers
                + "sid INTEGER NOT NULL," // foreign key vsnapshots
                + "uptype TEXT NOT NULL," // present, absent, technical
                + "action TEXT,"
                + "value INTEGER,"
                + "timestamp TEXT,"
                + "streamstate TEXT,"
                + "FOREIGN KEY (vid) REFERENCES `viewers` (id)"
                + "FOREIGN KEY (sid) REFERENCES `vsnapshots` (id)"
                + ");");
    }

    ///////////////// VB ACTIONS //////////////////

    public int addViewer(ViewerInfo vi) {
        if (con == null) {
            getConnection();
        }
        log.debug("Adding viewer: \n" + vi);

        List<Parameter> params = new ArrayList<>();
        params.add(new Parameter(DataType.INT, "latestSnapshot", -1)); //default
        params.add(new Parameter(DataType.STRING, "userId", vi.userId));
        params.add(new Parameter(DataType.STRING, "channelId", vi.channelId));
        params.add(new Parameter(DataType.STRING, "platform", vi.platform));
        params.add(new Parameter(DataType.STRING, "UPID", vi.UPID));

        params.add(new Parameter(DataType.STRING, "roles", vi.getRoles()));
        params.add(new Parameter(DataType.STRING, "badges", vi.getBadges()));
        params.add(new Parameter(DataType.STRING, "color", vi.color));
        params.add(new Parameter(DataType.STRING, "username", vi.username));
        params.add(new Parameter(DataType.STRING, "displayname", vi.displayname));
        params.add(new Parameter(DataType.STRING, "bio", vi.bio));
        params.add(new Parameter(DataType.STRING, "link", vi.link));
        params.add(new Parameter(DataType.STRING, "imageLink", vi.imageLink));
        params.add(new Parameter(DataType.INT, "followersCount", vi.followersCount));
        params.add(new Parameter(DataType.INT, "subCount", vi.subCount));

        params.add(new Parameter(DataType.INT, "watchtime", vi.watchtime));
        params.add(new Parameter(DataType.INT, "tskrpoints", vi.tskrpoints));

        long key = SQLUtil.insert(con, "viewers", params);
        if(key < 0) {
            log.severe("failed to add viewer: \n" + vi);
            return -1;
        } 

        //add snapshot after viewer for foreign key
        vi.id((int)key);
        log.trace("added viewer id: " + vi.id);

        if(!updateViewer(vi)) { //will also handle snapshot
            log.severe("failed to update viewer latestSnapshot while adding viewer: \n" + vi);
            return -2;
        }

        return vi.id;
    }

    public boolean updateViewer(ViewerInfo vi) {
        if (con == null) {
            getConnection();
        }
        log.debug("Updating viewer info: " + vi);
        
        addViewerSnapshot(vi);
        log.trace("Inserting new latestSnapshot into update: " + vi.latestSnapshot);
        
        List<Parameter> setParams = new ArrayList<>();
        setParams.add(new Parameter(DataType.INT, "latestSnapshot", vi.latestSnapshot));
        setParams.add(new Parameter(DataType.STRING, "userId", vi.userId));
        setParams.add(new Parameter(DataType.STRING, "channelId", vi.channelId));
        setParams.add(new Parameter(DataType.STRING, "platform", vi.platform));
        setParams.add(new Parameter(DataType.STRING, "UPID", vi.UPID));

        setParams.add(new Parameter(DataType.STRING, "roles", vi.getRoles()));
        setParams.add(new Parameter(DataType.STRING, "badges", vi.getBadges()));
        setParams.add(new Parameter(DataType.STRING, "color", vi.color));
        setParams.add(new Parameter(DataType.STRING, "username", vi.username));
        setParams.add(new Parameter(DataType.STRING, "displayname", vi.displayname));
        setParams.add(new Parameter(DataType.STRING, "bio", vi.bio));
        setParams.add(new Parameter(DataType.STRING, "link", vi.link));
        setParams.add(new Parameter(DataType.STRING, "imageLink", vi.imageLink));
        setParams.add(new Parameter(DataType.INT, "followersCount", vi.followersCount));
        setParams.add(new Parameter(DataType.INT, "subCount", vi.subCount));
        
        List<Parameter> whereParams = new ArrayList<>();
        whereParams.add(new Parameter(DataType.INT, "id", vi.id));
        
        if(!SQLUtil.update(con, "viewers", setParams, whereParams)) {
            log.severe("failed to update viewer: \n" + vi);
            return false;
        } 
        return true;
    }

    /**
     * Creates a latestSnapshot and adds it to vi
     * @param vi
     * @return the snapshot id, equal to vi.latestSnapshot
     */
    public int addViewerSnapshot(ViewerInfo vi) {
        if (con == null) {
            getConnection();
        }
        log.debug("Adding viewer snapshot for viewer: \n" + vi);

        if (vi.id <= 0) {
            int vid = findViewerId(vi);
            if (vid < 0) {
                log.severe("Failed to add viewer snapshot due to lack of id for viewer: \n" + vi);
                return -1;
            } else {
                vi.id = vid;
            }
        }

        List<Parameter> params = new ArrayList<>();
        params.add(new Parameter(DataType.INT, "vid", vi.id));
        params.add(new Parameter(DataType.STRING, "userId", vi.userId));
        params.add(new Parameter(DataType.STRING, "channelId", vi.channelId));
        params.add(new Parameter(DataType.STRING, "platform", vi.platform));
        params.add(new Parameter(DataType.STRING, "UPID", vi.UPID));

        params.add(new Parameter(DataType.STRING, "roles", vi.getRoles()));
        params.add(new Parameter(DataType.STRING, "badges", vi.getBadges()));
        params.add(new Parameter(DataType.STRING, "color", vi.color));
        params.add(new Parameter(DataType.STRING, "username", vi.username));
        params.add(new Parameter(DataType.STRING, "displayname", vi.displayname));
        params.add(new Parameter(DataType.STRING, "bio", vi.bio));
        params.add(new Parameter(DataType.STRING, "link", vi.link));
        params.add(new Parameter(DataType.STRING, "imageLink", vi.imageLink));
        params.add(new Parameter(DataType.INT, "followersCount", vi.followersCount));
        params.add(new Parameter(DataType.INT, "subCount", vi.subCount));

        long key = SQLUtil.insert(con, "vsnapshots", params);
        if(key < 0) {
            log.severe("Failed to add viewer snapshot for viewer: \n" + vi);
            return -1;
        }

        vi.latestSnapshot = (int)key;

        EventInfo ei = new EventInfo()
                .viewer(vi)
                .snapshotId(vi.latestSnapshot)
                .uptype("technical")
                .action("vsnapshot");
        addHistory(ei);

        return vi.latestSnapshot;
    }

    public int addHistory(EventInfo ei) {
        if (con == null) {
            getConnection();
        }
        if (ei.timestamp == null) {
            ei.timestamp = new java.sql.Timestamp(new java.util.Date().getTime());
        }
        log.debug("Adding history for event: \n" + ei);

        List<Parameter> params = new ArrayList<>();
        params.add(new Parameter(DataType.INT, "vid", ei.viewer.id));
        params.add(new Parameter(DataType.INT, "sid", ei.snapshotId));
        params.add(new Parameter(DataType.STRING, "uptype", ei.uptype));
        params.add(new Parameter(DataType.STRING, "action", ei.action));
        params.add(new Parameter(DataType.INT, "value", ei.value));
        params.add(new Parameter(DataType.STRING, "streamState", ei.streamState));
        params.add(new Parameter(DataType.TIMESTAMP, "timestamp", ei.timestamp));

        long key = SQLUtil.insert(con, "history", params);
        if(key < 0) {
            log.severe("failed to addHistory() for event: \n" + ei);
        }
        return (int)key;
    }

    /**
     * @return viewerId if viewer exists,
     *         -1 if successfully found no viewer,
     *         -2 if errored
     */
    public int findViewerId(ViewerInfo vi) {
        if (con == null) {
            getConnection();
        }
        log.trace("searching for id of viewer: \n" + vi);

        List<Parameter> whereList = new ArrayList<>();

        // determine most reliable info
        if (!vi.UPID.isEmpty()) {
            whereList.add(new Parameter(DataType.STRING, "UPID", vi.UPID));
        } else if (!vi.platform.isEmpty()) {
            whereList.add(new Parameter(DataType.STRING, "platform", vi.platform));
            if (!vi.userId.isEmpty()) {
                whereList.add(new Parameter(DataType.STRING, "userId", vi.userId));
            } else if (!vi.username.isEmpty()) {
                whereList.add(new Parameter(DataType.STRING, "username", vi.username));
            } else if (!vi.link.isEmpty()) {
                whereList.add(new Parameter(DataType.STRING, "link", vi.link));
            } else if (!vi.displayname.isEmpty()) {
                whereList.add(new Parameter(DataType.STRING, "displayname", vi.displayname));
            } else {
                //abort
                log.warn("abort finding viewer: \n" + vi);
                return -2;
            }
        } else {
            log.warn("abort finding viewer: \n" + vi);
            return -2;
        }

        int viewerId;
        SelectQuery sq = new SelectQuery()
                .select("id")
                .from("viewers")
                .where(whereList);
        ActiveResult ar = SQLUtil.select(con, sq);
        try {
            if (!ar.rs.next() || ar.rs.getInt("id") <= 0 ) {
                log.warn("unable to find viewer: \n" + vi);
                return -1;
            }
            viewerId = ar.rs.getInt("id");
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e,"SQLException while finding viewer: " + vi);
            return -2;
        } finally { ar.close(); }

        vi.id(viewerId);
        log.debug("found viewer id " + viewerId + " for viewer: \n" + vi);
        return viewerId;
    }

    public ViewerInfo retrieveCurrentViewerInfo(long id) {
        if (con == null) {
            getConnection();
        }
        log.trace("retrieving current viewer info.");

        SelectQuery sq = new SelectQuery().select("*").from("viewers").where(SQLUtil.qpl(DataType.INT, "id", id));
        ActiveResult ar = SQLUtil.select(con, sq);
        ResultSet rs = ar.rs;
        ViewerInfo ci = new ViewerInfo();
        try {
            if (!rs.next()) {
                log.warn("unable to find viewer with id: \n" + id);
                return null;
            }

            ci.id((int)rs.getLong("id"))
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
                    .subCount((int)rs.getLong("subCount"))
                    .watchtime((int)rs.getLong("watchtime"))
                    .tskrpoints((int)rs.getLong("tskrpoints"));
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e,"SQLException while retrieving viewer info with id: \n" + id);
        } finally { ar.close(); }
        log.debug("retrieved current viewer info: \n" + ci);
        return ci;
    }

    public boolean verifyCurrentViewerInfo(ViewerInfo vi) {
        log.trace("verifying current viewer info for viewer: \n" + vi);
        ViewerInfo ci = retrieveCurrentViewerInfo(vi.id);
        boolean result = vi.similar(ci);
        log.debug("viewer info is " + (result? "" : "not") + " current for viewer: \n" + vi + " with current info: \n" + ci);
        return result;
    }

    public List<Long> listAllViewers() {
        List<Long> ids = new ArrayList<>();
        if (con == null) {
            getConnection();
        }
        log.trace("listing all viewers");

        SelectQuery sq = new SelectQuery()
                .select("id")
                .from("viewers");
        ActiveResult ar = SQLUtil.select(con, sq);
        try {
            while(ar.rs.next()) {
                ids.add(ar.rs.getLong("id"));
            }
        } catch(SQLException e) {
            SQLUtil.SQLExHandle(e,"SQLException while selecting all viewer ids");
        } finally { ar.close(); }

        log.debug("all viewers list: " + ids);
        return ids;
    }

    public int findLatestSnapshot(int vid) {
        if (con == null) {
            getConnection();
        }
        log.trace("searching for id of latest snapshot for viewer with id: " + vid);

        SelectQuery sq = new SelectQuery()
                .select("latestSnapshot")
                .from("viewers")
                .where(SQLUtil.qpl(DataType.INT, "id", vid));

        int sid = -1;
        ActiveResult ar = SQLUtil.select(con, sq);
        try {
            if (!ar.rs.next()) {
                log.warn("unable to find viewer latestSnapshot for viewer with id: " + vid);
                return -1;
            }
            sid = ar.rs.getInt("latestSnapshot");
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e,"SQLException while searching for viewer latest snapshot: " + vid);
        } finally { ar.close(); }

        log.debug("id of latest snapshot is "+ sid + " for viewer with id " + vid);
        return sid;
    }

    public Parameter findLastSnapshotValue(int vid, Parameter toFind) {
        if (con == null) {
            getConnection();
        }
        log.trace("finding last value of " + toFind + " for viewer " + vid + " in snapshot records.");

        Object value = null;
        if(toFind.type == DataType.INT) value = ViewerInfo.INT_DEFAULT;
        if(toFind.type == DataType.STRING) value = ViewerInfo.STRING_DEFAULT;

        SelectQuery sq = new SelectQuery()
                .select(SQLUtil.qpl(toFind))
                .from("vsnapshots")
                .where(SQLUtil.qpl(DataType.INT,"vid",vid))
                .orderBy("id").desc();
        ActiveResult ar = SQLUtil.select(con, sq);
        try {
            while(ar.rs.next()) {
                if(toFind.type == DataType.INT) {
                    long result = ar.rs.getLong(toFind.column);
                    if(result == ViewerInfo.INT_DEFAULT) {
                        continue;
                    } else {
                        value = result;
                        break;
                    }
                } else if(toFind.type == DataType.STRING) {
                    String result = ar.rs.getString(toFind.column);
                    log.debug("lastValue contender: ", result);
                    if(result == null || result.equals(ViewerInfo.STRING_DEFAULT)) {
                        continue;
                    } else {
                        if(toFind.column == "roles") { //special case due to serialization, if this becomes regular need to add DataType.SERIALIZED
                            if(result.equals("[]")) continue;
                        } else if(toFind.column == "channelId") { //special case due to for some reason API returning int values
                            if(result.equals(String.valueOf(ViewerInfo.INT_DEFAULT))) continue;
                        }
                        value = result;
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e, "SQLException while finding last value of " + toFind + " for viewer " + vid + " in snapshot records.");
        } finally { ar.close(); }

        Parameter result = new Parameter(toFind.type, toFind.column, value);
        log.debug("last value of " + toFind + " for viewer " + vid + " in snapshot records: \n" + result);
        return result;
    }
}