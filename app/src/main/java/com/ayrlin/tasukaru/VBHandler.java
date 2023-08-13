package com.ayrlin.tasukaru;

import java.sql.*;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class VBHandler {
    public static final String vbVersion = "1.0";

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
        con.createStatement().executeUpdate("create table meta("
                + "property TEXT PRIMARY KEY,"
                + "value TEXT"
                + ");");
        con.createStatement().executeUpdate("insert into meta values(\"version\",\"" + vbVersion + "\");");
    }

    private void createViewerSnapshotTable() throws SQLException {
        // vsnapshots table definition
        con.createStatement().executeUpdate("create table vsnapshots("
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
                + "FOREIGN KEY(vid) REFERENCES viewers(id)"
                + ");");
    }

    private void createViewerTable() throws SQLException {
        // viewers table definition
        con.createStatement().executeUpdate("create table viewers(" // TODO implement UPID
                + "id INTEGER PRIMARY KEY,"
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
                + "tskrpoints INTEGER"
                + ");");
    }

    private void createHistoryTable() throws SQLException {
        // history table definition
        con.createStatement().executeUpdate("create table history("
                + "id INTEGER PRIMARY KEY,"
                + "vid INTEGER NOT NULL," // foreign key viewers
                + "sid INTEGER NOT NULL," // foreign key vsnapshots
                + "uptype TEXT NOT NULL," // present, absent, technical
                + "action TEXT,"
                + "value INTEGER,"
                + "timestamp TEXT,"
                + "FOREIGN KEY(vid) REFERENCES viewers(id)"
                + "FOREIGN KEY(sid) REFERENCES vsnapshots(id)"
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

    public void addViewer(ViewerInfo vi) {
        if (con == null) {
            getConnection();
        }
        log.debug("Adding viewer: \n" + vi);

        String query = "INSERT INTO viewers(userId, channelId, platform, UPID, roles, badges, color, username, displayname, bio, link, imageLink, followersCount, subCount, watchtime, tskrpoints) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement prep = con.prepareStatement(query);
            prep.setString(1, vi.userId);
            prep.setString(2, vi.channelId);
            prep.setString(3, vi.platform);
            prep.setString(4, vi.UPID);
            prep.setString(5, vi.getRoles());
            prep.setString(6, vi.getBadges());
            prep.setString(7, vi.color);
            prep.setString(8, vi.username);
            prep.setString(9, vi.displayname);
            prep.setString(10, vi.bio);
            prep.setString(11, vi.link);
            prep.setString(12, vi.imageLink);
            prep.setLong(13, vi.followersCount);
            prep.setLong(14, vi.subCount);
            prep.setLong(15, vi.watchtime);
            prep.setLong(16, vi.tskrpoints);
            prep.execute();
        } catch (SQLException e) {
            log.severe("failed to execute addViewer() SQL query: \n" + query + "\nfor viewer:\n" + vi);
            e.printStackTrace();
            return;
        }

        addViewerSnapshot(vi.id(retrieveLastInsertId()));
    }

    // public void addHistory(String username, String platform, String uptype,
    // String action, int value) {
    // int userId = findUserId(username, platform);
    // if (userId < 0) {
    // log.severe("unable to add history for user " + username + "on" + platform
    // + ": " + uptype + "," + action + "," + value);
    // return;
    // }
    // addHistory(userId, uptype, action, value);
    // }

    // public void addHistory(int userid, String uptype, String action, int value) {
    // Timestamp now = new java.sql.Timestamp(new java.util.Date().getTime());
    // addHistory(userid, uptype, action, value, now);
    // }

    private void addViewerSnapshot(ViewerInfo vi) {
        log.debug("Adding viewer snapshot for viewer: \n" + vi);
        if (vi.id <= 0) {
            int vid = findViewerId(vi);
            if (vid < 0) {
                log.severe("Failed to add viewer snapshot due to lack of id for viewer: \n" + vi);
                return;
            } else {
                vi.id = vid;
            }
        }

        String query = "INSERT INTO vsnapshots(vid, userId, channelId, platform, UPID, roles, badges, color, username, displayname, bio, link, imageLink, followersCount, subCount) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement prep = con.prepareStatement(query);
            prep.setInt(1, vi.id);
            prep.setString(2, vi.userId);
            prep.setString(3, vi.channelId);
            prep.setString(4, vi.platform);
            prep.setString(5, vi.UPID);
            prep.setString(6, vi.getRoles());
            prep.setString(7, vi.getBadges());
            prep.setString(8, vi.color);
            prep.setString(9, vi.username);
            prep.setString(10, vi.displayname);
            prep.setString(11, vi.bio);
            prep.setString(12, vi.link);
            prep.setString(13, vi.imageLink);
            prep.setLong(14, vi.followersCount);
            prep.setLong(15, vi.subCount);
            prep.execute();
        } catch (SQLException e) {
            log.severe("failed to execute addViewer() SQL query: \n" + query + "\nfor viewer:\n" + vi);
            e.printStackTrace();
            return;
        }

        EventInfo ei = new EventInfo()
                .viewer(vi)
                .snapshotId(retrieveLastInsertId())
                .uptype("technical")
                .action("vsnapshot");
        addHistory(ei);
    }

    public void addHistory(EventInfo ei) {
        if (con == null) {
            getConnection();
        }
        if (ei.timestamp == null) {
            ei.timestamp = new java.sql.Timestamp(new java.util.Date().getTime());
        }
        log.debug("Adding history for event: \n" + ei);

        String query = "insert into history(vid,sid,uptype,action,value,timestamp) values(?,?,?,?,?,?);";
        try {
            PreparedStatement prep = con.prepareStatement(query);
            prep.setInt(1, ei.viewer.id);
            prep.setInt(2, ei.snapshotId);
            prep.setString(3, ei.uptype);
            prep.setString(4, ei.action);
            prep.setInt(5, ei.value);
            prep.setTimestamp(6, ei.timestamp);
            prep.execute();
        } catch (SQLException e) {
            log.severe("failed to execute addHistory() SQL for event: \n" + ei);
            e.printStackTrace();
        }
    }

    /**
     * 
     * @return last insert primary key if successful,
     *         -1 if error
     */
    private int retrieveLastInsertId() {
        String query = "SELECT last_insert_rowid();";
        int lid;
        try {
            PreparedStatement prep = con.prepareStatement(query);
            lid = prep.executeQuery().getInt("last_insert_rowid()");
        } catch (SQLException e) {
            log.severe("SQLException while retrieving last insert ID.");
            return -1;
        }
        return lid;
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
            } else if (!vi.channelId.isEmpty()) {
                query = "`channelId` == ?";
                param2 = vi.channelId;
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
            log.severe("SQLException while finding user: \n" + vi + "\nusing SQL: \n" + query + "\n and parameter(s): "
                    + param1 + ((secondParam == 1) ? " and " + param2 : ""));
            e.printStackTrace();
            return -2;
        }

        vi.id(viewerId);
        log.trace("found viewer id " + viewerId + " for viewer: \n" + vi);
        return viewerId;
    }
}