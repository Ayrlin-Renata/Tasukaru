package com.ayrlin.tasukaru;

import java.util.HashMap;
import java.util.Map;

import com.ayrlin.tasukaru.data.EventInfo.PAct;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetSettings;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;

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
        this.buildMaps();
        // TODO when widgetlayout implemented: this.renderSettingsLayout();
        return true;
    }

    public void buildMaps() {
        platmap.put("select", "select");
        platmap.put("all", "all platforms");
        for(String plat : TLogic.instance().getSupportedPlatforms()) {
            platmap.put(plat, plat.toLowerCase());
        }

        actionmap.put("select", "select");
        for(PAct p : PAct.values()) {
            actionmap.put(p.name(), p.toString());
        }
    }

    private void renderSettingsLayout() {
        WidgetSettings tsets = tskr.settings();
        WidgetSettingsLayout tLayout = new WidgetSettingsLayout();

        // POINTS
        WidgetSettingsSection sectionPoints = new WidgetSettingsSection("points","points");

        sectionPoints.addItem(WidgetSettingsItem.asNumber("watchtime", "points per hour watched", 0, 1, Integer.MIN_VALUE, Integer.MAX_VALUE));
        // TODO offline multipliers
        tLayout.addSection(sectionPoints);
        
        // BONUSES
        WidgetSettingsSection sectionBonuses = new WidgetSettingsSection("bonuses","edit bonuses");

        sectionBonuses.addItem(WidgetSettingsItem.asDropdown("s_platforms", "change bonuses for", "select", platmap));

        String s_platforms = tsets.getString("bonuses.s_platforms");
        if(!s_platforms.equals("select")) {
            if(s_platforms.equals("all")) {
                
                sectionBonuses.addItem(WidgetSettingsItem.asDropdown("s_actions", "change action", "select", actionmap));
                
                String s_actions = tsets.getString("bonuses.s_actions");
                if(!s_actions.equals("select")) {
                    sectionBonuses.addItem(WidgetSettingsItem.asNumber("all_" + s_actions, actionmap.get(s_actions) + " point bonus", 0, 1, Integer.MIN_VALUE, Integer.MAX_VALUE));
                    sectionBonuses.addItem(WidgetSettingsItem.asCheckbox("apply_all", "apply to all platforms", false));
                    if(tsets.getBoolean("bonuses.apply_all")) {
                        for(String plat : TLogic.instance().getSupportedPlatforms()) {
                            tsets.set("bonuses." + plat + "_" + s_actions, tsets.getNumber("bonuses.all_" + s_actions));
                        }
                        //notify user
                        tsets.set("bonuses.apply_all", false);
                    }
                }
            } else {
                for(PAct p : PAct.values()) {
                    sectionBonuses.addItem(WidgetSettingsItem.asNumber(s_platforms + "_" + p.name(), p.toString() + " point bonus", 0, 1, Integer.MIN_VALUE, Integer.MAX_VALUE));
                }
            }

        }
        tLayout.addSection(sectionBonuses);

        // ALL BONUSES
        WidgetSettingsSection sectionBonusesAll = new WidgetSettingsSection("bonuses_all","all bonuses");

        for(String plat : TLogic.instance().getSupportedPlatforms()) {
            for(PAct p : PAct.values()) {
                sectionBonusesAll.addItem(WidgetSettingsItem.asNumber(plat + "_" + p.name(), plat + " " + p.toString(), 0, 1, Integer.MIN_VALUE, Integer.MAX_VALUE));
            }
        }
        tLayout.addSection(sectionBonusesAll);

        // WATCHTIME
        WidgetSettingsSection sectionWatchtime = new WidgetSettingsSection("watchtime","watchtime");

        //add help: Tasukaru calculates overall watchtime by recording all provided viewer interactions to make a 'graph' of viewer stream presence. 'Watchtime around interaction' tells Tasukaru how many minutes before and after a message/follow/etc. to assume the viewer was present. 
        sectionWatchtime.addItem(WidgetSettingsItem.asNumber("around_present", "watchtime around interaction (mins.)", 5, 1, Integer.MIN_VALUE, Integer.MAX_VALUE));
        //add help: Tasukaru assumes all time inside a viewer's 'interaction chain' is watchtime. After the 'interaction chain timeout', a new message/follow/etc. will not count towards the chain, allowing the intermediary time to be assumed unwatched.
        sectionWatchtime.addItem(WidgetSettingsItem.asNumber("chain_timeout", "interaction chain timeout (mins.)", 20, 1, Integer.MIN_VALUE, Integer.MAX_VALUE));

        tLayout.addSection(sectionWatchtime);
        
        // BEHAVIOURS
        WidgetSettingsSection sectionBehaviours = new WidgetSettingsSection("behaviours","plugin behaviours");
        
        sectionBehaviours.addItem(WidgetSettingsItem.asCheckbox("cull_backups", "delete old backups?", true));
        if(tsets.getBoolean("behaviours.cull_backups")) {
            sectionBehaviours.addItem(WidgetSettingsItem.asNumber("backup_size", "max backup folder size MBs", 1024, 1, 1, Integer.MAX_VALUE));
        }
        tLayout.addSection(sectionBehaviours);
        
        tskr.setSettingsLayout(tLayout);
    }

    public void onSettingsUpdate() {
        renderSettingsLayout();
    }
}
