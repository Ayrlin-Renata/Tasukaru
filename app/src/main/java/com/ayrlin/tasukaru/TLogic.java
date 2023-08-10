package com.ayrlin.tasukaru;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class TLogic {
    FastLogger log;
    VBHandler vb;

    public TLogic(FastLogger log, VBHandler vb) {
        this.log = log;
        this.vb = vb;
    }

    public boolean incoming(EventInfo ei) {
        log.debug("processing " + ei.uptype + " event for " + ei.action + " of " + ei.viewer.displayname);

        // find or add viewer
        int viewerId = vb.findUserId(ei.viewer.username, ei.viewer.platform);
        if (viewerId < 0) {
            vb.addViewer(ei.viewer.username, ei.viewer.displayname, ei.viewer.platuserid, ei.viewer.platform);
            // TODO add history event for adding viewer
            viewerId = vb.findUserId(ei.viewer.username, ei.viewer.platform);
            if (viewerId < 0) {
                log.severe("viewerId still not existing after adding viewer, aborting incoming event");
                return false;
            }
        }
        if (viewerId >= 0) {
            ei.viewer.userId = viewerId;
            // TODO update viewer table info here
        }

        // follow through with event actions
        if (processEventActions(ei)) {
            // add event to database if action success
            addHistEvent(ei);
            return true;
        }
        // if failure to process actions, incoming failed
        return false;
    }

    private boolean processEventActions(EventInfo ei) {
        boolean actSuccess = true;
        switch (ei.uptype) {
            case "present":
                switch (ei.action) {
                    case "join":

                        break;
                    case "message":

                        // TODO add points
                        break;
                    case "follow":

                        // TODO add points
                        break;
                    case "subscribe":

                        // TODO add points based on value
                        break;
                    case "donate":

                        // TODO add points based on value
                        break;
                }
                break;
            case "absent":
                switch (ei.action) {
                    case "leave":

                        break;
                }
                break;
            case "technical":
                switch (ei.action) {
                    case "updateViewerData":

                        break;
                }
                break;
            default:
                log.severe("malformed event: unknown event uptype");
                actSuccess = false;
                break;
        }
        return actSuccess;
    }

    private void addHistEvent(EventInfo ei) {
        vb.addHistory(ei.viewer.userId, ei.uptype, ei.action, ei.value, ei.timestamp);
        log.trace("adding " + ei.uptype + "  history event for " + ei.action + " of "
                + ei.viewer.displayname);
    }
}
