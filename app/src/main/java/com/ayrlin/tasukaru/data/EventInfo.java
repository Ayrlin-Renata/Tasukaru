package com.ayrlin.tasukaru.data;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.ayrlin.tasukaru.data.info.BoolInfo;
import com.ayrlin.tasukaru.data.info.Info;
import com.ayrlin.tasukaru.data.info.JsonInfo;
import com.ayrlin.tasukaru.data.info.LongInfo;
import com.ayrlin.tasukaru.data.info.StringInfo;
import com.ayrlin.tasukaru.data.info.TimeInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
public class EventInfo extends InfoObject<EventInfo> {

    private @Getter AccountInfo account;
    private @Getter ViewerInfo viewer;

    @AllArgsConstructor
    public enum UpType {
        PRESENT("present"),
        ABSENT("absent"),
        TECHNICAL("technical");

        String str;

        @Override
        public String toString() {
            return str;
        }
    }

    public interface Origin {}
    public interface Action extends Origin {}

    /**
     * Present Action
     */
    @AllArgsConstructor
    public enum PAct implements Action {
        MESSAGE("message"),
        FOLLOW("follow"),
        SUBSCRIBE("subscribe"),
        DONATE("donate"),
        JOIN("join"),
        RAID("raid"),
        CHANNELPOINTS("channel points"),
        LISTED("listed");

        String str;

        @Override
        public String toString() {
            return str;
        }
    }

    /**
     * Absent Action
     */
    @AllArgsConstructor
    public enum AAct implements Action {
        LEAVE("leave");

        String str;

        @Override
        public String toString() {
            return str;
        }
    }

    /**
     * Technical Action
     */
    @AllArgsConstructor
    public enum TAct implements Action {
        SNAPSHOT("snapshot"),
        POINTS("points");

        String str;

        @Override
        public String toString() {
            return str;
        }
    }

    @AllArgsConstructor
    public enum Source implements Origin {
        WATCHTIME("watchtime"),
        COMMAND("command"),
        KOIEVENT("koi");

        String str;

        @Override
        public String toString() {
            return str;
        }
    }

    @AllArgsConstructor
    public enum Stream {
        ONLINE("live"),
        OFFLINE("offline");

        String str;

        @Override
        public String toString() {
            return str;
        }
    }

    public EventInfo(Timestamp timestamp) {
        this.data = this.definition();
        this.set("timestamp", timestamp);
    }

    public EventInfo() {
        this(new java.sql.Timestamp(new java.util.Date().getTime()));
    }

    protected Map<String,Info<?>> definition() {
        Map<String,Info<?>> def = new HashMap<>();
        def.put("aid", new LongInfo());
        def.put("sid", new StringInfo());
        def.put("uptype", new StringInfo());
        def.put("action", new StringInfo());
        def.put("value", new LongInfo());
        def.put("origin", new StringInfo());
        def.put("streamstate", new StringInfo().setDefault("unrecorded"));
        def.put("timestamp", new TimeInfo());
        def.put("event", new JsonInfo());
        def.put("processed", new BoolInfo());

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

    public EventInfo setAccount(AccountInfo ai) {
        this.account = ai;
        this.set("aid", ai.get("id"));
        //this.set("sid", ai.get("latestsnapshot"));
        return this;
    }

    public EventInfo setViewer(ViewerInfo vi) {
        this.viewer = vi;
        return this;
    }
}
