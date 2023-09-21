package com.ayrlin.sqlutil.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.ayrlin.sqlutil.SQLUtil;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public abstract class SFQuery implements Query {

    @Override
    public PreparedStatement prepare(Connection con) throws SQLException {
        String query = getQueryString();
        PreparedStatement prep = con.prepareStatement(query);
        FastLogger.logStatic(LogLevel.TRACE, "Prepared SQL query: \n" + query);
        return prep;
    }
    
    @Override
    public Boolean execute(Connection con) {
        if(!isReady()) {
            FastLogger.logStatic(LogLevel.SEVERE,"query is unexpectedly not ready: \n" + this);
            return false;
        }
        try {
            PreparedStatement prep = prepare(con);
            prep.execute();
            FastLogger.logStatic(LogLevel.TRACE, "SQL statement executed.");
            return true;
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e, "failed to execute SQL query: \n" + getQueryString());
            return false;
        }
    }
}
