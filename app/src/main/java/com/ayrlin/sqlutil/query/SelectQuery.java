package com.ayrlin.sqlutil.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ayrlin.sqlutil.SQLUtil;

import lombok.ToString;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@ToString
public class SelectQuery implements Query {
    public List<Parameter> select; 
    public String from;
    public List<Parameter> where;
    public String selectString;
    public String orderBy;
    public boolean asc;

    public SelectQuery() {
        select = new ArrayList<Parameter>();
        where = new ArrayList<Parameter>();
        selectString = "";
        orderBy = "";
        asc = true;
    }

    public SelectQuery select(String s) {
        this.selectString = s;
        return this;
    }

    public SelectQuery select(List<Parameter> cols) {
        this.select.addAll(cols);
        return this;
    }

    public SelectQuery from(String table) {
        this.from = table;
        return this;
    }

    public SelectQuery where(List<Parameter> match) {
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
        boolean built = true;
        if(select.isEmpty() && selectString.isEmpty()) { built = false; }
        if(from.isEmpty()) { built = false; }
        //if(where.isEmpty()) { built = false; }
        return built;
    }
    
    @Override
    public String getQueryString() {
        if(selectString.isEmpty()) {
            List<String> selectList = new ArrayList<>();
            for(Parameter p : select) {
                selectList.add(p.getColumn());
            }
            selectString = String.join(", ",selectList);
        }
        
        String qs = "SELECT " + selectString + " FROM " + from;
        if(!where.isEmpty()) { 
            List<String> whereList = new ArrayList<>();
            for(Parameter p : where) {
                whereList.add(p.getColumn() + " = ?");
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
        if(!where.isEmpty()) { prep = SQLUtil.prepDataTypes(prep, where); }
        FastLogger.logStatic(LogLevel.TRACE, "Prepared SQL SELECT query: \n" + query + "\n SELECT params: \n" + select + "\n WHERE params: \n" + where);
        return prep;
    }
}
