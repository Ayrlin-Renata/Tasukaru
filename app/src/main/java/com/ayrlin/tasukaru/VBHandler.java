package com.ayrlin.tasukaru;

import java.sql.*;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class VBHandler {
    private static Connection con;
    private static boolean hasData = false;

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
        if (!hasData) {
            hasData = true;
            // check for database table
            ResultSet viewerRes = con.createStatement()
                    .executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='viewers'");
            if (!viewerRes.next()) {
                log.warn("No Viewer table found, creating new Viewer table.");

                // viewer table definition
                con.createStatement().executeUpdate("create table viewers("
                        + "id INTEGER PRIMARY KEY,"
                        + "username TEXT NOT NULL,"
                        + "displayname TEXT,"
                        + "platuserid TEXT,"
                        + "platform TEXT,"
                        + "watchtime INTEGER," // in seconds, would take ~130 years to go over 32 bit
                        + "tskrpoints INTEGER"
                        + ");");
            }
            ResultSet historyRes = con.createStatement()
                    .executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='history'");
            if (!historyRes.next()) {
                log.warn("No History table found, creating new History table.");

                // history table definition
                con.createStatement().executeUpdate("create table history("
                        + "id INTEGER PRIMARY KEY,"
                        + "userid INTEGER NOT NULL," // foreign key viewers
                        + "uptype TEXT NOT NULL," // viewing, awol, manual
                        + "action TEXT,"
                        + "value INTEGER,"
                        + "timestamp TEXT,"
                        + "FOREIGN KEY(userid) REFERENCES viewers(id)"
                        + ");");
            }
        }
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

    // TODO some kind of viewer info object because this is getting out of hand

    public void addViewer(String username, String platform) {
        addViewer(username, platform, 0, 0);
    }

    public void addViewer(String username, String platform, int watchtime, int tskrpoints) {
        addViewer(username, "", "", platform, watchtime, tskrpoints);
    }

    public void addViewer(String username, String displayname, String platuserid, String platform) {
        addViewer(username, displayname, platuserid, platform, 0, 0);
    }

    public void addViewer(String username, String displayname, String platuserid, String platform, int watchtime,
            int tskrpoints) {
        if (con == null) {
            getConnection();
        }
        try {
            PreparedStatement prep = con
                    .prepareStatement(
                            "insert into viewers(username,displayname,platuserid,platform,watchtime,tskrpoints) values(?,?,?,?);");
            prep.setString(1, username);
            prep.setString(2, platform);
            prep.setInt(3, watchtime);
            prep.setInt(4, tskrpoints);
            prep.execute();
        } catch (SQLException e) {
            log.severe("failed to execute addViewer() SQL.");
            e.printStackTrace();
        }
    }

    public void addHistory(String username, String platform, String uptype, String action, int value) {
        int userId = findUserId(username, platform);
        if (userId < 0) {
            log.severe("unable to add history for user " + username + "on" + platform
                    + ": " + uptype + "," + action + "," + value);
            return;
        }
        addHistory(userId, uptype, action, value);
    }

    public void addHistory(int userid, String uptype, String action, int value) {
        Timestamp now = new java.sql.Timestamp(new java.util.Date().getTime());
        addHistory(userid, uptype, action, value, now);
    }

    public void addHistory(int userid, String uptype, String action, int value, Timestamp timestamp) {
        if (con == null) {
            getConnection();
        }
        try {
            PreparedStatement prep = con
                    .prepareStatement("insert into history(userid,uptype,action,value,timestamp) values(?,?,?,?,?);");
            prep.setInt(1, userid);
            prep.setString(2, uptype);
            prep.setString(3, action);
            prep.setInt(4, value);
            prep.setTimestamp(5, timestamp);
            prep.execute();
        } catch (SQLException e) {
            log.severe("failed to execute addHistory() SQL.");
            e.printStackTrace();
        }
    }

    public int findUserId(String username, String platform) {
        if (con == null) {
            getConnection();
        }
        int userId;
        try {
            PreparedStatement prep = con
                    .prepareStatement("select id from viewers where username == ? and platform == ?");
            prep.setString(1, username);
            prep.setString(2, platform);
            ResultSet res = prep.executeQuery();
            if (!res.next()) {
                log.warn("unable to find user: " + username + " on " + platform);
                return -1;
            }
            userId = res.getInt("id");
        } catch (SQLException e) {
            log.severe("SQLException while finding user: " + username + " on " + platform);
            e.printStackTrace();
            return -1;
        }
        log.trace("found userId " + userId + " for player " + username + " on " + platform);
        return userId;
    }
}