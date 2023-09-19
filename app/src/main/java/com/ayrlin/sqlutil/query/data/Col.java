package com.ayrlin.sqlutil.query.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Col extends SCol {
    public boolean notNull;
    public boolean primaryKey;
    public boolean autoIncrement;
    public boolean unique;
    public String references;
    public Object default_;

    public Col(String column, DataType type) {
        super(column, type);
    }

    public Col notNull(boolean notNull) {
        this.notNull = notNull;
        return this;
    }
    
    public Col primaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }
    
    public Col autoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
        return this;
    }
    
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
}
