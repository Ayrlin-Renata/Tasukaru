package com.ayrlin.tasukaru.data.info;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.Param;
import com.ayrlin.tasukaru.Tasukaru;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.ToString;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@ToString(callSuper = true)
@JsonClass
public abstract class ListInfo<T> extends Info<List<T>> {

    public ListInfo(List<T> def, String name) { //used by subclasses
        super(def, name); 
    }

    public void valueFromString(String s) {
        try {
            value = Rson.DEFAULT.fromJson(s, this.datatype);
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Exception while deserializing ListInfo String: " + s);
            e.printStackTrace();
        }
    }

    @Override
    public boolean atDefault() {
        return (this.value.equals(defaultInfo));
    }

    @Override
    public boolean equals(Info<List<T>> other) {
        return this.value.equals(other.value);
    }

    @Override
    public Param getParam() {
        return new Param(DataType.STRING, name, (String) Rson.DEFAULT.toJson(value).toString());
    }

    @Override
    public List<T> assign(ResultSet rs) throws SQLException {
        String input = rs.getString(name);
        try {
            setValue(Rson.DEFAULT.fromJson(input, this.datatype));
        } catch (JsonParseException e) {
            Tasukaru.instance().getLogger().warn("exception while parsing list from VB: " + input);
            e.printStackTrace();
        }
        return value;
    }
}
