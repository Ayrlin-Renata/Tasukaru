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
public class BoolInfo extends Info<Boolean> {
    public static final boolean BOOL_DEFAULT = false;

    public BoolInfo() {
        this("");
    }
    public BoolInfo(String name) {
        super(BOOL_DEFAULT, name);
        this.type = new TypeToken<Info<Boolean>>() {};
        this.datatype = new TypeToken<Boolean>() {};
    }

    @Override
    public boolean atDefault() {
        return (this.value == defaultInfo);
    }

    @Override
    public boolean equals(Info<Boolean> other) {
        return (this.value == other.value);
    }

    @Override
    public Param getParam() {
        return new Param(DataType.BOOL, name, (boolean) value);
    }
    
    @Override
    public Boolean assign(ResultSet rs) throws SQLException {
        setValue(rs.getBoolean(name));
        if(rs.wasNull()) {
            this.value = defaultInfo;
        }
        return value;
    }
    
}
