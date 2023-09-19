package com.ayrlin.sqlutil.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ayrlin.sqlutil.SQLUtil;
import com.ayrlin.sqlutil.query.data.Param;

import lombok.ToString;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@ToString
public class InsertQuery implements Query {
    public String into;
    public List<Param> params; 

    public InsertQuery() {
        params = new ArrayList<>();
    }

    public InsertQuery into(String into) {
        this.into = into;
        return this;
    }

    public InsertQuery values(List<Param> params) {
        this.params.addAll(params);
        return this;
    }

    @Override
    public boolean isReady() {
        if(into.isEmpty()) return false;
        if(params.isEmpty()) return false;
        return true;
    }

    @Override
    public String getQueryString() {
        Stream<Param> pStream = params.stream();
        String columns = String.join(", ", pStream.map(Param::getColumn).collect(Collectors.toList()));
        
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

    @Override
    public Long execute(Connection con) {
        if(!isReady()) {
            FastLogger.logStatic(LogLevel.SEVERE,"query is unexpectedly not ready: \n" + this);
            return (long) -1;
        }
        int matched, rows;
        long key;
        try (PreparedStatement prep = prepare(con)) {
            matched = prep.executeUpdate();
            rows = prep.getUpdateCount(); 
            try (ResultSet gk = prep.getGeneratedKeys()) {
                key = -1; 
                if(gk.next()) {
                    key = gk.getLong(1);
                }
            } catch (SQLException e) {
                SQLUtil.SQLExHandle(e, "failed to retrieve generated keys SQL query: \n" + getQueryString());
                return (long) -1;
            }
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e, "failed to execute SQL query: \n" + getQueryString());
            return (long) -1;
        }
        
        FastLogger.logStatic(LogLevel.DEBUG, "INSERT statement (key " + key + ") executed, matching " + matched + " rows and impacting " + rows + " rows.");
        if(matched <= 0) return (long) -2; //successful insert returns matched 1 rows 0 for weird reasons
        if(key < 0) {
            int lid = SQLUtil.retrieveLastInsertId(con);
            if(lid < 0) return (long) -3;
            key = lid;
        }
        return key;
    }
}
