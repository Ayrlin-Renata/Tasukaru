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
public class RealInfo extends Info<Double> {
    public static final double REAL_DEFAULT = -1D;

    public RealInfo() {
        this("");
    }
    public RealInfo(String name) {
        super(REAL_DEFAULT, name);
        this.type = new TypeToken<Info<Double>>() {};
        this.datatype = new TypeToken<Double>() {};
    }

    @Override
    public boolean atDefault() {
        return (this.value == defaultInfo);
    }

    @Override
    public boolean equals(Info<Double> other) {
        return (this.value == other.value);
    }

    @Override
    public Param getParam() {
        return new Param(DataType.DOUBLE, name, (double) value);
    }

    @Override
    public Double assign(ResultSet rs) throws SQLException {
        setValue(rs.getDouble(name));
        if(rs.wasNull()) {
            this.value = defaultInfo;
        }
        return value;
    }
    
}
