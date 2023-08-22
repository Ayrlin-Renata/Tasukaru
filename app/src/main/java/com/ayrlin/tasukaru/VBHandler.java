package com.ayrlin.tasukaru;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.ayrlin.sqlutil.SQLUtil;
import com.ayrlin.sqlutil.query.Parameter;
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
        } catch (Exception e) {
            e.printStackTrace();
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
                String readVersion = versionCheckResult.getString("version"); 
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

    private void getConnection() {
        // sqlite driver
        try {
            Class.forName("org.sqlite.JDBC");
            // database path, if it's new database, it will be created in the project folder
            // con =
            // DriverManager.getConnection("jdbc:sqlite:../plugins/Tasukaru/ViewerBase.db");
            String conPath = "jdbc:sqlite://" + tDir + "ViewerBase.db";
            log.debug("Attempting to connect to DB at: " + conPath);
            con = DriverManager.getConnection(conPath);
            initialise();
        } catch (ClassNotFoundException e) {
            log.severe("couldnt find JDBC");
            e.printStackTrace();
        } catch (SQLException e) {
            log.severe("couldnt connect to the viewerbase DB");
            e.printStackTrace();
        }
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

        if(!SQLUtil.insert(con, "viewers", params)) {
            log.severe("failed to add viewer: \n" + vi);
            return -1;
        } 

        //add snapshot after viewer for foreign key
        int vid = SQLUtil.retrieveLastInsertId(con);
        vi.latestSnapshot = addViewerSnapshot(vi.id(vid));

        //update new viewer entry to have latest snapshot id
        List<Parameter> sParams = new ArrayList<>();
        sParams.add(new Parameter(DataType.INT, "latestSnapshot", vi.latestSnapshot));
        List<Parameter> wParams = new ArrayList<>();
        wParams.add(new Parameter(DataType.INT, "id", vi.id));

        if(!SQLUtil.update(con, "viewers", sParams, wParams)) {
            log.severe("failed to update viewer latestSnapshot while adding viewer: \n" + vi);
            return -1;
        } 

        return vid;
    }

    public void updateViewer(ViewerInfo vi) {
        if (con == null) {
            getConnection();
        }
        log.debug("Updating viewer info: " + vi);
        
        vi.latestSnapshot = addViewerSnapshot(vi);
        
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
            return;
        } 
    }

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

        if(!SQLUtil.insert(con, "vsnapshots", params)) {
            log.severe("Failed to add viewer snapshot for viewer: \n" + vi);
            return -1;
        }

        EventInfo ei = new EventInfo()
                .viewer(vi)
                .snapshotId(SQLUtil.retrieveLastInsertId(con))
                .uptype("technical")
                .action("vsnapshot");
        addHistory(ei);

        return SQLUtil.retrieveLastInsertId(con);
    }

    public void addHistory(EventInfo ei) {
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

        if(!SQLUtil.insert(con, "history", params)) {
            log.severe("failed to addHistory() for event: \n" + ei);
        }
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

        int viewerId;
        String query = "SELECT `id` FROM `viewers` WHERE ";
        String param1 = "";
        String param2 = "";
        byte secondParam = 0;

        // determine most reliable info
        if (!vi.UPID.isEmpty()) {
            query += "`UPID` == ?";
            param1 = vi.UPID;
        } else if (!vi.platform.isEmpty()) {
            query += "`platform` == ? AND ";
            param1 = vi.platform;
            secondParam++;
            if (!vi.userId.isEmpty()) {
                query += "`userId` == ?";
                param2 = vi.userId;
            } else if (!vi.username.isEmpty()) {
                query = "`username` == ?";
                param2 = vi.username;
            } else if (!vi.link.isEmpty()) {
                query = "`link` == ?";
                param2 = vi.link;
            } else if (!vi.displayname.isEmpty()) {
                query = "`displayname` == ?";
                param2 = vi.displayname;
            } else {
                query = "ABORT SQL QUERY";
            }
        } else {
            query = "ABORT SQL QUERY";
        }

        if (query == "ABORT SQL QUERY") {
            log.warn("abort finding viewer: \n" + vi);
            return -2;
        }

        try {
            PreparedStatement prep = con.prepareStatement(query);
            prep.setString(1, param1);
            if (secondParam == 1)
                prep.setString(2, param2);
            ResultSet res = prep.executeQuery();
            if (!res.next()) {
                log.warn("unable to find viewer: \n" + vi);
                return -1;
            }
            viewerId = res.getInt("id");
        } catch (SQLException e) {
            log.severe("SQLException while finding viewer: \n" + vi + "\nusing SQL: \n" + query + "\n and parameter(s): "
                    + param1 + ((secondParam == 1) ? " and " + param2 : ""));
            e.printStackTrace();
            return -2;
        }

        vi.id(viewerId);
        log.trace("found viewer id " + viewerId + " for viewer: \n" + vi);
        return viewerId;
    }

    public boolean verifyCurrentViewerInfo(ViewerInfo vi) {
        if (con == null) {
            getConnection();
        }
        log.trace("verifying viewer info is current.");

        boolean isCurrent = true; 
        String query = "SELECT * FROM viewers WHERE id == ?";
        try {
            PreparedStatement prep = con.prepareStatement(query);
            prep.setInt(1, vi.id);
            ResultSet res = prep.executeQuery();
            if (!res.next()) {
                log.warn("unable to find viewer: \n" + vi);
                return false;
            }

            //no userId, diff userId means diff account
            if(!res.getString("channelId").equals(vi.channelId)) { isCurrent = false; }
            //no platform, diff platform and same id etc. means something is weirdchamp
            if(!res.getString("roles").equals(vi.getRoles())) {
                //if this breaks compare lists (unordered)
                log.trace("query roles: \n" + res.getString("roles") + " are not similar to VI roles: \n" + vi.getRoles());
            } 
            if(!res.getString("badges").equals(vi.getBadges())) {
                //if this breaks compare lists (unordered)
                log.trace("query roles: \n" + res.getString("badges") + " are not similar to VI roles: \n" + vi.getBadges());
            }
            if(!res.getString("color").equals(vi.color)) { isCurrent = false; }
            if(!res.getString("username").equals(vi.username)) { isCurrent = false; }
            if(!res.getString("displayname").equals(vi.displayname)) { isCurrent = false; }
            if(!res.getString("bio").equals(vi.bio)) { isCurrent = false; }
            if(!res.getString("link").equals(vi.link)) { isCurrent = false; }
            if(!res.getString("imageLink").equals(vi.imageLink)) { isCurrent = false; }
            if(res.getInt("followersCount") != vi.followersCount) { isCurrent = false; }
            if(res.getInt("subCount") != vi.subCount) { isCurrent = false; }
            //no user data like watchtime/tskrpoints
            
        } catch (SQLException e) {
            log.severe("SQLException while verifying viewer info: \n" + vi);
            e.printStackTrace();
        }
        return isCurrent;
    }

    public int findLatestSnapshot(int vid) {
        if (con == null) {
            getConnection();
        }
        log.trace("searching for id of latest snapshot for viewer with id: " + vid);

        int sid = -1;
        String query = "SELECT latestSnapshot FROM `viewers` WHERE id = ?";
        try {
            PreparedStatement prep = con.prepareStatement(query);
            prep.setInt(1, vid);
            ResultSet res = prep.executeQuery();
            if (!res.next()) {
                log.warn("unable to find viewer latestSnapshot for viewer with id: " + vid);
                return -1;
            }
            sid = res.getInt("latestSnapshot");
        } catch (SQLException e) {
            log.severe("SQLException while searching for viewer latest snapshot: " + vid);
            e.printStackTrace();
        }
        return sid;
    }
}