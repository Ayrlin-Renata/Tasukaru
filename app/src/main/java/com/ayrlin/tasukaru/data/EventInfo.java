package com.ayrlin.tasukaru.data;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.ayrlin.tasukaru.data.info.Info;
import com.ayrlin.tasukaru.data.info.JsonInfo;
import com.ayrlin.tasukaru.data.info.NumInfo;
import com.ayrlin.tasukaru.data.info.StringInfo;
import com.ayrlin.tasukaru.data.info.TimeInfo;

import co.casterlabs.koi.api.types.events.KoiEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true)
public class EventInfo extends InfoObject<EventInfo> {

    private @Getter AccountInfo account;
    private @Getter @Setter ViewerInfo viewer;
    private @Getter KoiEvent event;

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

    public interface Action {
        // :3
    }

    /**
     * Present Action
     */
    @AllArgsConstructor
    public enum PAct implements Action {
        MESSAGE("message"),
        FOLLOW("follow"),
        SUBSCRIBE("subscribe"),
        DONATE("donate"),
        JOIN("join");

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

    public EventInfo(Timestamp timestamp) {
        this.data = this.definition();
        this.set("timestamp", timestamp);
    }

    public EventInfo() {
        this(new java.sql.Timestamp(new java.util.Date().getTime()));
    }

    protected Map<String,Info<?>> definition() {
        Map<String,Info<?>> def = new HashMap<>();
        def.put("timestamp", new TimeInfo());
        def.put("aid", new NumInfo());
        def.put("sid", new StringInfo());
        def.put("event", new JsonInfo());
        def.put("uptype", new StringInfo());
        def.put("action", new StringInfo());
        def.put("value", new NumInfo());
        def.put("streamstate", new StringInfo().setDefault("unrecorded"));
        def.put("processed", new StringInfo().setDefault("false"));

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

    public EventInfo setEvent(KoiEvent e) {
        this.event = e;
        this.set("event", e);
        return this;
    }

    public EventInfo setAccount(AccountInfo ai) {
        this.account = ai;
        this.set("aid", ai.get("id"));
        //this.set("sid", ai.get("latestsnapshot"));
        return this;
    }
}
