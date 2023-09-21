package com.ayrlin.sqlutil.query.data;

import com.ayrlin.sqlutil.query.AlterTableQuery;
import com.ayrlin.tasukaru.VBHandler;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper=true)
public class Col extends SCol {
    public boolean notNull;
    public boolean primaryKey;
    public boolean autoIncrement;
    public boolean unique;
    public String references;
    public Object default_;

    public Col(String column, DataType type) {
        super(column, type);
        notNull = false;
        primaryKey = false;
        autoIncrement = false;
        unique = false;
        references = "";
        default_ = null;
    }

    public Col notNull() { return notNull(true); }
    public Col notNull(boolean notNull) {
        this.notNull = notNull;
        return this;
    }
    
    public Col primaryKey() { return primaryKey(true); }
    public Col primaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }
    
    public Col autoIncrement() { return autoIncrement(true); }
    public Col autoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
        return this;
    }
    
    public Col unique() { return unique(true); }
    public Col unique(boolean unique) {
        this.unique = unique;
        return this;
    }
    
    public Col references(String references) {
        this.references = references;
        return this;
    }

    public Col default_(Object default_) {
        this.default_ = default_;
        return this;
    }

    public String defString() {
        return this.column + " " 
                + this.type.toString()
                + ((this.primaryKey)? " PRIMARY KEY" + ((this.autoIncrement)? " AUTOINCREMENT" : "") : "")
                + ((this.notNull)? " NOT NULL" : "")
                + ((this.unique)? " UNIQUE" : "")
                + ((this.default_ != null)? " DEFAULT " + this.default_.toString() : "")
                + ((!this.references.isEmpty())? " REFERENCES " + this.references : "");
    }

    public boolean alterInto(Table table) {
        return alterInto(table.name);
    }
    public boolean alterInto(String tablename) {
        return new AlterTableQuery().alterTable(tablename).addColumn(this).execute(VBHandler.instance().getConnection());
    }
}
