package com.ayrlin.tasukaru;

import java.sql.Timestamp;

import com.ayrlin.tasukaru.data.AccountInfo;
import com.ayrlin.tasukaru.data.EventInfo;
import com.ayrlin.tasukaru.data.EventInfo.*;

import co.casterlabs.caffeinated.pluginsdk.Currencies;
import co.casterlabs.koi.api.listener.*;
import co.casterlabs.koi.api.types.events.*;
import co.casterlabs.koi.api.types.events.rich.Donation;
import co.casterlabs.koi.api.types.user.User;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class TListener implements KoiEventListener {
    private static TListener instance;

    private FastLogger log;
    private TLogic tl;

    private TListener() {
        this.log = Tasukaru.instance().getLogger();
        this.tl = TLogic.instance();
    }

    /**
     * singleton
     * @return THE TListener
     */
    public static TListener instance() {
        if(instance == null) {
            instance = new TListener();
        }
        return instance;
    }

    @KoiEventHandler
    public void onViewerList(ViewerListEvent e) { //todo list
        for(User u : e.getViewers()) {
            ingestHelper(UpType.PRESENT, PAct.LISTED, u, e);
        }
    }

    @KoiEventHandler
    public void onViewerJoin(ViewerJoinEvent e) {
        ingestHelper(UpType.PRESENT, PAct.JOIN, e.getViewer(), e);
    }

    @KoiEventHandler
    public void onViewerLeave(ViewerLeaveEvent e) {
        ingestHelper(UpType.ABSENT, AAct.LEAVE, e.getViewer(), e);
    }

    // for messages and also donations
    @KoiEventHandler
    public void onRichMessage(RichMessageEvent e) {
        if(!e.getDonations().isEmpty()) {
            double donoValue = 0D;
            for(Donation dono : e.getDonations()) {
                double trueAmount = dono.getAmount() * dono.getCount();
                double convertedAmount = -1D;
                try {
                    convertedAmount = Currencies.convertCurrency(trueAmount, dono.getCurrency(), Currencies.baseCurrency).await();
                } catch(Throwable t) {
                    log.warn("exception while converting currencies in donation event: " + dono);
                    FastLogger.logException(t);
                }
                donoValue = (convertedAmount < 0)? -1D : convertedAmount;
            }
            if(donoValue < 0) {
                log.warn("donation unable to be appraised!");
                ingestHelper(UpType.PRESENT, PAct.DONATE, e.getSender(), e, -1D);
            } else {
                ingestHelper(UpType.PRESENT, PAct.DONATE, e.getSender(), e, donoValue);
            }
        } else {
            ingestHelper(UpType.PRESENT, PAct.MESSAGE, e.getSender(), e);
        }
    }

    @KoiEventHandler
    public void onFollow(FollowEvent e) {
        ingestHelper(UpType.PRESENT, PAct.FOLLOW, e.getFollower(), e);
    }

    @KoiEventHandler
    public void onSubscription(SubscriptionEvent e) {
        ingestHelper(UpType.PRESENT, PAct.SUBSCRIBE, e.getSubscriber(), e);
    }

    @KoiEventHandler
    public void onRaid(RaidEvent e) { 
        ingestHelper(UpType.PRESENT, PAct.RAID, e.getHost(), e, e.getViewers());
    }

    @KoiEventHandler
    public void onChannelPoints(ChannelPointsEvent e) { 
        ingestHelper(UpType.PRESENT, PAct.CHANNELPOINTS, e.getSender(), e, e.getReward().getCost());
    }

    public void ingestHelper(UpType up, Action act, User user, KoiEvent e, Number value, boolean novalue) {
        log.debug("Tasukaru recieved KoiEvent: " + e.getType());
        log.trace(e);

        AccountInfo ai = new AccountInfo(user);
        EventInfo ei = new EventInfo(Timestamp.from(e.getTimestamp()))
                .setAccount(ai)
                .set("uptype", up)
                .set("action", act)
                .set("origin", Source.KOIEVENT)
                .set("streamState", tl.streamLive(user.getPlatform())? Stream.ONLINE : Stream.OFFLINE);
        if(!novalue) ei.set("value", value.doubleValue());
        tl.incoming(ei);
    }

    public void ingestHelper(UpType up, Action act, User user, KoiEvent e, Number value) {
        ingestHelper(up, act, user, e, value, false);
    }

    public void ingestHelper(UpType up, Action act, User user, KoiEvent e) {
        ingestHelper(up, act, user, e, 0L, true);
    }

    // @KoiEventHandler
    // public void onViewerCount(ViewerCountEvent e) {
    //     log.debug("Tasukaru recieved ViewerCountEvent.");
    //     log.trace(e);
    // }

    // @KoiEventHandler
    // public void onStreamStatus(StreamStatusEvent e) {
    //     log.debug("Tasukaru recieved StreamStatusEvent.");
    //     log.trace(e);
    // }

    // // "clear chat happens when you clear the chat OR when a user gets banned and
    // // their messages get pruned"
    // @KoiEventHandler
    // public void onClearChat(ClearChatEvent e) {
    // log.debug("Tasukaru recieved ClearChatEvent.");

    // }

    // // this might catch all events, maybe
    // @KoiEventHandler
    // public void onKoi(KoiEvent e) {
    // log.debug("Tasukaru recieved KoiEvent.");

    // }

    // @KoiEventHandler
    // public void onLike(LikeEvent e) { 
    // log.debug("Tasukaru recieved LikeEvent.");

    // }

    // // system messages
    // // "one place that i can think of that uses it is youtube, where if you try
    // to
    // // send a message BEFORE you create a live stream we send back a warning
    // using
    // // PLATFORM_MESSAGE"
    // //
    // https://github.com/Casterlabs/Koi/blob/v2/Integration-Youtube/src/main/java/co/casterlabs/koi/integration/youtube/YoutubeProvider.java#L177
    // @KoiEventHandler
    // public void onPlatformMessage(PlatformMessageEvent e) {
    // log.debug("Tasukaru recieved PlatformMessageEvent.");

    // }

    // // "room state is for things like follower only mode, emote only, etc"
    // @KoiEventHandler
    // public void onRoomstate(RoomstateEvent e) {
    // log.debug("Tasukaru recieved RoomstateEvent.");

    // }

    // // gets called a bunch randomly
    // @KoiEventHandler
    // public void onUserUpdate(UserUpdateEvent e) {
    // log.debug("Tasukaru recieved UserUpdateEvent.");

    // }

}
