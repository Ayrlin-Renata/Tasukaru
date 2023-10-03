package com.ayrlin.tasukaru.data.info;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.Param;

import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.ToString;

@ToString(callSuper = true)
@JsonClass
public class LongInfo extends Info<Long> {
    public static final long LONG_DEFAULT = -1;

    public LongInfo() {
        this("");
    }
    public LongInfo(String name) {
        super(LONG_DEFAULT, name);
        this.type = new TypeToken<Info<Long>>() {};
        this.datatype = new TypeToken<Long>() {};
    }

    @Override
    public boolean atDefault() {
        return (this.value == defaultInfo);
    }

    @Override
    public boolean equals(Info<Long> other) {
        return (this.value == other.value);
    }

    @Override
    public Param getParam() {
        return new Param(DataType.LONG, name, (long) value);
    }
    
    @Override
    public Long assign(ResultSet rs) throws SQLException {
        setValue(rs.getLong(name));
        if(rs.wasNull()) {
            this.value = defaultInfo;
        }
        return value;
    }
    
}
