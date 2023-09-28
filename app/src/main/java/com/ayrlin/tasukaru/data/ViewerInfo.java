package com.ayrlin.tasukaru.data;

import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import com.ayrlin.tasukaru.data.info.NumInfo;
import com.ayrlin.tasukaru.data.info.StringInfo;

import co.casterlabs.koi.api.types.user.UserPlatform;

import com.ayrlin.tasukaru.TLogic;
import com.ayrlin.tasukaru.data.info.Info;

@ToString(callSuper = true)
public class ViewerInfo extends InfoObject<ViewerInfo> {

    public ViewerInfo() {
        data = this.definition();
    }

    protected Map<String,Info<?>> definition() {
        Map<String,Info<?>> def = new HashMap<>();
        def.put("id", new NumInfo());
        def.put("clid", new StringInfo());
        def.put("clname", new StringInfo());
        def.put("fallbackname", new StringInfo());
        def.put("watchtime", new NumInfo());
        def.put("points", new NumInfo());
        def.put("lurking", new StringInfo());

        for(UserPlatform plat : TLogic.instance().getSupportedPlatforms()) {
            def.put(plat.name(), new NumInfo());
        }

        for(Info<?> i : def.values()) {
            for(Entry<String, Info<?>> entry : def.entrySet()) {
                if(Objects.equals(i, entry.getValue())) {
                    i.setName(entry.getKey());
                    break;
                }
            }
        }
        return def;
    }

    public String getName() {
        return (this.data.get("clname").atDefault()? 
                ((StringInfo) this.data.get("fallbackname")).getValue() 
                : ((StringInfo) this.data.get("clname")).getValue());
    }

    public List<Long> getAccountIds() {
        List<Long> accs = new ArrayList<>();
        for(UserPlatform plat : TLogic.instance().getSupportedPlatforms()) {
            NumInfo ni = ((NumInfo)data.get(plat.name()));
            if(!ni.atDefault())
                accs.add(ni.getValue());
        }
        return accs;
    }
}
