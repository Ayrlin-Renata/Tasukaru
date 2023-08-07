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
                        + "platform TEXT,"
                        + "watchtime INTEGER," // in seconds, would take ~130 years to go over 32 bit
                        + "tskrpoints INTEGER"
                        + ");");

                // inserting some sample data
                // PreparedStatement prep = con.prepareStatement("insert into user
                // values(?,?,?);");
                // prep.setString(2, "first1");
                // prep.setString(3, "last1");
                // prep.execute();

                // PreparedStatement prep2 = con.prepareStatement("insert into user
                // values(?,?,?);");
                // prep2.setString(2, "first2");
                // prep2.setString(3, "last2");
                // prep2.execute();
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
                        + "FOREIGN KEY(userid) REFERENCES viewers(id)"
                        + ");");
            }
        }
    }

    private void getConnection() throws ClassNotFoundException, SQLException {
        // sqlite driver
        Class.forName("org.sqlite.JDBC");
        // database path, if it's new database, it will be created in the project folder
        // con =
        // DriverManager.getConnection("jdbc:sqlite:../plugins/Tasukaru/ViewerBase.db");
        String conPath = "jdbc:sqlite://" + tDir + "ViewerBase.db";
        log.debug("Attempting to connect to DB at: " + conPath);
        con = DriverManager.getConnection(conPath);
        initialise();
    }

    ///////////////// VB ACTIONS //////////////////

    public ResultSet displayUsers() throws SQLException, ClassNotFoundException {
        if (con == null) {
            // get connection
            getConnection();
        }
        Statement state = con.createStatement();
        ResultSet res = state.executeQuery("select fname, lname from user");
        return res;
    }

    public void addUser(String firstname, String lastname) throws ClassNotFoundException, SQLException {
        if (con == null) {
            // get connection
            getConnection();
        }
        PreparedStatement prep = con
                .prepareStatement("insert into user values(?,?,?);");
        prep.setString(2, firstname);
        prep.setString(3, lastname);
        prep.execute();

    }
}