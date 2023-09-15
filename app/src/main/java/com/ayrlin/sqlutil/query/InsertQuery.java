package com.ayrlin.sqlutil.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ayrlin.sqlutil.SQLUtil;

import lombok.ToString;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@ToString
public class InsertQuery implements Query {
    public String into;
    public List<Parameter> params; 

    public InsertQuery() {
        params = new ArrayList<Parameter>();
    }

    public InsertQuery into(String into) {
        this.into = into;
        return this;
    }

    public InsertQuery values(List<Parameter> params) {
        this.params.addAll(params);
        return this;
    }

    @Override
    public boolean isReady() {
        boolean built = true;
        if(into.isEmpty()) { built = false; }
        if(params.isEmpty()) { built = false; }
        return built;
    }

    @Override
    public String getQueryString() {
        Stream<Parameter> pStream = params.stream();
        String columns = String.join(", ", pStream.map(Parameter::getColumn).collect(Collectors.toList()));
        
        //String values = String.join(", ", pStream.map(Parameter::getValue).collect(Collectors.toList()));
        List<String> qMarks = new ArrayList<String>(); 
        for (int i = 0; i < params.size(); i++) {
            qMarks.add("?");
        }
        String values = String.join(", ", qMarks);

        return "INSERT INTO `" + into + "` ( " + columns + " ) VALUES ( " + values + " );"; 
    }

    @Override
    public PreparedStatement prepare(Connection con) throws SQLException {
        String query = getQueryString();
        PreparedStatement prep = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        prep = SQLUtil.prepDataTypes(prep, params);
        FastLogger.logStatic(LogLevel.TRACE, "Prepared SQL INSERT query: \n" + query + "\n VALUES params: \n" + params);
        return prep;
    }
}
