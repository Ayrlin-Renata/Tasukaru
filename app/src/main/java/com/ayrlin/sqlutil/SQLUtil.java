package com.ayrlin.sqlutil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.ayrlin.sqlutil.query.*;
import com.ayrlin.sqlutil.query.Parameter.DataType;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;


public class SQLUtil {

    /**
     * Remember to close() any ActiveResult
     * @param con
     * @param columns
     * @param table
     * @param matchConditions
     * @return
     */
    public static ActiveResult select(Connection con, List<Parameter> columns, String table, List<Parameter> matchConditions) {
        SelectQuery q = new SelectQuery().select(columns).from(table).where(matchConditions);
        return select(con, q);
    }
    public static ActiveResult select(Connection con, SelectQuery q) {
        if(!q.isReady()) {
            FastLogger.logStatic(LogLevel.SEVERE,"query is unexpectedly not ready: \n" + q);
            return null;
        }
        try {
            PreparedStatement prep = q.prepare(con);
            ResultSet res = prep.executeQuery();
            FastLogger.logStatic(LogLevel.TRACE, "SELECT statement executed.");
            return new ActiveResult(con, prep, res);
        } catch (SQLException e) {
            SQLExHandle(e, "failed to execute SQL query: \n" + q.getQueryString());
            return null;
        }
    }

    /**
     * 
     * @param con
     * @param table
     * @param params
     * @return generated keys, -1 if failed to execute, -2 if query unsuccessful, -3 if key unable to be retrieved.
     */
    public static long insert(Connection con, String table, List<Parameter> params) {
        InsertQuery q = new InsertQuery().into(table).values(params);
        if(!q.isReady()) {
            FastLogger.logStatic(LogLevel.SEVERE,"query is unexpectedly not ready: \n" + q);
            return -1;
        }
        int matched, rows;
        long key;
        try (PreparedStatement prep = q.prepare(con)) {
            matched = prep.executeUpdate();
            rows = prep.getUpdateCount(); 
            try (ResultSet gk = prep.getGeneratedKeys()) {
                key = -1; 
                if(gk.next()) {
                    key = gk.getLong(1);
                }
            } catch (SQLException e) {
                SQLExHandle(e, "failed to retrieve generated keys SQL query: \n" + q.getQueryString());
                return -1;
            }
        } catch (SQLException e) {
            SQLExHandle(e, "failed to execute SQL query: \n" + q.getQueryString());
            return -1;
        }
        
        FastLogger.logStatic(LogLevel.DEBUG, "INSERT statement (key " + key + ") executed, matching " + matched + " rows and impacting " + rows + " rows.");
        if(matched <= 0) return -2; //successful insert returns matched 1 rows 0 for weird reasons
        if(key < 0) {
            int lid = retrieveLastInsertId(con);
            if(lid < 0) return -3;
            key = lid;
        }
        return key;
    }

    public static boolean update(Connection con, String table, List<Parameter> setValues, List<Parameter> matchConditions) {
        UpdateQuery q = new UpdateQuery().update(table).set(setValues).where(matchConditions);
        if(!sanityCheck(con, table, matchConditions)) {
            FastLogger.logStatic(LogLevel.SEVERE,"cannot proceed with UPDATE query: \n" + q);
            return false;
        }
        if(!q.isReady()) {
            FastLogger.logStatic(LogLevel.SEVERE,"query is unexpectedly not ready: \n" + q);
            return false;
        }
        int matched, rows;
        try (PreparedStatement prep = q.prepare(con);) {
            matched = prep.executeUpdate();
            rows = prep.getUpdateCount(); 
        } catch (SQLException e) {
            SQLExHandle(e, "failed to execute SQL query: \n" + q.getQueryString());
            return false;
        }
        FastLogger.logStatic(LogLevel.TRACE, "UPDATE statement executed, matching " + matched + " rows and impacting " + rows + " rows.");
        if(matched <= 0) return false; //successful update returns matched 1 rows 0 for weird reasons
        return true;
    }

    public static boolean sanityCheck(Connection con, String table, List<Parameter> matchConditions) {
        List<Parameter> all = retrieveColumnNames(con, table);
        SelectQuery q = new SelectQuery().select(all).from(table).where(matchConditions);
        ActiveResult ar = select(con, q);
        try {
            if(ar.hasNull() || !ar.rs.next()) {
                FastLogger.logStatic(LogLevel.WARNING, "sanity check failed for query: \n" + q);
                return false;
            }
            FastLogger.logStatic(LogLevel.TRACE, "Sanity check query ResultSet.getString(1): \n" + ar.rs.getString(1));
        } catch(SQLException e) {
            SQLExHandle(e, "SQLException during sanity check for query: \n" + q);
            return false;
        } finally {
            ar.close();
        }
        return true;
    }

    /**
     * DO NOT CALL THIS MORE THAN ONCE PER PREPAREDSTATEMENT
     * THE SET INDEX RESETS EVERY TIME
     * append your param lists in correct order first instead 
     * @param prep
     * @param params
     * @return
     * @throws SQLException
     */
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
            FastLogger.logStatic(LogLevel.TRACE, "Prepared Parameter " + i + ": " + p);
        }
        return prep;
    }

    /**
     * Quick Param List
     * just makes a list out of data for one param for shorthand
     * @param type
     * @param col
     * @param value
     * @return an ArrayList containing one param built as specified
     */
    public static List<Parameter> qpl(DataType type, String col, Object value) {
        return qpl(new Parameter(type, col, value));
    }
    public static List<Parameter> qpl(Parameter p) {
        List<Parameter> qpl = new ArrayList<>();
        qpl.add(p);
        return qpl;
    }

    /**
     * CREATE STATEMENT WITH Statement.RETURN_GENERATED_KEYS INSTEAD OF THIS IF POSSIBLE
     * @return last insert primary key if successful,
     *         -1 if error
     */
    public static int retrieveLastInsertId(Connection con) {
        String query = "SELECT last_insert_rowid();";
        int lid;
        try {
            PreparedStatement prep = con.prepareStatement(query);
            lid = prep.executeQuery().getInt("last_insert_rowid()");
            prep.close();
        } catch (SQLException e) {
            SQLExHandle(e, "SQLException while retrieving last insert ID.");
            return -1;
        }
        FastLogger.logStatic(LogLevel.TRACE,"Last Insert ID Retrieved: " + lid);
        return lid;
    }

    public static List<Parameter> retrieveColumnNames(Connection con, String table) {
        ArrayList<Parameter> name = new ArrayList<>();
        name.add(new Parameter(DataType.STRING,"name",""));
        SelectQuery q = new SelectQuery().select(name).from("pragma_table_info('" + table + "')");
        ActiveResult ar = select(con, q); 
        if(ar.hasNull()) {
            FastLogger.logStatic(LogLevel.SEVERE, "cannot retrieve column names for table " + table);
            return null;
        }
        List<Parameter> cols = new ArrayList<>();
        try {
            while (ar.rs.next()) {
                cols.add(new Parameter(DataType.STRING,ar.rs.getString("name"),""));
            }
            ar.close();
        } catch(SQLException e) {
            SQLExHandle(e, "SQLException while retrieving column names for table: " + table);
            return null;
        }
        if(cols.isEmpty()) {
            FastLogger.logStatic(LogLevel.WARNING, "column names for table " + table + " appears to be empty, silently continuing");
        }
        FastLogger.logStatic(LogLevel.TRACE, "column names for table " + table + ": \n" + cols);
        return cols;
    }

    /**
     * logs more verbose error details
     * @param e
     * @param string
     */
    public static void SQLExHandle(SQLException e, String errormessage) {
        FastLogger.logStatic(LogLevel.SEVERE, errormessage);
        FastLogger.logStatic(LogLevel.SEVERE, "SQLState: " + e.getSQLState());
        FastLogger.logStatic(LogLevel.SEVERE, "Error Code: " + e.getErrorCode());
        FastLogger.logStatic(LogLevel.SEVERE, "Error Message: " + e.getLocalizedMessage());
        e.printStackTrace();
    }
}
