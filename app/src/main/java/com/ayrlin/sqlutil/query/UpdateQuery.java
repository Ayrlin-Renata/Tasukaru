package com.ayrlin.sqlutil.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ayrlin.sqlutil.SQLUtil;
import com.ayrlin.sqlutil.query.data.OpParam;
import com.ayrlin.sqlutil.query.data.Param;

import lombok.ToString;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@ToString
public class UpdateQuery implements Query {
    public String update;
    public List<Param> set; 
    public List<OpParam> where; 

    public UpdateQuery() {
        set = new ArrayList<>();
        where = new ArrayList<>();
    }

    public UpdateQuery update(String table) {
        this.update = table;
        return this;
    }

    public UpdateQuery set(List<Param> params) {
        set.addAll(params);
        return this;
    }

    public UpdateQuery where(List<OpParam> match) {
        where.addAll(match);
        return this;
    }

    @Override
    public boolean isReady() {
        if(update.isEmpty()) return false;
        if(set.isEmpty()) return false;
        if(where.isEmpty()) return false;
        return true;
    }

    @Override
    public String getQueryString() {
        List<String> setList = new ArrayList<>();
        for(Param p : set) {
            setList.add("\"" + p.getColumn() + "\" = ?");
        }
        String setString = String.join(", ",setList);

        List<String> whereList = new ArrayList<>();
        for(OpParam p : where) {
            whereList.add("\"" + p.getColumn() + "\" " + p.operation.toString() + " ?");
        }
        String whereString = String.join(" AND ",whereList);

        return "UPDATE `" + update + "` SET " + setString + " WHERE " + whereString + ";"; 
    }

    @Override
    public PreparedStatement prepare(Connection con) throws SQLException {
        String query = getQueryString();
        PreparedStatement prep = con.prepareStatement(query);
        prep = SQLUtil.prepDataTypes(prep, Stream.concat(set.stream(), where.stream()).collect(Collectors.toList()));
        FastLogger.logStatic(LogLevel.TRACE, "Prepared SQL UPDATE query: \n" + query + "\n SET params: \n" + set + "\n WHERE params: \n" + where);
        return prep;
    }

    @Override
    public Boolean execute(Connection con) {
        if(!SQLUtil.sanityCheck(con, update, where)) {
            FastLogger.logStatic(LogLevel.SEVERE,"cannot proceed with UPDATE query: \n" + this);
            return false;
        }
        if(!isReady()) {
            FastLogger.logStatic(LogLevel.SEVERE,"query is unexpectedly not ready: \n" + this);
            return false;
        }
        int matched, rows;
        try (PreparedStatement prep = prepare(con);) {
            matched = prep.executeUpdate();
            rows = prep.getUpdateCount(); 
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e, "failed to execute SQL query: \n" + getQueryString());
            return false;
        }
        FastLogger.logStatic(LogLevel.TRACE, "UPDATE statement executed, matching " + matched + " rows and impacting " + rows + " rows.");
        if(matched <= 0) return false; //successful update returns matched 1 rows 0 for weird reasons
        return true;
    }
    
}
