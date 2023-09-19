package com.ayrlin.sqlutil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.ayrlin.sqlutil.query.*;
import com.ayrlin.sqlutil.query.data.*;
import com.ayrlin.sqlutil.query.data.OpParam.Op;

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
    public static ActiveResult select(Connection con, List<Param> columns, String table, List<OpParam> matchConditions) {
        return new SelectQuery().select(columns).from(table).where(matchConditions).execute(con);
    }

    /**
     * 
     * @param con
     * @param table
     * @param params
     * @return generated keys, -1 if failed to execute, -2 if query unsuccessful, -3 if key unable to be retrieved.
     */
    public static long insert(Connection con, String table, List<Param> params) {
        return new InsertQuery().into(table).values(params).execute(con);
    }

    /**
     * 
     * @param con
     * @param table
     * @param setValues
     * @param matchConditions
     * @return true if it worked, false otherwise
     */
    public static boolean update(Connection con, String table, List<Param> setValues, List<OpParam> matchConditions) {
        return new UpdateQuery().update(table).set(setValues).where(matchConditions).execute(con);
    }

    /**
     * for making the WHERE part of a query actually returns results
     * @param con
     * @param table
     * @param matchConditions
     * @return true if it does (ResultSet.next() is true)
     */
    public static boolean sanityCheck(Connection con, String table, List<OpParam> matchConditions) {
        List<Param> all = retrieveColumnNames(con, table);
        SelectQuery q = new SelectQuery().select(all).from(table).where(matchConditions);
        ActiveResult ar = q.execute(con);
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
    public static PreparedStatement prepDataTypes(PreparedStatement prep, List<Param> params) throws SQLException {
        for(int i = 0; i < params.size(); i++) {
            Param p = params.get(i);
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

    public static List<Param> opToParam(List<OpParam> opParam) {
        List<Param> param = new ArrayList<>();
        param.addAll(opParam);
        return param;
    }

    /**
     * Quick Param List
     * just makes a list out of data for one param for shorthand
     * @param type
     * @param col
     * @param value
     * @return an ArrayList containing one param built as specified
     */
    public static List<Param> qpl(DataType type, String col, Object value) {
        return qpl(new Param(type, col, value));
    }
    public static List<Param> qpl(Param p) {
        List<Param> qpl = new ArrayList<>();
        qpl.add(p);
        return qpl;
    }

    /**
     * Quick OpParam List
     * just makes a list out of data for one param for shorthand
     * @param type
     * @param col
     * @param op
     * @param value
     * @return an ArrayList containing one param built as specified
     */
    public static List<OpParam> qol(DataType type, String col, Op op, Object value) {
        return qol(new OpParam(type, col, op, value));
    }
    public static List<OpParam> qol(OpParam p) {
        List<OpParam> qol = new ArrayList<>();
        qol.add(p);
        return qol;
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

    public static List<Param> retrieveColumnNames(Connection con, String table) {
        ArrayList<Param> name = new ArrayList<>();
        name.add(new Param(DataType.STRING,"name",""));
        SelectQuery q = new SelectQuery().select(name).from("pragma_table_info('" + table + "')");
        ActiveResult ar = q.execute(con); 
        if(ar.hasNull()) {
            FastLogger.logStatic(LogLevel.SEVERE, "cannot retrieve column names for table " + table);
            return null;
        }
        List<Param> cols = new ArrayList<>();
        try {
            while (ar.rs.next()) {
                cols.add(new Param(DataType.STRING,ar.rs.getString("name"),""));
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
