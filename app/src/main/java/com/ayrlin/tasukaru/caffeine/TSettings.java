package com.ayrlin.tasukaru.caffeine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ayrlin.sqlutil.SQLUtil;
import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.DefParam;
import com.ayrlin.sqlutil.query.data.OpParam.Op;
import com.ayrlin.tasukaru.TLogic;
import com.ayrlin.tasukaru.Tasukaru;
import com.ayrlin.tasukaru.VBHandler;
import com.ayrlin.tasukaru.data.AccountInfo;
import com.ayrlin.tasukaru.data.EventInfo.PAct;
import com.ayrlin.tasukaru.data.handler.AccountHandler;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetSettings;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.element.JsonObject;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

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
        //pre-render settings
        TConfig.init();
        //build dropdowns
        Tasukaru.instance().getLogger().trace("building maps");
        this.buildMaps();
        //render
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
        List<UserPlatform> platforms = TLogic.instance().getSupportedPlatforms();

        WidgetSettingsLayout tLayout = new WidgetSettingsLayout();

        // POINTS
        WidgetSettingsSection sectionPoints = new WidgetSettingsSection("points","points");

        sectionPoints.addItem(WidgetSettingsItem.asNumber(
                TConfig.Points.watchtime.column, 
                "points per hour watched",
                (long) TConfig.Points.watchtime.defaultValue, 
                1, 
                Integer.MIN_VALUE, Integer.MAX_VALUE));

        // offline multipliers
        sectionPoints.addItem(WidgetSettingsItem.asNumber(
                TConfig.Points.offline_bonus_mult.column, 
                "offline bonus multiplier",
                (double) TConfig.Points.offline_bonus_mult.defaultValue, 
                0.001, 
                Integer.MIN_VALUE, Integer.MAX_VALUE));
        sectionPoints.addItem(WidgetSettingsItem.asNumber(
                TConfig.Points.offline_chat_mult.column, 
                "offline chat bonus multiplier",
                (double) TConfig.Points.offline_chat_mult.defaultValue, 
                0.001, 
                Integer.MIN_VALUE, Integer.MAX_VALUE));

        // lurk multiplier
        sectionPoints.addItem(WidgetSettingsItem.asNumber(
                TConfig.Points.lurk_mult.column, 
                "lurk multiplier",
                (double) TConfig.Points.lurk_mult.defaultValue, 
                0.001, 
                Integer.MIN_VALUE, Integer.MAX_VALUE));

        //raider_bonus
        sectionPoints.addItem(WidgetSettingsItem.asNumber(
                TConfig.Points.raider_bonus.column, 
                "host bonus per raider",
                (long) TConfig.Points.raider_bonus.defaultValue, 
                0.001, 
                Integer.MIN_VALUE, Integer.MAX_VALUE));

        //dono_per_unit
        sectionPoints.addItem(WidgetSettingsItem.asNumber(
                TConfig.Points.dono_per_unit.column, 
                "donation bonus per USD",
                (long) TConfig.Points.dono_per_unit.defaultValue, 
                0.001, 
                Integer.MIN_VALUE, Integer.MAX_VALUE));

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
                        for(UserPlatform plat : platforms) {
                            jsets.put("bonuses." + plat.name() + "_" + s_actions, tsets.getNumber("bonuses.all_" + s_actions));
                        }
                        //notify user
                        jsets.put("bonuses.apply_all", false);
                        tskr.setSettings(jsets);
                        return;
                    }
                }
            } else {
                for(PAct p : PAct.values()) {
                    DefParam dp = TConfig.Bonuses.bonuses.get(s_platforms + "_" + p.name());
                    sectionBonuses.addItem(WidgetSettingsItem.asNumber(
                            dp.column, 
                            p.toString() + " point bonus", 
                            (long) dp.defaultValue, 
                            1, 
                            Integer.MIN_VALUE, Integer.MAX_VALUE));
                }
            }

        }
        tLayout.addSection(sectionBonuses);

        // CHANNEL POINTS
        WidgetSettingsSection sectionChannelPoints = new WidgetSettingsSection("channelpoints","platform points");

        for(UserPlatform plat : platforms) {
            DefParam dp = TConfig.ChannelPoints.conversions.get(plat.name() + "_mult");
            sectionChannelPoints.addItem(WidgetSettingsItem.asNumber(
                    dp.column, 
                    plat.toString() + " pts conversion multi.", 
                    (double) dp.defaultValue, 
                    0.001, 
                    Integer.MIN_VALUE, Integer.MAX_VALUE));
        }
        tLayout.addSection(sectionChannelPoints);

        // WATCHTIME
        WidgetSettingsSection sectionWatchtime = new WidgetSettingsSection("watchtime","watchtime");

        //add help: Tasukaru calculates overall watchtime by recording all provided viewer interactions to make a 'graph' of viewer stream presence. 'Watchtime around interaction' tells Tasukaru how many minutes before and after a message/follow/etc. to assume the viewer was present. 
        sectionWatchtime.addItem(WidgetSettingsItem.asNumber(
                TConfig.Watchtime.around_present.column, 
                "watchtime around interaction (mins.)",         
                (long) TConfig.Watchtime.around_present.defaultValue, 
                1, 
                0, Integer.MAX_VALUE));
        //add help: Tasukaru assumes all time inside a viewer's 'interaction chain' is watchtime. After the 'interaction chain timeout', a new message/follow/etc. will not count towards the chain, allowing the intermediary time to be assumed unwatched.
        sectionWatchtime.addItem(WidgetSettingsItem.asNumber(
                TConfig.Watchtime.chain_timeout.column, 
                "interaction chain timeout (mins.)",         
                (long) TConfig.Watchtime.chain_timeout.defaultValue, 
                1, 
                0, Integer.MAX_VALUE));
        //lurk
        sectionWatchtime.addItem(WidgetSettingsItem.asNumber(
                TConfig.Watchtime.lurk_bonus.column, 
                "lurk base watchtime (mins.)",         
                (long) TConfig.Watchtime.lurk_bonus.defaultValue, 
                1, 
                0, Integer.MAX_VALUE));
        sectionWatchtime.addItem(WidgetSettingsItem.asNumber(
                TConfig.Watchtime.lurk_chain.column, 
                "lurk chain timeout (mins.)",         
                (long) TConfig.Watchtime.lurk_chain.defaultValue, 
                1, 
                0, Integer.MAX_VALUE));
        sectionWatchtime.addItem(WidgetSettingsItem.asNumber(
                TConfig.Watchtime.lurk_end.column, 
                "lurk end density (chat/5min)",         
                (long) TConfig.Watchtime.lurk_end.defaultValue, 
                1, 
                0, Integer.MAX_VALUE));
        sectionWatchtime.addItem(WidgetSettingsItem.asNumber(
                TConfig.Watchtime.lurk_timeout.column, 
                "lurk timeout (mins.)",         
                (long) TConfig.Watchtime.lurk_timeout.defaultValue, 
                1, 
                0, Integer.MAX_VALUE));

        tLayout.addSection(sectionWatchtime);

        // MODERATION
        WidgetSettingsSection sectionModeration = new WidgetSettingsSection("moderation","moderation");
        
        //TODO Tasukaru can automatically detect which users are mods based on their chat badges.
        sectionModeration.addItem(WidgetSettingsItem.asCheckbox(
                TConfig.Moderation.detect_mods.column, 
                "auto-detect mods", 
                (boolean) TConfig.Moderation.detect_mods.defaultValue));
        for(UserPlatform plat : platforms) {
            DefParam dp = TConfig.Moderation.mods.get(plat.name() + "_mods");
            sectionModeration.addItem(WidgetSettingsItem.asTextArea(
                    dp.column, 
                    plat.toString() + " mod accounts", 
                    (String) dp.defaultValue, 
                    "enter account id's or usernames on each line (not display names)",
                    3));
                }
        sectionModeration.addItem(WidgetSettingsItem.asCheckbox(
                "apply_mods", 
                "apply mod selection", 
                false));
        if(tsets.getBoolean("moderation.apply_mods")) {
            for(UserPlatform plat : platforms) {
                applyMods(tsets, plat);
            }
            jsets.put("moderation.apply_mods", false);
            tskr.setSettings(jsets);
            return;
        }
                    
        tLayout.addSection(sectionModeration);

        // BEHAVIOURS
        WidgetSettingsSection sectionBehaviours = new WidgetSettingsSection("behaviours","settings");
        
        sectionBehaviours.addItem(WidgetSettingsItem.asCheckbox(
                TConfig.Behaviours.cull_backups.column, 
                "delete old backups?", 
                (boolean) TConfig.Behaviours.cull_backups.defaultValue));
        if(tsets.getBoolean("behaviours.cull_backups", false)) {
            sectionBehaviours.addItem(WidgetSettingsItem.asNumber(
                    TConfig.Behaviours.backup_size.column, 
                    "max backup folder size MBs", 
                    (long) TConfig.Behaviours.backup_size.defaultValue, 
                    1, 
                    1, Integer.MAX_VALUE));
        }
        
        tLayout.addSection(sectionBehaviours);
        
        tskr.setSettingsLayout(tLayout);
    }

    private void applyMods(WidgetSettings tsets, UserPlatform plat) {
        FastLogger log = Tasukaru.instance().getLogger();
        log.trace("applying mod settings for " + plat.toString());
        String modString = tsets.getString("moderation." + plat.name() + "_mods");

        AccountHandler ah = VBHandler.instance().getAccountHandler();
        List<AccountInfo> possibilities;
        List<String> mods; 
        List<Long> aids = new ArrayList<>(); 

        if(modString.isEmpty() || modString.isBlank()) {
            log.debug("no specified mods " + plat);
        } else {
            mods = new ArrayList<>(Arrays.asList(modString.split("\r?\n|\r")));
            if(mods.isEmpty()) {
                log.debug("no parsed mods " + plat);
            } else {
                possibilities = mods.stream()
                        .flatMap((String mod) -> 
                            Stream.of(
                                new AccountInfo()
                                    .set("userid", mod)
                                    .set("platform", plat.name()),
                                new AccountInfo()
                                    .set("username", mod)
                                    .set("platform", plat.name())))
                        .collect(Collectors.toList());
                if(mods.isEmpty()) {
                    log.debug("no mod account candidates " + plat);
                } else {
                    aids = ah.findAccountIds(possibilities);
                }
            }
        }
        ah.updateOnAccounts(
                    aids, 
                    new DefParam(DataType.BOOL, "mod", true, false), 
                    SQLUtil.qol(DataType.STRING, "platform", Op.EQUAL, plat.name()));
        //TODO user feedback
    }

    public void onSettingsUpdate() {
        renderSettingsLayout();
    }
}
