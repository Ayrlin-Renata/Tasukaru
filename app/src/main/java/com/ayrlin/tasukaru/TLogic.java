package com.ayrlin.tasukaru;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class TLogic {
    FastLogger log;
    VBHandler vb;

    public TLogic(FastLogger log, VBHandler vb) {
        this.log = log;
        this.vb = vb;
    }

    public void incoming(EventInfo ei) {
        log.debug("processing " + ei.uptype + " event for " + ei.action + " of " + ei.viewer.displayname);
        int viewerId = vb.findUserId(ei.viewer.username, ei.viewer.platform);
        if (viewerId < 0) {
            vb.addViewer(ei.viewer.username, ei.viewer.displayname, ei.viewer.platuserid, ei.viewer.platform);
            // TODO add history event for adding viewer
            viewerId = vb.findUserId(ei.viewer.username, ei.viewer.platform);
            if (viewerId < 0) {
                log.severe("viewerId still not existing after adding viewer, aborting incoming event");
                return;
            }
        }
        // TODO update viewer table info here
        switch (ei.uptype) {
            case "present":
                switch (ei.action) {
                    case "join":
                        vb.addHistory(viewerId, ei.uptype, ei.action, ei.value, ei.timestamp);
                        log.trace("adding " + ei.uptype + "  history event for " + ei.action + " of "
                                + ei.viewer.displayname);
                        break;
                    case "message":
                        vb.addHistory(viewerId, ei.uptype, ei.action, ei.value, ei.timestamp);
                        log.trace("adding " + ei.uptype + "  history event for " + ei.action + " of "
                                + ei.viewer.displayname);
                        // TODO add points
                        break;
                    case "donation":
                        vb.addHistory(viewerId, ei.uptype, ei.action, ei.value, ei.timestamp);
                        log.trace("adding " + ei.uptype + "  history event for " + ei.action + " of "
                                + ei.viewer.displayname);
                        // TODO add points based on value
                        break;
                }
                break;
            case "absent":
                switch (ei.action) {
                    case "leave":
                        vb.addHistory(viewerId, ei.uptype, ei.action, ei.value, ei.timestamp);
                        log.trace("adding " + ei.uptype + "  history event for " + ei.action + " of "
                                + ei.viewer.displayname);
                        break;
                }
                break;
            default:
                log.severe("malformed event: unknown event uptype");
                break;
        }
    }
}
