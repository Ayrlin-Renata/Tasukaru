package com.ayrlin.sqlutil.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.ayrlin.sqlutil.SQLUtil;
import com.ayrlin.sqlutil.query.data.Col;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class AlterTableQuery implements Query {
    public String alterTable;
    public Col addColumn;

    public AlterTableQuery() {
        alterTable = "";
        addColumn = null;
    }

    @Override
    public boolean isReady() {
        if(alterTable.isEmpty()) return false;
        if(addColumn == null) return false;
        return true;
    }

    public AlterTableQuery alterTable(String table) {
        this.alterTable = table;
        return this;
    }

    public AlterTableQuery addColumn(Col column) {
        this.addColumn = column;
        return this;
    }

    @Override
    public String getQueryString() {
        String q = "ALTER TABLE `" + alterTable + "`";
        if(addColumn != null) {
            q += " ADD COLUMN " + addColumn.column + " " 
                + addColumn.type.toString()
                + ((addColumn.primaryKey)? " PRIMARY KEY" + ((addColumn.autoIncrement)? " AUTOINCREMENT" : "") : "")
                + ((addColumn.notNull)? " NOT NULL" : "")
                + ((addColumn.unique)? " UNIQUE" : "")
                + ((addColumn.default_ != null)? " DEFAULT" + addColumn.default_.toString() : "")
                + ((!addColumn.references.isEmpty())? " REFERENCES " + addColumn.references : "")
                + ";";
        }
        return q;
    }

    @Override
    public PreparedStatement prepare(Connection con) throws SQLException {
        String query = getQueryString();
        PreparedStatement prep = con.prepareStatement(query);
        FastLogger.logStatic(LogLevel.TRACE, "Prepared SQL ALTER TABLE query: \n" + query);
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
            FastLogger.logStatic(LogLevel.TRACE, "ALTER TABLE statement executed.");
            return true;
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e, "failed to execute SQL query: \n" + getQueryString());
            return false;
        }
    }


    
}
