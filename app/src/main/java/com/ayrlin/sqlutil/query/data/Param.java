package com.ayrlin.sqlutil.query.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper=true)
public class Param extends SCol {
    public Object value;

    public Param(DataType type, String column, Object value) {
        super(column, type);
        this.value = value;
    }
}
