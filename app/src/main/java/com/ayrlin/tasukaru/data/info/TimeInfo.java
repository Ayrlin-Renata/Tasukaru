package com.ayrlin.tasukaru.data.info;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.Param;

import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.ToString;

@ToString(callSuper = true)
@JsonClass
public class TimeInfo extends Info<Timestamp> {
        public static final Timestamp TIMESTAMP_DEFAULT = new Timestamp(0);

    public TimeInfo() {
        this("");
    }
    public TimeInfo(String name) {
        super(TIMESTAMP_DEFAULT, name);
        this.type = new TypeToken<Info<Timestamp>>() {};
        this.datatype = new TypeToken<Timestamp>() {};
    }

    @Override
    public boolean atDefault() {
        return (this.value.equals(defaultInfo));
    }

    @Override
    public boolean equals(Info<Timestamp> other) {
        return (this.value.equals(other.value));
    }

    @Override
    public Param getParam() {
        return new Param(DataType.TIMESTAMP, name, (Timestamp) value);
    }
    @Override
    public Timestamp assign(ResultSet rs) throws SQLException {
        setValue(new java.sql.Timestamp(rs.getLong(name)));
        return value;
    }
}
