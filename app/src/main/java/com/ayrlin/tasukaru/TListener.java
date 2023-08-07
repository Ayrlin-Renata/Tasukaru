package com.ayrlin.tasukaru;

import co.casterlabs.koi.api.listener.*;
import co.casterlabs.koi.api.types.events.*;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class TListener implements KoiEventListener {

    private FastLogger log;

    public TListener(FastLogger fl) {
        log = fl;
        log.debug("Tasukaru()");
    }

    @KoiEventHandler
    public void onViewerCount(ViewerCountEvent e) {
        log.debug("Tasukaru recieved ViewerCountEvent.");

    }

    @KoiEventHandler
    public void onViewerList(ViewerListEvent e) {
        log.debug("Tasukaru recieved ViewerListEvent.");

    }

    @KoiEventHandler
    public void onViewerJoin(ViewerJoinEvent e) {
        log.debug("Tasukaru recieved ViewerJoinEvent.");

    }

    @KoiEventHandler
    public void onViewerLeave(ViewerLeaveEvent e) {
        log.debug("Tasukaru recieved ViewerLeaveEvent.");

    }

    // @KoiEventHandler
    // public void onChannelPoints(ChannelPointsEvent e) {
    // log.debug("Tasukaru recieved ChannelPointsEvent.");

    // }

    // // "clear chat happens when you clear the chat OR when a user gets banned and
    // // their messages get pruned"
    // @KoiEventHandler
    // public void onClearChat(ClearChatEvent e) {
    // log.debug("Tasukaru recieved ClearChatEvent.");

    // }

    // @KoiEventHandler
    // public void onFollow(FollowEvent e) {
    // log.debug("Tasukaru recieved FollowEvent.");

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

    // @KoiEventHandler
    // public void onStreamStatus(StreamStatusEvent e) {
    // log.debug("Tasukaru recieved StreamStatusEvent.");

    // }

    // @KoiEventHandler
    // public void onSubscription(SubscriptionEvent e) {
    // log.debug("Tasukaru recieved SubscriptionEvent.");

    // }

    // // gets called a bunch randomly
    // @KoiEventHandler
    // public void onUserUpdate(UserUpdateEvent e) {
    // log.debug("Tasukaru recieved UserUpdateEvent.");

    // }

}
