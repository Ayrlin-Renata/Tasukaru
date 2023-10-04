package com.ayrlin.sqlutil.query.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefParam extends Param {
    public Object defaultValue;

    public DefParam(DataType type, String column, Object value, Object defObject) {
        super(type, column, value);
        defaultValue = defObject;
    }
    
}
