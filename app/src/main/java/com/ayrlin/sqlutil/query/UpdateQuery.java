package com.ayrlin.sqlutil.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ayrlin.sqlutil.SQLUtil;

import lombok.ToString;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@ToString
public class UpdateQuery implements Query {
    public String update;
    public List<Parameter> set; 
    public List<Parameter> where; 

    public UpdateQuery() {
        set = new ArrayList<Parameter>();
        where = new ArrayList<Parameter>();
    }

    public UpdateQuery update(String table) {
        this.update = table;
        return this;
    }

    public UpdateQuery set(List<Parameter> params) {
        set.addAll(params);
        return this;
    }

    public UpdateQuery where(List<Parameter> match) {
        where.addAll(match);
        return this;
    }

    @Override
    public boolean isReady() {
        boolean built = true;
        if(update.isEmpty()) { built = false; }
        if(set.isEmpty()) { built = false; }
        if(where.isEmpty()) { built = false; }
        return built;
    }

    @Override
    public String getQueryString() {
        List<String> setList = new ArrayList<>();
        for(Parameter p : set) {
            setList.add("\"" + p.getColumn() + "\" = ?");
        }
        String setString = String.join(", ",setList);

        List<String> whereList = new ArrayList<>();
        for(Parameter p : where) {
            whereList.add("\"" + p.getColumn() + "\" = ?");
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
    
}
