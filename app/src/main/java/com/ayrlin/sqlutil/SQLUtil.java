package com.ayrlin.sqlutil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.ayrlin.sqlutil.query.InsertIntoQuery;
import com.ayrlin.sqlutil.query.SelectQuery;
import com.ayrlin.sqlutil.query.UpdateQuery;
import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.OpParam;
import com.ayrlin.sqlutil.query.data.OpParam.Op;
import com.ayrlin.sqlutil.query.data.Param;
import com.ayrlin.sqlutil.query.data.SCol;

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
        return new InsertIntoQuery().insertInto(table).values(params).execute(con);
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

    public static boolean existsCheck(Connection con, String table) {
        FastLogger.logStatic(LogLevel.TRACE, "checking master table for table " + table + " existance");
        List<OpParam> match = new ArrayList<>();
        match.add(new OpParam(DataType.STRING, "type", Op.EQUAL, "table"));
        match.add(new OpParam(DataType.STRING, "name", Op.EQUAL, table));

        SelectQuery q = new SelectQuery().select("name").from("sqlite_master").where(match).limit(1);
        ActiveResult ar = q.execute(con);
        try {
            if(ar.hasNull() || !ar.rs.next()) {
                FastLogger.logStatic(LogLevel.WARNING, "exists check failed for query: \n" + q);
                return false;
            }
            FastLogger.logStatic(LogLevel.DEBUG, "Exists check for table " + table + " success!");
        } catch(SQLException e) {
            SQLExHandle(e, "SQLException during sanity check for query: \n" + q);
            return false;
        } finally {
            ar.close();
        }
        return true;
    }

    /**
     * for making the WHERE part of a query actually returns results
     * @param con
     * @param table
     * @param matchConditions
     * @return true if it does (ResultSet.next() is true)
     */
    public static boolean sanityCheck(Connection con, String table, List<OpParam> matchConditions) {
        //List<Param> all = retrieveColumnNames(con, table);
        SelectQuery q = new SelectQuery().select("*").from(table).where(matchConditions).limit(1);
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
                    if (p.value instanceof String) {
                        prep.setString(i + 1, p.value.toString()); //+1 because prepared statement indexes are 1-based
                    } else {
                        throw new IllegalArgumentException("prepDataTypes() passed non-String value: " + p.value);
                    }
                    break;
                case LONG : 
                    if (p.value instanceof Number) {
                        prep.setLong(i + 1, ((Number) p.value).longValue());
                    } else {
                        throw new IllegalArgumentException("prepDataTypes() passed non-Long value: " + p.value);
                    }
                    break;
                case TIMESTAMP : 
                    if (p.value instanceof Timestamp) {
                        prep.setTimestamp(i + 1, (Timestamp) p.value);    
                    } else {
                        throw new IllegalArgumentException("prepDataTypes() passed non-Timestamp value: " + p.value);
                    }
                    break;
                case BOOL:
                    if (p.value instanceof Boolean) {
                        prep.setBoolean(i + 1, (boolean) p.value);
                    } else {
                        throw new IllegalArgumentException("prepDataTypes() passed non-Boolean value: " + p.value);
                    }
                    break;
                case DOUBLE:
                    if (p.value instanceof Number) {
                        prep.setDouble(i + 1, ((Number) p.value).doubleValue());
                    } else {
                        throw new IllegalArgumentException("prepDataTypes() passed non-Double value: " + p.value);
                    }
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
    public static long retrieveLastInsertId(Connection con) {
        String query = "SELECT last_insert_rowid();";
        long lid;
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

    public static List<SCol> retrieveColumnNames(Connection con, String table) {
        SelectQuery q = new SelectQuery().select("name").from("pragma_table_info('" + table + "')");
        ActiveResult ar = q.execute(con); 
        if(ar.hasNull()) {
            FastLogger.logStatic(LogLevel.SEVERE, "cannot retrieve column names for table " + table);
            return null;
        }
        List<SCol> cols = new ArrayList<>();
        try {
            while (ar.rs.next()) {
                cols.add(new SCol(ar.rs.getString("name"), DataType.STRING));
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
