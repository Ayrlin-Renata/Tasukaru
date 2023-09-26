package com.ayrlin.tasukaru;

import java.util.ArrayList;
import java.util.List;

import com.ayrlin.tasukaru.data.AccountInfo;
import com.ayrlin.tasukaru.data.EventInfo;
import com.ayrlin.tasukaru.data.ViewerInfo;
import com.ayrlin.tasukaru.data.EventInfo.AAct;
import com.ayrlin.tasukaru.data.EventInfo.PAct;
import com.ayrlin.tasukaru.data.EventInfo.TAct;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetSettings;
import co.casterlabs.koi.api.types.user.UserPlatform;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class TLogic {
    private static TLogic instance;
    private FastLogger log;
    private WidgetSettings tsets;
    private VBHandler vb;
    private List<String> supportedPlatforms;

    private TLogic() {
        this.log = Tasukaru.instance().getLogger();
        this.tsets = Tasukaru.instance().settings();
        this.vb = VBHandler.instance();
    }

    /**
     * singleton
     * @return THE TLogic
     */
    public static TLogic instance() {
        if(instance == null) {
            instance = new TLogic();
        }
        return instance;
    }

    public void incoming(EventInfo ei) {
        if(processEvent(ei)) {
            vb.addHistory(ei.processed());
        } else {
            vb.addHistory(ei);
        }
        //calculate interaction time
        //processWatchtime(ei);
    }

    public boolean processEvent(EventInfo ei) {
        log.debug("processing " + ei.uptype + " event for " + ei.action + " of " + ei.account.displayname);
        log.trace(ei);

        // find or add account
        int accountId = vb.findAccountId(ei.account);
        if (accountId == -2) {
            log.severe("aborting incoming event: error finding account id for event: \n" + ei);
            return false;
        } else if (accountId == -1) {
            accountId = vb.addAccount(ei.account);
            if(accountId < 0) {
                if(accountId == -1) {
                    //sqlerror
                    log.severe("error adding account, aborting incoming event: \n" + ei);
                    return false;
                }
                if(accountId == -2) {
                    //no latestSnapshot update for unexpected reasons
                    if(!vb.updateAccount(ei.account)) {
                        //failed retry
                        log.warn("added account missing latestSnapshot, silently continuing incoming event: \n" + ei);
                    }
                }
            }
        }
        if (accountId >= 0) {
            if(!vb.verifyCurrentAccountInfo(ei.account)) {
                ei.account.fillDefaults(vb.retrieveCurrentAccountInfo(ei.account.id));
                vb.updateAccount(ei.account);
            } 
            if(ei.action == PAct.MESSAGE && ei.account.badges.isEmpty()) { 
                //special case for badges
                AccountInfo curAI = vb.retrieveCurrentAccountInfo(ei.account.id);
                if(!curAI.badges.isEmpty()) {
                    ei.account.fillDefaults(curAI);
                    ei.account.badges(new ArrayList<>());
                    vb.updateAccount(ei.account);
                }
            }
            ei.snapshotId = vb.findLatestSnapshot(ei.account.id);
        }
        // find or add viewer
        if(ei.account.vid <= 0) {
            log.trace("searching for viewer for account " + ei.account.id);
            ViewerInfo vi = vb.findViewer(ei.account);
            if(vi == null) {
                log.debug("unable to find viewer based on account: \n" + ei.account);
                long vkey = vb.addViewer(ei.account);
                if(vkey < 0) {
                    log.severe("unable to add viewer based on account: \n" + ei.account);
                    return false;
                }
                log.debug("fetching newly added viewer \n" + vkey);
                vi = vb.retrieveViewer(vkey);
            }
            ei.account.vid(vi.id);
            ei.viewer = vi;
        } else {
            log.trace("fetching viewer for account " + ei.account.id + " with vid " + ei.account.vid);
            ViewerInfo vi = vb.retrieveViewer(ei.account.vid);
            ei.account.vid(vi.id);
            ei.viewer = vi;
        }

        // follow through with event actions
        if (processEventActions(ei)) {
            return true;
        }
        return false;
    }

    private boolean processEventActions(EventInfo ei) {
        switch (ei.uptype) {
            case PRESENT:
                switch ((PAct) ei.action) {
                    case JOIN:
                    case MESSAGE:
                    case FOLLOW:
                    case SUBSCRIBE:
                        Long points = tsets.getNumber("bonuses." + ei.account.platform + "_" + ((PAct)ei.action).name()).longValue();
                        vb.addPoints(ei, points);
                        break;
                    case DONATE:
                        Long d_points = tsets.getNumber("bonuses." + ei.account.platform + "_" + ((PAct)ei.action).name()).longValue();
                        vb.addPoints(ei, d_points);
                        // featurecreep: add points based on value
                        break;
                }
                break;
            case ABSENT:
                switch ((AAct) ei.action) {
                    case LEAVE:

                        break;
                }
                break;
            case TECHNICAL:
                switch ((TAct) ei.action) {
                    case SNAPSHOT:

                        break;
                    case POINTS:

                        break;
                }
                break;
        }
        return true;
    }

    // public boolean processWatchtime(EventInfo ei) {
    //     int neededCount = 2;
    //     if(ei.viewer.lurking) {//TODO implement lurking in viewerdata
    //         neededCount = tsets.getNumber("watchtime.lurk_end").intValue();
    //     }
    //     List<EventInfo> eis = vb.retrieveLastViewerInteractions(ei.viewer, neededCount);
    //     EventInfo lastEI = eis.get(1); //second event aka prior to ei
    //     long since = timeBetweenEvents(ei,lastEI);
    //     //consider around time
    //     //consider chain
    //     //consider end lurk
    //     //assign points
    //     //TODO
    //     return true;
    // }

    // private long timeBetweenEvents(EventInfo e1, EventInfo e2) {
    //     return Math.abs(e1.timestamp - e2.timestamp);
    // }

    public List<String> getSupportedPlatforms() {
        if(supportedPlatforms != null) return supportedPlatforms;

        supportedPlatforms = new ArrayList<>();
        for (UserPlatform plat : UserPlatform.values()) {
            if(plat == UserPlatform.CASTERLABS_SYSTEM || plat == UserPlatform.CUSTOM_INTEGRATION) {
                continue; //these are for system messages and integrations like Ko-Fi
            }
            supportedPlatforms.add(plat.name());
        }
        log.debug("Found supported CL platforms: \n" + supportedPlatforms);
        return supportedPlatforms;
    }
}
