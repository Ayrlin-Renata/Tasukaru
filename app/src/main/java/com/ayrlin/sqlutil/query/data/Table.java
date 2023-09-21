package com.ayrlin.sqlutil.query.data;

import java.util.ArrayList;
import java.util.List;

import com.ayrlin.sqlutil.query.CreateTableQuery;
import com.ayrlin.tasukaru.VBHandler;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@Getter
@Setter
@ToString
public class Table {
    public String name;
    public List<Col> colDefs;

    public Table(String name) {
        this.name = name;
        colDefs = new ArrayList<>();
    }

    public Table cols(List<Col> columns) {
        colDefs.addAll(columns);
        return this;
    }

    public Table col(Col column) {
        colDefs.add(column);
        return this;
    }

    public boolean create() {
        FastLogger.logStatic(LogLevel.TRACE, "creating table: n" + this);
        return new CreateTableQuery().createTable(this).execute(VBHandler.instance().getConnection());
    }
}
