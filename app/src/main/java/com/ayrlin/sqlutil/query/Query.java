package com.ayrlin.sqlutil.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface Query {

    public String getQueryString();
    public boolean isReady();
    
    public PreparedStatement prepare(Connection con) throws SQLException;
}
