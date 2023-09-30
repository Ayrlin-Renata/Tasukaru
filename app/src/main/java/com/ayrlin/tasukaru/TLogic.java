package com.ayrlin.tasukaru;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.ayrlin.tasukaru.data.AccountInfo;
import com.ayrlin.tasukaru.data.EventInfo;
import com.ayrlin.tasukaru.data.EventInfo.AAct;
import com.ayrlin.tasukaru.data.EventInfo.PAct;
import com.ayrlin.tasukaru.data.EventInfo.Source;
import com.ayrlin.tasukaru.data.EventInfo.TAct;
import com.ayrlin.tasukaru.data.EventInfo.UpType;
import com.ayrlin.tasukaru.data.ViewerInfo;
import com.ayrlin.tasukaru.data.handler.AccountHandler;
import com.ayrlin.tasukaru.data.handler.EventHandler;
import com.ayrlin.tasukaru.data.handler.ViewerHandler;

import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
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
        processWatchtime(ei);
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
            //update ei
            ei.set("aid", acc.get("id"));
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
        float offlineBonusMult = tsets.getNumber("points.offline_bonus_mult").floatValue(); 
        float offlineChatMult = tsets.getNumber("points.offline_chat_mult").floatValue(); 
        float offlineTotalMult = offlineBonusMult;
        UserPlatform platform = UserPlatform.valueOf(ei.getAccount().get("platform").toString());
        boolean offline = !streamLive(platform);

        switch (UpType.valueOf(((String) ei.get("uptype")).toUpperCase())) {
            case PRESENT:
                PAct presAct = PAct.valueOf(((String) ei.get("action")).toUpperCase());
                switch (presAct) {
                    case MESSAGE:
                        offlineTotalMult = offlineChatMult;
                    case JOIN:
                    case FOLLOW:
                    case SUBSCRIBE:
                        Long points = tsets.getNumber("bonuses." + platform.name() + "_" + presAct.name()).longValue();
                        vb.addPoints(ei, (long) (points * (offline? offlineTotalMult : 1F)), presAct);
                        break;
                    case DONATE:
                        Long d_points = tsets.getNumber("bonuses." + platform.name() + "_" + presAct.name()).longValue();
                        vb.addPoints(ei, (long) (d_points * (offline? offlineTotalMult : 1F)), presAct);
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

    public boolean processWatchtime(EventInfo ei) {
        if(!streamLive(UserPlatform.valueOf(ei.getAccount().get("platform").toString()))) {
            log.trace("stream offline, skipping processing for watchtime of " + ei.getViewer().getName());
            return true;
        }
        long timeCountedMs = 0L;

        long neededCount = 2;
        boolean lurking = (boolean) ei.getViewer().get("lurking");
        long lurkEnd = tsets.getNumber("watchtime.lurk_end").longValue(); //in chat/5mins
        if(lurking) {
            neededCount = lurkEnd;
        }

        List<EventInfo> eis = vb.retrieveLastViewerInteractions(ei.getViewer(), neededCount);
        EventInfo lastEI = eis.get(1); //second event aka prior to ei
        long sinceMs = timeBetweenEvents(ei,lastEI);
        
        //consider chain
        long chainTimeoutMs = minsToMs(tsets.getNumber(lurking? "watchtime.lurk_chain" : "watchtime.chain_timeout").longValue());
        boolean chain = sinceMs < chainTimeoutMs;
        
        //consider around time
        long aroundTimeMs = minsToMs(tsets.getNumber("watchtime.around_present").longValue());

        // CALCULATE
        if(chain) {
            timeCountedMs += sinceMs;
            log.trace("extending viewer " + ei.getViewer().getName() + " chain by " + sinceMs);
        } else {
            timeCountedMs += aroundTimeMs * 2; //the end and beginning of the previous chain, since it wasnt counted at the start 
            log.trace("ending viewer " + ei.getViewer().getName() + " chain. adding around time: " + aroundTimeMs * 2);
        }
        
        //consider end lurk
        if(lurking) {
            boolean endLurk = false;
            long lurkTimeoutMs = minsToMs(tsets.getNumber("watchtime.lurk_timeout").longValue());
            if(sinceMs > lurkTimeoutMs) {
                endLurk = true;
                log.trace("ending viewer " + ei.getViewer().getName() + " lurk. timeout: " + lurkTimeoutMs + " time since last: " + sinceMs);
            } else {
                int earliestIndex = (int) Math.min((long) eis.size(), lurkEnd) - 1; //zero based
                EventInfo earliestEI = eis.get(earliestIndex); //earliest event in eis
                long chatDensity = timeBetweenEvents(ei, earliestEI); 
                if(chatDensity > 5L * 60L * 1000L) {
                    endLurk = true;  
                    log.trace("ending viewer " + ei.getViewer().getName() + " lurk. time since earliest interaction (" + earliestEI.get("id") + ") within density: " + chatDensity);
                }
            }
            if(endLurk) {
                ei.getViewer().set("lurking", String.valueOf(false));
            }
        }

        //assign points
        float lurkMult = tsets.getNumber("points.lurk_mult").floatValue(); 
        float pointsPerHr = tsets.getNumber("points.watchtime").floatValue();
        float pointsPerS = pointsPerHr / 3600F;
        long totalPoints = (long) ((timeCountedMs / 60F) * pointsPerS * lurkMult); 
        vb.addPoints(ei, totalPoints, Source.WATCHTIME);
        
        return true;
    }

    private long timeBetweenEvents(EventInfo e1, EventInfo e2) {
        return Math.abs(((Timestamp) e2.get("timestamp")).getTime() - ((Timestamp) e1.get("timestamp")).getTime());
    }

    private long minsToMs(long mins) {
        return mins * 60L * 1000L; //60 s/min, 1000 ms/s
    }

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

    public boolean streamLive(UserPlatform plat) {
        return (Caffeinated.getInstance().getKoi().getStreamStates().get(plat).isLive());
    }
}
