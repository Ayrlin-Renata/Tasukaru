package com.ayrlin.sqlutil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class ActiveResult {
    public Connection con;
    public PreparedStatement stmt;
    public ResultSet rs;

    public ActiveResult(Connection con, PreparedStatement stmt, ResultSet rs) {
        this.con = con;
        this.stmt = stmt;
        this.rs = rs;
    }

    public boolean hasNull() {
        return con == null || stmt == null || rs == null;
    }

    public void close() {
        try { 
            if(rs != null) rs.close(); 
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Exception while closing ResultSet in ActiveResult.");
            e.printStackTrace();
        };
        try { 
            if(stmt != null) stmt.close(); 
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Exception while closing PreparedStatement in ActiveResult.");
            e.printStackTrace();
        };
    }
}
