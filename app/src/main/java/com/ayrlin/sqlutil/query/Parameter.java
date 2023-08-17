package com.ayrlin.sqlutil.query;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Parameter {
    public DataType type;
    public String column;
    public Object value;

    public Parameter(DataType type, String column, Object value) {
        this.type = type;
        this.column = column;
        this.value = value;
    }

    public enum DataType {
        STRING,
        INT,
        TIMESTAMP
    }
}
