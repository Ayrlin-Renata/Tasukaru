package com.ayrlin.sqlutil.query;
import com.ayrlin.sqlutil.query.data.Col;

public class AlterTableQuery extends SFQuery {
    public String alterTable;
    public Col addColumn;

    public AlterTableQuery() {
        alterTable = "";
        addColumn = null;
    }

    @Override
    public boolean isReady() {
        if(alterTable.isEmpty()) return false;
        if(addColumn == null) return false;
        return true;
    }

    public AlterTableQuery alterTable(String table) {
        this.alterTable = table;
        return this;
    }

    public AlterTableQuery addColumn(Col column) {
        this.addColumn = column;
        return this;
    }

    @Override
    public String getQueryString() {
        String q = "ALTER TABLE `" + alterTable + "`";
        if(addColumn != null) {
            q += " ADD COLUMN " + addColumn.defString()
                + ";";
        }
        return q;
    }
}
