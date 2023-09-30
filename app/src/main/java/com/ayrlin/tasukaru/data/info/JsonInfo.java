package com.ayrlin.tasukaru.data.info;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.Param;
import com.ayrlin.tasukaru.Tasukaru;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.ToString;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@ToString(callSuper = true)
@JsonClass
public class JsonInfo extends Info<Object> {
    private static final Object JSON_DEFAULT = new Object();

    public JsonInfo() {
        this("");
    }
    public JsonInfo(String name) {
        super(JSON_DEFAULT, name);
        this.type = new TypeToken<Info<Object>>() {};
        this.datatype = new TypeToken<Object>() {};
    }

    public void valueFromString(String s) {
        try {
            value = Rson.DEFAULT.fromJson(s, this.datatype);
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Exception while deserializing JsonInfo String: " + s);
            e.printStackTrace();
        }
    }

    @Override
    public boolean atDefault() {
        return (this.value.equals(defaultInfo));
    }

    @Override
    public boolean equals(Info<Object> other) {
        return this.value.equals(other.value);
    }

    @Override
    public Param getParam() {
        return new Param(DataType.STRING, name, (String) Rson.DEFAULT.toJson(value).toString());
    }
    @Override
    public Object assign(ResultSet rs) throws SQLException {
        String input = rs.getString(name);
        try {
            setValue(Rson.DEFAULT.fromJson(input, this.datatype));
        } catch (JsonParseException e) {
            Tasukaru.instance().getLogger().warn("exception while parsing json from VB: " + input);
            e.printStackTrace();
        }
        if(rs.wasNull()) {
            this.value = defaultInfo;
        }
        return value;
    }
}
