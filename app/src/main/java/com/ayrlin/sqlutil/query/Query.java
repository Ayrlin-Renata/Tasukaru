package com.ayrlin.sqlutil.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface Query {

    public boolean isReady();
    public String getQueryString();
    
    public PreparedStatement prepare(Connection con) throws SQLException;
}
