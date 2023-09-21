package com.ayrlin.sqlutil.query;

import java.util.ArrayList;
import java.util.List;

import com.ayrlin.sqlutil.query.data.Col;
import com.ayrlin.sqlutil.query.data.Table;

public class CreateTableQuery extends SFQuery {
    private String createTable; 
    private List<Col> cols;

    public CreateTableQuery() {
        createTable = "";
        cols = new ArrayList<>();
    }

    public CreateTableQuery createTable(Table table) {
        createTable = table.name;
        cols = table.colDefs;
        return this;
    }

    public CreateTableQuery createTable(String tableName) {
        createTable = tableName;
        return this;
    }

    public CreateTableQuery cols(List<Col> cols) {
        this.cols.addAll(cols);
        return this;
    }

    @Override
    public boolean isReady() {
        if(createTable.isEmpty()) return false;
        if(cols.isEmpty()) return false;
        return true;
    }

    @Override
    public String getQueryString() {
        String q = "CREATE TABLE `" + createTable + "` ( ";
        List<String> colList = new ArrayList<>();
        for(Col c : cols) {
            colList.add(c.defString());
        }
        q += String.join(", ", colList) + " );";
        return q;
    }
}
