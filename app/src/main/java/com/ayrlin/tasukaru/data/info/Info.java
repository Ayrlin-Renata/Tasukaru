package com.ayrlin.tasukaru.data.info;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ayrlin.sqlutil.query.data.Param;

import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@JsonClass
public abstract class Info<T> {
    protected @Getter T defaultInfo; //default should be a value which is NOT normally valid!
    protected @Getter @Setter String name;
    protected @Getter T value;
    protected @Getter @Setter TypeToken<Info<T>> type;
    protected @Getter @Setter TypeToken<T> datatype;

    public Info(T defaultInfo) {
        this.defaultInfo = defaultInfo;
        value = defaultInfo;
    }
    public Info(T defaultInfo, String name) {
        this(defaultInfo);
        this.name = name;
    }

    public Info<T> setDefault(T newDef) {
        if(atDefault()) value = newDef; 
        defaultInfo = newDef;
        return this;
    }

    @SuppressWarnings("unchecked")
    public void setValue(Object o) throws ClassCastException {
        if(datatype.getTypeClass().isInstance(o))
            value = (T)o;
        else throw new ClassCastException("attempted to setValue of Info to a type which it is not: " + o);
    }
    /**
     * 
     * @return true iff the value is the same as the current default
     */
    public abstract boolean atDefault();

    public static List<Info<?>> getNonDefault(List<Info<?>> list) {
        List<Info<?>> nl = new ArrayList<>();
        for(Info<?> i : list) {
            if(!i.atDefault()) nl.add(i);
        }
        return nl;
    }

    /**
     * 
     * @param other
     * @return true iff the value of both can be considered identical
     */
    public abstract boolean equals(Info<T> other);

    /**
     * 
     * @param other
     * @return true iff both have a non-default value and the value is not equal.
     */
    public boolean contradicts(Info<?> other) {
        return (!this.atDefault() && !other.atDefault() && !this.value.equals(other.value));
    }

    public abstract Param getParam();

    public abstract T assign(ResultSet rs) throws SQLException;

}
