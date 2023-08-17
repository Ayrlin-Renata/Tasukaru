package com.ayrlin.sqlutil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.ayrlin.sqlutil.query.*;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;


public class SQLUtil {

    public static boolean insert(Connection con, String table, List<Parameter> params) {
        InsertQuery q = new InsertQuery().into(table).values(params);
        if(!q.isReady()) {
            FastLogger.logStatic(LogLevel.SEVERE,"query is unexpectedly not ready: /n" + q);
        }
        try {
            PreparedStatement prep = q.prepare(con);
            prep.execute();
            return true;
        } catch (SQLException e) {
            FastLogger.logStatic(LogLevel.SEVERE, "failed to execute SQL query: \n" + q.getQueryString());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean update(Connection con, String table, List<Parameter> setValues, List<Parameter> matchConditions) {
        UpdateQuery q = new UpdateQuery().update(table).set(setValues).where(matchConditions);
        if(!q.isReady()) {
            FastLogger.logStatic(LogLevel.SEVERE,"query is unexpectedly not ready: /n" + q);
        }
        try {
            PreparedStatement prep = q.prepare(con);
            prep.execute();
            return true;
        } catch (SQLException e) {
            FastLogger.logStatic(LogLevel.SEVERE, "failed to execute SQL query: \n" + q.getQueryString());
            e.printStackTrace();
            return false;
        }
    }

    public static PreparedStatement prepDataTypes(PreparedStatement prep, List<Parameter> params) throws SQLException {
        for(int i = 0; i < params.size(); i++) {
            Parameter p = params.get(i);
            switch(p.type) {
                case STRING : 
                    prep.setString(i + 1, (String)p.value); //+1 because prepared statement indexes are 1-based
                    break;
                case INT : 
                    prep.setLong(i + 1, Long.valueOf(p.value.toString())); //weird bc it could be an int
                    break;
                case TIMESTAMP : 
                    prep.setTimestamp(i + 1, (Timestamp)p.value);
                    break;
                default: 
                    FastLogger.logStatic(LogLevel.SEVERE, "unrecognized parameter type for Parameter: " + p);
                break;
            }
        }
        return prep;
    }

    /**
     * 
     * @return last insert primary key if successful,
     *         -1 if error
     */
    public static int retrieveLastInsertId(Connection con) {
        String query = "SELECT last_insert_rowid();";
        int lid;
        try {
            PreparedStatement prep = con.prepareStatement(query);
            lid = prep.executeQuery().getInt("last_insert_rowid()");
        } catch (SQLException e) {
            FastLogger.logStatic(LogLevel.SEVERE,"SQLException while retrieving last insert ID.");
            return -1;
        }
        return lid;
    }
}
