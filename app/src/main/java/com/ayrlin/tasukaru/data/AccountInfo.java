package com.ayrlin.tasukaru.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.ayrlin.sqlutil.query.data.Param;
import com.ayrlin.tasukaru.Tasukaru;
import com.ayrlin.tasukaru.data.info.Info;
import com.ayrlin.tasukaru.data.info.RolesListInfo;
import com.ayrlin.tasukaru.data.info.StringListInfo;
import com.ayrlin.tasukaru.data.info.NumInfo;
import com.ayrlin.tasukaru.data.info.StringInfo;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.User.UserRoles;
import lombok.ToString;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@ToString(callSuper = true)
public class AccountInfo extends InfoObject<AccountInfo> {

    public AccountInfo() {
        data = this.definition();
    }

    public AccountInfo(User user) {
        this();
        set("userid", (String) user.getId());
        set("channelid", (String) user.getChannelId());
        set("platform", (String) user.getPlatform().name());
        set("upid", (String) user.getUPID());
        set("roles", (List<UserRoles>) user.getRoles());
        set("badges", (List<String>) user.getBadges());
        set("color", (String) user.getColor());
        set("username", (String) user.getUsername());
        set("displayname", (String) user.getDisplayname());
        set("bio", (String) user.getBio());
        set("link", (String) user.getLink());
        if(user.getImageLink() != null) set("imagelink", (String) user.getImageLink());
        set("followerscount", (Long) user.getFollowersCount());
        set("subcount", (Long) user.getSubCount());
    }

    public AccountInfo(AccountInfo ai) {
        this();
        this.data = ai.data; //this will definately work. TODO you know to come back here if it dont work
    }

    protected Map<String,Info<?>> definition() {
        Map<String,Info<?>> def = new HashMap<>();
        def.put("id", new NumInfo());
        def.put("vid", new NumInfo());
        def.put("latestsnapshot", new NumInfo());
        def.put("userid", new StringInfo());
        def.put("channelid", new StringInfo().setDefault("-1"));
        def.put("platform", new StringInfo());
        def.put("upid", new StringInfo());
        def.put("roles", new RolesListInfo());
        def.put("badges", new StringListInfo());
        def.put("color", new StringInfo());
        def.put("username", new StringInfo());
        def.put("displayname", new StringInfo());
        def.put("bio", new StringInfo());
        def.put("link", new StringInfo());
        def.put("imagelink", new StringInfo());
        def.put("followerscount", new NumInfo());
        def.put("subcount", new NumInfo());

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

    /**
     * fills any default values of this VI with values from base
     * @param base
     * @return this
     */
    public AccountInfo fillDefaults(AccountInfo base) {
        Tasukaru.instance().getLogger().trace("filling defaults using account: \n" + base);
        for(Info<?> i : data.values()) {
            if (i.atDefault()) {
                Tasukaru.instance().getLogger().trace("info needs filling: " + i);
                Tasukaru.instance().getLogger().trace("info filling from: " + base.data.get(i.getName()));
                i.setValue(base.get(i.getName()));
            }
        }
        return this;
    }

    /**
     * compares all semi-permanent info, ignoring comparisons where either side has default values
     * @param other
     * @return
     */
    public boolean notContradictory(AccountInfo other) {
        for(Info<?> i : data.values()) {
            if(i.contradicts((Info<?>) other.data.get(i.getName()))) {
                Tasukaru.instance().getLogger().debug("Contradiction found: \n" + i + "\n" + other.data.get(i.getName()));
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        AccountInfo other;
        if(o instanceof AccountInfo) {
            other = (AccountInfo) o;
        } else {
            Tasukaru.instance().getLogger().warn("AccountInfo compared to non-AccountInfo Object!\nAccountInfo:\n" + this + "\nObject:\n" + o.toString());
            return false;
        }
        for(Info<?> i : data.values()) {
            if(!i.equals(other.data.get(i.getName()))) {
                return false;
            }
        }
        return true;
    }

    public List<Param> listUnfilledValues() {
        List<Param> params = new ArrayList<>();
        for(Info<?> i : data.values()) {
            if(i.atDefault()) params.add(i.getParam());
        }

        FastLogger.logStatic(LogLevel.TRACE, "unfilled values for viewer " + data.get("id").getValue() + ": \n" + params);
        return params;
    }
}
