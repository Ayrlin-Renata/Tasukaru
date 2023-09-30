package com.ayrlin.tasukaru;

import com.ayrlin.tasukaru.data.AccountInfo;
import com.ayrlin.tasukaru.data.EventInfo;
import com.ayrlin.tasukaru.data.EventInfo.AAct;
import com.ayrlin.tasukaru.data.EventInfo.PAct;
import com.ayrlin.tasukaru.data.EventInfo.UpType;

import co.casterlabs.koi.api.listener.*;
import co.casterlabs.koi.api.types.events.*;
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
    public void onViewerCount(ViewerCountEvent e) {
        log.debug("Tasukaru recieved ViewerCountEvent.");
        log.trace(e);
    }

    @KoiEventHandler
    public void onViewerList(ViewerListEvent e) {
        log.debug("Tasukaru recieved ViewerListEvent.");
        log.trace(e);

    }

    @KoiEventHandler
    public void onViewerJoin(ViewerJoinEvent e) {
        log.debug("Tasukaru recieved ViewerJoinEvent.");
        log.trace(e);
        AccountInfo tskrViewerData = new AccountInfo(e.getViewer());
        EventInfo histEvent = new EventInfo()
                .setAccount(tskrViewerData)
                .set("uptype", UpType.PRESENT)
                .set("action", PAct.JOIN);
        tl.incoming(histEvent);
    }

    @KoiEventHandler
    public void onViewerLeave(ViewerLeaveEvent e) {
        log.debug("Tasukaru recieved ViewerLeaveEvent.");
        log.trace(e);
        AccountInfo tskrViewerData = new AccountInfo(e.getViewer());
        EventInfo histEvent = new EventInfo()
                .setAccount(tskrViewerData)
                .set("uptype", UpType.ABSENT)
                .set("action", AAct.LEAVE);
        tl.incoming(histEvent);
    }

    // @KoiEventHandler
    // public void onStreamStatus(StreamStatusEvent e) {
    //     log.debug("Tasukaru recieved StreamStatusEvent.");
    //     log.trace(e);
    // }

    // for messages and also donations
    @KoiEventHandler
    public void onRichMessage(RichMessageEvent e) {
        log.debug("Tasukaru recieved RichMessageEvent.");
        log.trace(e);

        AccountInfo tskrViewerData = new AccountInfo(e.getSender());
        EventInfo histEvent = new EventInfo()
                .setAccount(tskrViewerData)
                .set("event", e)
                .set("uptype", UpType.PRESENT)
                .set("streamState", tl.streamLive(e.getSender().getPlatform())? "live" : "offline"); //is the stream live rn
        if (e.getDonations().isEmpty()) {
            histEvent = histEvent.set("action", PAct.MESSAGE);
        } else {
            histEvent = histEvent.set("action", PAct.DONATE);
            // featurecreep .value(TUtil.usdValue(e.getDonations())); use Currencies from casterlabs util
        }
        tl.incoming(histEvent);
    }

    @KoiEventHandler
    public void onFollow(FollowEvent e) {
        log.debug("Tasukaru recieved FollowEvent.");
        log.trace(e);

        AccountInfo tskrViewerData = new AccountInfo(e.getFollower());
        EventInfo histEvent = new EventInfo()
                .setAccount(tskrViewerData)
                .set("uptype", UpType.PRESENT)
                .set("action", PAct.FOLLOW);
        tl.incoming(histEvent);
    }

    @KoiEventHandler
    public void onSubscription(SubscriptionEvent e) {
        log.debug("Tasukaru recieved SubscriptionEvent.");
        log.trace(e);

        AccountInfo tskrViewerData = new AccountInfo(e.getSubscriber());
        EventInfo histEvent = new EventInfo()
                .setAccount(tskrViewerData)
                .set("uptype", UpType.PRESENT)
                .set("action", PAct.SUBSCRIBE);
        tl.incoming(histEvent);
    }

    @KoiEventHandler
    public void onChannelPoints(ChannelPointsEvent e) {
        log.debug("Tasukaru recieved ChannelPointsEvent.");
        log.trace(e);
    }

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

    // @KoiEventHandler
    // public void onRaid(RaidEvent e) {
    // log.debug("Tasukaru recieved RaidEvent.");

    // }

    // // for messages and also donations
    // @KoiEventHandler
    // public void onRichMessage(RichMessageEvent e) {
    // log.debug("Tasukaru recieved RichMessageEvent.");

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
