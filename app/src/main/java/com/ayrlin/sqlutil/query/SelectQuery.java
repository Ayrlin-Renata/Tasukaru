package com.ayrlin.sqlutil.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ayrlin.sqlutil.ActiveResult;
import com.ayrlin.sqlutil.SQLUtil;
import com.ayrlin.sqlutil.query.data.OpParam;
import com.ayrlin.sqlutil.query.data.Param;
import com.ayrlin.sqlutil.query.data.SCol;

import lombok.ToString;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@ToString
public class SelectQuery implements Query {
    public List<SCol> select; 
    public String from;
    public List<OpParam> where;
    public String selectString;
    public String orderBy;
    public boolean asc;

    public SelectQuery() {
        select = new ArrayList<>();
        where = new ArrayList<>();
        selectString = "";
        orderBy = "";
        asc = true;
    }

    public SelectQuery select(String s) {
        this.selectString = s;
        return this;
    }

    public SelectQuery select(List<Param> cols) {
        this.select.addAll(cols);
        return this;
    }

    public SelectQuery from(String table) {
        this.from = table;
        return this;
    }

    public SelectQuery where(List<OpParam> match) {
        this.where.addAll(match);
        return this;
    }

    public SelectQuery orderBy(String sort) {
        this.orderBy = sort;
        return this;
    }

    public SelectQuery asc() {
        this.asc = true;
        return this;
    }

    public SelectQuery desc() {
        this.asc = false;
        return this;
    }

    @Override
    public boolean isReady() {
        if(select.isEmpty() && selectString.isEmpty()) return false;
        if(from.isEmpty()) return false;
        return true;
    }
    
    @Override
    public String getQueryString() {
        if(selectString.isEmpty()) {
            List<String> selectList = new ArrayList<>();
            for(SCol p : select) {
                selectList.add(p.getColumn());
            }
            selectString = String.join(", ",selectList);
        }
        
        String qs = "SELECT " + selectString + " FROM " + from;
        if(!where.isEmpty()) { 
            List<String> whereList = new ArrayList<>();
            for(OpParam p : where) {
                whereList.add(p.getColumn() + " " + p.operation.toString() + " ?");
            }
            String whereString = String.join(" AND ",whereList);
            qs += " WHERE " + whereString;
        }
        if(!orderBy.isEmpty()) {
            qs += " ORDER BY " + orderBy + (asc? " ASC" : " DESC");
        }
        return qs + ";";
    }

    @Override
    public PreparedStatement prepare(Connection con) throws SQLException {
        String query = getQueryString();
        PreparedStatement prep = con.prepareStatement(query);
        if(!where.isEmpty()) { prep = SQLUtil.prepDataTypes(prep, SQLUtil.opToParam(where)); }
        FastLogger.logStatic(LogLevel.TRACE, "Prepared SQL SELECT query: \n" + query + "\n SELECT params: \n" + select + "\n WHERE params: \n" + where);
        return prep;
    }

    @Override
    public ActiveResult execute(Connection con) {
        if(!isReady()) {
            FastLogger.logStatic(LogLevel.SEVERE,"query is unexpectedly not ready: \n" + this);
            return null;
        }
        try {
            PreparedStatement prep = prepare(con);
            ResultSet res = prep.executeQuery();
            FastLogger.logStatic(LogLevel.TRACE, "SELECT statement executed.");
            return new ActiveResult(con, prep, res);
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e, "failed to execute SQL query: \n" + getQueryString());
            return null;
        }
    }
}
