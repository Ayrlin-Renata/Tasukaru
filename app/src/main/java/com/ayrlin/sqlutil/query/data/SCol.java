package com.ayrlin.sqlutil.query.data;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class SCol {
    public String column;
    public DataType type;

    @SuppressWarnings("rawtypes")
    public Class getTypeClass() {
        Class typeClass = null;
        switch(type) {
            case BOOL:
                typeClass = Boolean.class;
                break;
            case DOUBLE:
                typeClass = Double.class;
                break;
            case LONG:
                typeClass = Long.class;
                break;
            case STRING:
                typeClass = String.class;
                break;
            case TIMESTAMP:
                typeClass = Timestamp.class;
                break;
        }
        return typeClass;
    }
}
