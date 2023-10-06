package com.ayrlin.tasukaru.caffeine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.DefParam;
import com.ayrlin.tasukaru.TLogic;
import com.ayrlin.tasukaru.Tasukaru;
import com.ayrlin.tasukaru.data.EventInfo.PAct;

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

    public static class Moderation {
        public static DefParam detect_mods = new DefParam(DataType.BOOL, "detect_mods", null, false);
        public static Map<String, DefParam> mods = new HashMap<>();
    }

    public static class Behaviours {
        public static DefParam cull_backups = new DefParam(DataType.BOOL, "cull_backups", null, true);
        public static DefParam backup_size = new DefParam(DataType.LONG, "backup_size", null, 1024L);
    }

    /**
     * fill any missing settings() values
     */
    public static void init() {
        jsets = Tasukaru.instance().settings().getJson();
        List<UserPlatform> platforms = TLogic.instance().getSupportedPlatforms(); 

        //points
        initOrDef("points", Points.watchtime);
        initOrDef("points", Points.offline_bonus_mult);
        initOrDef("points", Points.offline_chat_mult);
        initOrDef("points", Points.lurk_mult);
        initOrDef("points", Points.raider_bonus);
        initOrDef("points", Points.dono_per_unit);
        
        //bonuses
        for(UserPlatform plat : platforms) {
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
                initOrDef("bonuses", dp);
            }
        }

        //channelpoints
        for(UserPlatform plat : platforms) {
            String key = plat.name() + "_mult";
            DefParam dp = new DefParam(DataType.LONG, key, null, 0D);
            ChannelPoints.conversions.put(key, dp);
            initOrDef("channelpoints", dp);
        }
        
        //watchtime
        initOrDef("watchtime", Watchtime.around_present);
        initOrDef("watchtime", Watchtime.chain_timeout);
        initOrDef("watchtime", Watchtime.lurk_bonus);
        initOrDef("watchtime", Watchtime.lurk_chain);
        initOrDef("watchtime", Watchtime.lurk_end);
        initOrDef("watchtime", Watchtime.lurk_timeout);

        //moderation
        initOrDef("moderation", Moderation.detect_mods);
        for(UserPlatform plat : platforms) {
            String key = plat.name() + "_mods";
            DefParam dp = new DefParam(DataType.STRING, key, "", "");
            Moderation.mods.put(key, dp);
            initOrDef("moderation", dp);
        }
        
        //behaviours
        initOrDef("behaviours", Behaviours.cull_backups);
        initOrDef("behaviours", Behaviours.backup_size);

        Tasukaru.instance().setSettings(jsets);
    }

    private static void initOrDef(String section, DefParam param) {
        if(!jsets.containsKey(section + "." + param.column)) {
            putWithType(section + "." + param.column, param.type, param.defaultValue);
        }
    }
    
    private static void putWithType(String key, DataType type, Object value) {
        switch(type) {
            case BOOL:
                jsets.put(key, (boolean) value);
                break;
            case DOUBLE:
            case LONG:
                jsets.put(key, (Number) value);
                break;
            case STRING:
            case TIMESTAMP:
                jsets.put(key, (String) value);
                break;
        }
    }

    /**
     * set values from tskr.settings()
     */
    public static void update() {
        jsets = Tasukaru.instance().settings().getJson();
        List<UserPlatform> platforms = TLogic.instance().getSupportedPlatforms(); 

        //points
        Points.watchtime.setValue(jsets.get("points.watchtime").getAsNumber().longValue());
        Points.offline_bonus_mult.setValue(jsets.get("points.offline_bonus_mult").getAsNumber().doubleValue());
        Points.offline_chat_mult.setValue(jsets.get("points.offline_chat_mult").getAsNumber().doubleValue());
        Points.lurk_mult.setValue(jsets.get("points.lurk_mult").getAsNumber().doubleValue());
        Points.raider_bonus.setValue(jsets.get("points.raider_bonus").getAsNumber().longValue());
        Points.dono_per_unit.setValue(jsets.get("points.dono_per_unit").getAsNumber().longValue());
        
        //bonuses
        for(UserPlatform plat : platforms) {
            for(PAct p : PAct.values()) {
                String key = plat.name() + "_" + p.name();
                Bonuses.bonuses.get(key).setValue(jsets.get("bonuses." + key).getAsNumber().longValue());
            }
        }
        //channelpoints
        for(UserPlatform plat : platforms) {
            String key = plat.name() + "_mult";
            ChannelPoints.conversions.get(key).setValue(jsets.get("channelpoints." + key).getAsNumber().doubleValue());
        }
        
        //watchtime
        Watchtime.around_present.setValue(jsets.get("watchtime.around_present").getAsNumber().longValue());
        Watchtime.chain_timeout.setValue(jsets.get("watchtime.chain_timeout").getAsNumber().longValue());
        Watchtime.lurk_bonus.setValue(jsets.get("watchtime.lurk_bonus").getAsNumber().longValue());
        Watchtime.lurk_chain.setValue(jsets.get("watchtime.lurk_chain").getAsNumber().longValue());
        Watchtime.lurk_end.setValue(jsets.get("watchtime.lurk_end").getAsNumber().longValue());
        Watchtime.lurk_timeout.setValue(jsets.get("watchtime.lurk_timeout").getAsNumber().longValue());
        
        //moderation
        Moderation.detect_mods.setValue(jsets.get("moderation.detect_mods"));
        for(UserPlatform plat : platforms) {
            String key = plat.name() + "_mods";
            Moderation.mods.get(key).setValue(jsets.get("moderation." + key).getAsString());
        }

        //behaviours
        Behaviours.cull_backups.setValue(jsets.get("behaviours.cull_backups").getAsBoolean());
        Behaviours.backup_size.setValue(jsets.get("behaviours.backup_size").getAsNumber().longValue());

    }
}
