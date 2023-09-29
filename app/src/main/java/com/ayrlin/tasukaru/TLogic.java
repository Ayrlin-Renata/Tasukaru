package com.ayrlin.tasukaru;

import java.util.ArrayList;
import java.util.List;

import com.ayrlin.tasukaru.data.AccountInfo;
import com.ayrlin.tasukaru.data.EventInfo;
import com.ayrlin.tasukaru.data.ViewerInfo;
import com.ayrlin.tasukaru.data.EventInfo.AAct;
import com.ayrlin.tasukaru.data.EventInfo.PAct;
import com.ayrlin.tasukaru.data.EventInfo.TAct;
import com.ayrlin.tasukaru.data.EventInfo.UpType;
import com.ayrlin.tasukaru.data.handler.AccountHandler;
import com.ayrlin.tasukaru.data.handler.EventHandler;
import com.ayrlin.tasukaru.data.handler.ViewerHandler;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetSettings;
import co.casterlabs.koi.api.types.user.UserPlatform;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class TLogic {
    private static TLogic instance;
    private FastLogger log;
    private WidgetSettings tsets;
    private VBHandler vb;
    private List<UserPlatform> supportedPlatforms;
    private EventHandler eh;

    private TLogic() {
        this.log = Tasukaru.instance().getLogger();
        this.tsets = Tasukaru.instance().settings();
        this.vb = VBHandler.instance();
        this.eh = vb.getEventHandler();
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
            eh.addToVB(ei.set("processed", "true"));
        } else {
            eh.addToVB(ei);
        }
        //calculate interaction time
        //processWatchtime(ei);
    }

    public boolean processEvent(EventInfo ei) {
        log.debug("processing " + ei.get("uptype") + " event for " + ei.get("action") + " of " + ei.getAccount().get("displayname"));
        log.trace(ei);

        // find or add account
        AccountHandler ah = vb.getAccountHandler();
        long accountId = vb.findAccountId(ei.getAccount());
        if (accountId == -2) {
            log.severe("aborting incoming event: error finding account id for event: \n" + ei);
            return false;
        } else if (accountId == -1) {
            accountId = ah.addToVB(ei.getAccount());
            if(accountId < 0) {
                if(accountId == -1) {
                    //sqlerror
                    log.severe("error adding account, aborting incoming event: \n" + ei);
                    return false;
                }
                if(accountId == -2) {
                    //no latestsnapshot update for unexpected reasons
                    if(!ah.updateToVB(ei.getAccount())) {
                        //failed retry
                        log.warn("added account missing latestsnapshot, silently continuing incoming event: \n" + ei);
                    }
                }
            }
        }

        //make sure account has all fields
        AccountInfo acc = ei.getAccount();
        acc.fillDefaults(VBHandler.instance().getAccountHandler().getFromVB(accountId));
        
        //compare with current data
        if (accountId >= 0) {
            if(!ah.isCurrent(acc)) {
                acc.fillDefaults(ah.getFromVB((long) acc.get("id")));
                ah.updateToVB(acc);
            } 
            @SuppressWarnings("unchecked")
            List<String> b1 = (List<String>) acc.get("badges");
            if(ei.get("action") == PAct.MESSAGE && b1.isEmpty()) { 
                //special case for badges
                AccountInfo curAI = ah.getFromVB((long) acc.get("id"));
                @SuppressWarnings("unchecked")
                List<String> b2 = (List<String>) curAI.get("badges");
                if(!b2.isEmpty()) {
                    acc.fillDefaults(curAI);
                    acc.set("badges", new ArrayList<>());
                    ah.updateToVB(acc);
                }
            }
            ei.set("sid", (int) vb.findLatestSnapshot((long) acc.get("id")));
        }
        // find or add viewer
        ViewerHandler vh = vb.getViewerHandler();
        if((long) acc.get("vid") <= 0) {
            log.trace("searching for viewer for account " + (long) acc.get("id"));
            ViewerInfo vi = vb.findViewer(acc);
            if(vi == null) {
                log.debug("unable to find viewer based on account: \n" + acc);
                long vkey = vh.addViewer(acc);
                if(vkey < 0) {
                    log.severe("unable to add viewer based on account: \n" + acc);
                    return false;
                }
                log.debug("fetching newly added viewer \n" + vkey);
                vi = vh.getFromVB(vkey);
            }
            acc.set("vid", (long) vi.get("id"));
            ei.setViewer(vi);
        } else {
            log.trace("fetching viewer for account " + (long) acc.get("id") + " with vid " + (long) acc.get("vid"));
            ViewerInfo vi = vh.getFromVB((long) acc.get("vid"));
            acc.set("vid", (long) vi.get("id"));
            ei.setViewer(vi);
        }

        // follow through with event actions
        if (processEventActions(ei)) {
            return true;
        }
        return false;
    }

    private boolean processEventActions(EventInfo ei) {
        switch (UpType.valueOf(((String) ei.get("uptype")).toUpperCase())) {
            case PRESENT:
                switch (PAct.valueOf(((String) ei.get("action")).toUpperCase())) {
                    case JOIN:
                    case MESSAGE:
                    case FOLLOW:
                    case SUBSCRIBE:
                        Long points = tsets.getNumber("bonuses." + ei.getAccount().get("platform") + "_" + (PAct.valueOf(((String) ei.get("action")).toUpperCase()).name())).longValue();
                        vb.addPoints(ei, points);
                        break;
                    case DONATE:
                        Long d_points = tsets.getNumber("bonuses." + ei.getAccount().get("platform") + "_" + (PAct.valueOf(((String) ei.get("action")).toUpperCase()).name())).longValue();
                        vb.addPoints(ei, d_points);
                        // featurecreep: add points based on value
                        break;
                }
                break;
            case ABSENT:
                switch (AAct.valueOf(((String) ei.get("action")).toUpperCase())) {
                    case LEAVE:

                        break;
                }
                break;
            case TECHNICAL:
                switch (TAct.valueOf(((String) ei.get("action")).toUpperCase())) {
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

    public List<UserPlatform> getSupportedPlatforms() {
        if(supportedPlatforms != null) return supportedPlatforms;

        supportedPlatforms = new ArrayList<>();
        for (UserPlatform plat : UserPlatform.values()) {
            if(plat == UserPlatform.CASTERLABS_SYSTEM || plat == UserPlatform.CUSTOM_INTEGRATION) {
                continue; //these are for system messages and integrations like Ko-Fi
            }
            supportedPlatforms.add(plat);
        }
        log.debug("Found supported CL platforms: \n" + supportedPlatforms);
        return supportedPlatforms;
    }
}
