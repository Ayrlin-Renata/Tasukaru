package com.ayrlin.tasukaru.data;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.ayrlin.tasukaru.Tasukaru;
import com.ayrlin.tasukaru.data.info.*;

import co.casterlabs.koi.api.types.user.User.UserRoles;
import lombok.Getter;
import lombok.ToString;

@ToString
public abstract class InfoObject<T extends InfoObject<T>> {
    protected @Getter Map<String,Info<?>> data;

    @SuppressWarnings("unchecked")
    public T set(String key, Object value) {
        key = key.toLowerCase();
        if(data.containsKey(key)) {
            if(data.get(key) instanceof NumInfo) {
                ((NumInfo) data.get(key)).setValue((Long) value); 
            } else if (data.get(key) instanceof StringInfo) {
                ((StringInfo) data.get(key)).setValue(value.toString()); 
            } else if (data.get(key) instanceof TimeInfo) {
                ((TimeInfo) data.get(key)).setValue((Timestamp) value); 
            } else if (data.get(key) instanceof JsonInfo) {
                if (value instanceof String) {
                    ((JsonInfo) data.get(key)).valueFromString((String) value);
                } else {
                    ((JsonInfo) data.get(key)).setValue((Object) value); 
                }
            } else if (data.get(key) instanceof RolesListInfo) {
                if (value instanceof String) {
                    ((RolesListInfo) data.get(key)).valueFromString((String) value);
                } else {
                    ((RolesListInfo) data.get(key)).setValue((List<UserRoles>) value); 
                }
            } else if (data.get(key) instanceof StringListInfo) {
                if (value instanceof String) {
                    ((StringListInfo) data.get(key)).valueFromString((String) value);
                } else {
                    ((StringListInfo) data.get(key)).setValue((List<String>) value); 
                }
            } 
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setMultiple(Map<String, ?> data) {
        for(String key : data.keySet()) {
            set(key, data.get(key));
        }
        return (T) this;
    }

    public Object get(String key) {
        key = key.toLowerCase();
        Object value = null;
        try { 
            value = data.get(key).getValue(); 
        } catch(NullPointerException e) {
            Tasukaru.instance().getLogger().warn("null pointer retrieving key '" + key + "' from InfoObject: n" + this.toString());
        }
        return value;
    }
}
