package com.ayrlin.tasukaru;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ayrlin.tasukaru.data.EventInfo.PAct;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetSettings;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.element.JsonObject;

public class TSettings {
    private static TSettings instance;

    private Tasukaru tskr;
    private Map<String, String> platmap = new HashMap<>(); 
    private Map<String, String> actionmap = new HashMap<>(); 

    private TSettings() {
        tskr = Tasukaru.instance();
    }

    /**
     * singleton pattern
     * @return THE TSettings
     */
    public static TSettings instance() {
        if(instance == null) {
            instance = new TSettings();
        } 
        return instance;
    }

    public boolean begin() {
        //TODO pre-render settings to avoid unset settings issues
        // try {
        //     tskr.setSettings(new JsonObject()); // DEBUG RESET
        // } catch( Throwable t) {
        //     tskr.getLogger().warn("SETTINGS ERROR CAUGHT:\n" + t);
        // }
        Tasukaru.instance().getLogger().trace("building maps");
        this.buildMaps();
        Tasukaru.instance().getLogger().trace("rendering settings");
        this.renderSettingsLayout();
        return true;
    }

    public void buildMaps() {
        platmap = new HashMap<>(); 
        platmap.put("select", "select");
        platmap.put("all", "all platforms");
        for(UserPlatform plat : TLogic.instance().getSupportedPlatforms()) {
            platmap.put(plat.name(), plat.toString());
        }

        actionmap = new HashMap<>(); 
        actionmap.put("select", "select");
        for(PAct p : PAct.values()) {
            actionmap.put(p.name(), p.toString());
        }
    }

    private void renderSettingsLayout() {
        WidgetSettings tsets = tskr.settings();
        tskr.getLogger().trace("SETTINGS JSON"); //DEBUG
        tskr.getLogger().trace(tskr.settings().getJson()); //DEBUG
        JsonObject jsets = tsets.getJson();

        WidgetSettingsLayout tLayout = new WidgetSettingsLayout();

        // POINTS
        WidgetSettingsSection sectionPoints = new WidgetSettingsSection("points","points");

        sectionPoints.addItem(WidgetSettingsItem.asNumber("watchtime", "points per hour watched", 100, 1, Integer.MIN_VALUE, Integer.MAX_VALUE));
        // offline multipliers
        sectionPoints.addItem(WidgetSettingsItem.asNumber("offline_bonus_mult", "offline bonus multiplier", 2, 0.001, Integer.MIN_VALUE, Integer.MAX_VALUE));
        sectionPoints.addItem(WidgetSettingsItem.asNumber("offline_chat_mult", "offline chat bonus multiplier", 0.25, 0.001, Integer.MIN_VALUE, Integer.MAX_VALUE));
        // lurk multiplier
        sectionPoints.addItem(WidgetSettingsItem.asNumber("lurk_mult", "lurk multiplier", 1.25, 0.001, Integer.MIN_VALUE, Integer.MAX_VALUE));
        //raider_bonus
        sectionPoints.addItem(WidgetSettingsItem.asNumber("raider_bonus", "host bonus per raider", 100, 0.001, Integer.MIN_VALUE, Integer.MAX_VALUE));
        //dono_per_unit
        sectionPoints.addItem(WidgetSettingsItem.asNumber("dono_per_unit", "donation bonus per USD", 100, 0.001, Integer.MIN_VALUE, Integer.MAX_VALUE));


        tLayout.addSection(sectionPoints);
        
        // BONUSES
        WidgetSettingsSection sectionBonuses = new WidgetSettingsSection("bonuses","edit bonuses");

        sectionBonuses.addItem(WidgetSettingsItem.asDropdown("s_platforms", "change bonuses for", "select", platmap));

        String s_platforms = tsets.getString("bonuses.s_platforms", "select");
        if(!s_platforms.equals("select")) {
            if(s_platforms.equals("all")) {
                sectionBonuses.addItem(WidgetSettingsItem.asDropdown("s_actions", "change action", "select", actionmap));
                
                String s_actions = tsets.getString("bonuses.s_actions", "select");
                if(!s_actions.equals("select")) {
                    sectionBonuses.addItem(WidgetSettingsItem.asNumber("all_" + s_actions, actionmap.get(s_actions) + " point bonus", 0, 1, Integer.MIN_VALUE, Integer.MAX_VALUE));
                    sectionBonuses.addItem(WidgetSettingsItem.asCheckbox("apply_all", "apply to all platforms", false));
                    if(tsets.getBoolean("bonuses.apply_all", false)) {
                        tskr.getLogger().trace("setting settings for all " + s_actions);
                        for(UserPlatform plat : TLogic.instance().getSupportedPlatforms()) {
                            jsets.put("bonuses." + plat.name() + "_" + s_actions, tsets.getNumber("bonuses.all_" + s_actions));
                        }
                        //notify user
                        jsets.put("bonuses.apply_all", false);
                        tskr.setSettings(jsets);
                        return;
                    }
                }
            } else {
                List<WidgetSettingsItem> wsil = getPlatformBonusItems(s_platforms);
                for(WidgetSettingsItem wsi : wsil) {
                    sectionBonuses.addItem(wsi);
                }
            }

        }
        tLayout.addSection(sectionBonuses);

        // ALL BONUSES
        WidgetSettingsSection sectionChannelPoints = new WidgetSettingsSection("channelpoints","platform points");

        for(UserPlatform plat : TLogic.instance().getSupportedPlatforms()) {
            sectionChannelPoints.addItem(WidgetSettingsItem.asNumber(plat.name() + "_mult", plat.toString() + " pts conversion multi.", 0, 0.001, Integer.MIN_VALUE, Integer.MAX_VALUE));
        }
        tLayout.addSection(sectionChannelPoints);

        // WATCHTIME
        WidgetSettingsSection sectionWatchtime = new WidgetSettingsSection("watchtime","watchtime");

        //add help: Tasukaru calculates overall watchtime by recording all provided viewer interactions to make a 'graph' of viewer stream presence. 'Watchtime around interaction' tells Tasukaru how many minutes before and after a message/follow/etc. to assume the viewer was present. 
        sectionWatchtime.addItem(WidgetSettingsItem.asNumber("around_present", "watchtime around interaction (mins.)", 5, 1, 0, Integer.MAX_VALUE));
        //add help: Tasukaru assumes all time inside a viewer's 'interaction chain' is watchtime. After the 'interaction chain timeout', a new message/follow/etc. will not count towards the chain, allowing the intermediary time to be assumed unwatched.
        sectionWatchtime.addItem(WidgetSettingsItem.asNumber("chain_timeout", "interaction chain timeout (mins.)", 20, 1, 0, Integer.MAX_VALUE));
        //lurk
        sectionWatchtime.addItem(WidgetSettingsItem.asNumber("lurk_bonus", "lurk base watchtime (mins.)", 30, 1, 0, Integer.MAX_VALUE));
        sectionWatchtime.addItem(WidgetSettingsItem.asNumber("lurk_chain", "lurk chain timeout (mins.)", 60, 1, 0, Integer.MAX_VALUE));
        sectionWatchtime.addItem(WidgetSettingsItem.asNumber("lurk_end", "lurk end density (chat/5min)", 10, 1, 0, Integer.MAX_VALUE));
        sectionWatchtime.addItem(WidgetSettingsItem.asNumber("lurk_timeout", "lurk timeout (mins.)", 90, 1, 0, Integer.MAX_VALUE));

        tLayout.addSection(sectionWatchtime);
        
        // BEHAVIOURS
        WidgetSettingsSection sectionBehaviours = new WidgetSettingsSection("behaviours","plugin behaviours");
        
        sectionBehaviours.addItem(WidgetSettingsItem.asCheckbox("cull_backups", "delete old backups?", true));
        if(tsets.getBoolean("behaviours.cull_backups", false)) {
            sectionBehaviours.addItem(WidgetSettingsItem.asNumber("backup_size", "max backup folder size MBs", 1024, 1, 1, Integer.MAX_VALUE));
        }
        
        tLayout.addSection(sectionBehaviours);
        
        tskr.setSettingsLayout(tLayout);
    }

    private List<WidgetSettingsItem> getPlatformBonusItems(String s_platforms) {
        List<WidgetSettingsItem> wsil = new ArrayList<>();
        for(PAct p : PAct.values()) {
            double defaultPoints = 0D;
            switch(p) {
                case CHANNELPOINTS:
                    defaultPoints = 5D;
                    break;
                case DONATE:
                    defaultPoints = 100D;
                    break;
                case FOLLOW:
                    defaultPoints = 100D;
                    break;
                case JOIN:
                    break;
                case LISTED:
                    break;
                case MESSAGE:
                    defaultPoints = 5D;
                    break;
                case RAID:
                    defaultPoints = 500D;
                    break;
                case SUBSCRIBE:
                    defaultPoints = 500D;
                    break;
            }
            wsil.add(WidgetSettingsItem.asNumber(s_platforms + "_" + p.name(), p.toString() + " point bonus", defaultPoints, 1, Integer.MIN_VALUE, Integer.MAX_VALUE));
        }
        return wsil;
    }

    public void onSettingsUpdate() {
        renderSettingsLayout();
    }
}
