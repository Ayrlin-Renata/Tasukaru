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
public class StringInfo extends Info<String> {
    public static final String STRING_DEFAULT = "";

    public StringInfo() {
        this("");
    }
    public StringInfo(String name) {
        super(STRING_DEFAULT, name);
        this.type = new TypeToken<Info<String>>() {};
        this.datatype = new TypeToken<String>() {};
    }

    @Override
    public boolean atDefault() {
        return (this.value.equals(defaultInfo));
    }

    @Override
    public boolean equals(Info<String> other) {
        return (this.value.equals(other.value));
    }

    @Override
    public Param getParam() {
        return new Param(DataType.STRING, name, (String) value);
    }
    @Override
    public String assign(ResultSet rs) throws SQLException {
        setValue(rs.getString(name));
        return value;
    }
}
