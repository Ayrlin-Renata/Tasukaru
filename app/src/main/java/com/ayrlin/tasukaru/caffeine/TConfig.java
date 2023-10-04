package com.ayrlin.tasukaru.caffeine;

import java.util.HashMap;
import java.util.Map;

import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.DefParam;
import com.ayrlin.tasukaru.TLogic;
import com.ayrlin.tasukaru.Tasukaru;
import com.ayrlin.tasukaru.data.EventInfo.PAct;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetSettings;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.element.JsonObject;

public class TConfig {
    private static JsonObject jsets;

    public static class Points {
        public static DefParam watchtime = new DefParam(DataType.LONG, "watchtime", null, 100L );
        public static DefParam offline_bonus_mult = new DefParam(DataType.DOUBLE, "offline_bonus_mult", null, 2D );
        public static DefParam offline_chat_mult = new DefParam(DataType.DOUBLE, "offline_chat_mult", null,0.25D );
        public static DefParam lurk_mult = new DefParam(DataType.DOUBLE, "lurk_mult", null,1.25D );
        public static DefParam raider_bonus = new DefParam(DataType.LONG, "raider_bonus", null, 100L );
        public static DefParam dono_per_unit = new DefParam(DataType.LONG, "dono_per_unit", null, 100L );
    }
    public static class Bonuses {
        public static Map<String, DefParam> bonuses = new HashMap<>();
    }

    public static class ChannelPoints {
        public static Map<String, DefParam> conversions = new HashMap<>();
    }

    public static class Watchtime {
        public static DefParam around_present = new DefParam(DataType.LONG, "around_present", null, 5L);
        public static DefParam chain_timeout = new DefParam(DataType.LONG, "chain_timeout", null, 20L);
        public static DefParam lurk_bonus = new DefParam(DataType.LONG, "lurk_bonus", null, 30L);
        public static DefParam lurk_chain = new DefParam(DataType.LONG, "lurk_chain", null, 60L);
        public static DefParam lurk_end = new DefParam(DataType.LONG, "lurk_end", null, 10L);
        public static DefParam lurk_timeout = new DefParam(DataType.LONG, "lurk_timeout", null, 90L);
    }

    public static class Behaviours {
        public static DefParam cull_backups = new DefParam(DataType.BOOL, "cull_backups", null, true);
        public static DefParam backup_size = new DefParam(DataType.LONG, "backup_size", null, 1024L);
    }

    /**
     * fill any missing settings() values
     */
    public static void init() { //TODO implement all this into TSettings 
        WidgetSettings tsets = Tasukaru.instance().settings();
        jsets = tsets.getJson();

        //points
        initOrDef(Points.watchtime);
        initOrDef(Points.offline_bonus_mult);
        initOrDef(Points.offline_chat_mult);
        initOrDef(Points.lurk_mult);
        initOrDef(Points.raider_bonus);
        initOrDef(Points.dono_per_unit);
        
        //bonuses
        for(UserPlatform plat : TLogic.instance().getSupportedPlatforms()) {
            for(PAct p : PAct.values()) {
                String key = plat.name() + "_" + p.name();
                long defaultPoints = 0;
                switch(p) {
                    case CHANNELPOINTS:
                        defaultPoints = 5;
                        break;
                    case DONATE:
                        defaultPoints = 100;
                        break;
                    case FOLLOW:
                        defaultPoints = 100;
                        break;
                    case JOIN:
                        break;
                    case LISTED:
                        break;
                    case MESSAGE:
                        defaultPoints = 5;
                        break;
                    case RAID:
                        defaultPoints = 500;
                        break;
                    case SUBSCRIBE:
                        defaultPoints = 500;
                        break;
                }
                DefParam dp = new DefParam(DataType.LONG, key, null, defaultPoints);
                Bonuses.bonuses.put(key, dp);
                initOrDef(dp);
            }
        }

        //channelpoints
        for(UserPlatform plat : TLogic.instance().getSupportedPlatforms()) {
            String key = plat.name() + "_mult";
            DefParam dp = new DefParam(DataType.LONG, key, null, 0D);
            ChannelPoints.conversions.put(key, dp);
            initOrDef(dp);
        }
        
        //watchtime
        initOrDef(Watchtime.around_present);
        initOrDef(Watchtime.chain_timeout);
        initOrDef(Watchtime.lurk_bonus);
        initOrDef(Watchtime.lurk_chain);
        initOrDef(Watchtime.lurk_end);
        initOrDef(Watchtime.lurk_timeout);
        
        //behaviours
        initOrDef(Behaviours.cull_backups);
        initOrDef(Behaviours.backup_size);

        Tasukaru.instance().setSettings(jsets);
    }

    private static void initOrDef(DefParam param) {
        if(!jsets.containsKey(param.column)) {
            putWithType(jsets, param.column, param.type, param.defaultValue);
        }
    }
    
    private static void putWithType(JsonObject jso, String key, DataType type, Object value) {
        switch(type) {
            case BOOL:
                jso.put(key, (boolean) value);
                break;
            case DOUBLE:
            case LONG:
                jso.put(key, (Number) value);
                break;
            case STRING:
            case TIMESTAMP:
                jso.put(key, (String) value);
                break;
        }
    }

    /**
     * set values from tskr.settings()
     */
    public static void update() {
        JsonObject tsetsJson = Tasukaru.instance().settings().getJson().getObject("settings");

        //points
        Points.watchtime.setValue(tsetsJson.get("watchtime").getAsNumber().longValue());
        Points.offline_bonus_mult.setValue(tsetsJson.get("offline_bonus_mult").getAsNumber().doubleValue());
        Points.offline_chat_mult.setValue(tsetsJson.get("offline_chat_mult").getAsNumber().doubleValue());
        Points.lurk_mult.setValue(tsetsJson.get("lurk_mult").getAsNumber().doubleValue());
        Points.raider_bonus.setValue(tsetsJson.get("raider_bonus").getAsNumber().longValue());
        Points.dono_per_unit.setValue(tsetsJson.get("dono_per_unit").getAsNumber().longValue());
        
        //bonuses
        Bonuses.bonuses = new HashMap<>();
        for(UserPlatform plat : TLogic.instance().getSupportedPlatforms()) {
            for(PAct p : PAct.values()) {
                String key = plat.name() + "_" + p.name();
                Bonuses.bonuses.get(key).setValue(tsetsJson.get(key).getAsNumber().longValue());
            }
        }
        //channelpoints
        ChannelPoints.conversions = new HashMap<>();
        for(UserPlatform plat : TLogic.instance().getSupportedPlatforms()) {
            String key = plat.name() + "_mult";
            ChannelPoints.conversions.get(key).setValue(tsetsJson.get(key).getAsNumber().doubleValue());
        }
        
        //watchtime
        Watchtime.around_present.setValue(tsetsJson.get("around_present").getAsNumber().longValue());
        Watchtime.chain_timeout.setValue(tsetsJson.get("chain_timeout").getAsNumber().longValue());
        Watchtime.lurk_bonus.setValue(tsetsJson.get("lurk_bonus").getAsNumber().longValue());
        Watchtime.lurk_chain.setValue(tsetsJson.get("lurk_chain").getAsNumber().longValue());
        Watchtime.lurk_end.setValue(tsetsJson.get("lurk_end").getAsNumber().longValue());
        Watchtime.lurk_timeout.setValue(tsetsJson.get("lurk_timeout").getAsNumber().longValue());
        
        //behaviours
        Behaviours.cull_backups.setValue(tsetsJson.get("cull_backups").getAsBoolean());
        Behaviours.backup_size.setValue(tsetsJson.get("backup_size").getAsNumber().longValue());

    }
}
