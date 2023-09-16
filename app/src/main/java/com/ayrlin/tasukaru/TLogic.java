package com.ayrlin.tasukaru;

import java.util.List;

import com.ayrlin.sqlutil.query.Parameter;

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
        log.trace(ei);

        // find or add viewer
        int viewerId = vb.findViewerId(ei.viewer);
        if (viewerId == -2) {
            log.severe("aborting incoming event: error finding viewer id for event: \n" + ei);
            return false;
        } else if (viewerId == -1) {
            viewerId = vb.addViewer(ei.viewer);
            if(viewerId < 0) {
                if(viewerId == -1) {
                    //sqlerror
                    log.severe("error adding viewer, aborting incoming event: \n" + ei);
                    return false;
                }
                if(viewerId == -2) {
                    //no latestSnapshot update for unexpected reasons
                    if(!vb.updateViewer(ei.viewer)) {
                        //failed retry
                        log.warn("added viewer missing latestSnapshot, silently continuing incoming event: \n" + ei);
                    }
                }
            }
        }
        if (viewerId >= 0) {
            if(!vb.verifyCurrentViewerInfo(ei.viewer)) {
                ei.viewer.fillDefaults(vb.retrieveCurrentViewerInfo(ei.viewer.id));
                vb.updateViewer(ei.viewer);
            } 
            ei.snapshotId = vb.findLatestSnapshot(ei.viewer.id);
        }

        // follow through with event actions
        if (processEventActions(ei)) {
            // add event to database if action success
            vb.addHistory(ei);
            return true;
        }
        // if failure to process actions, incoming failed
        return false;
    }

    public static void fillViewerTableHoles(FastLogger log, VBHandler vb) {
        //find holes
        List<Long> vids = vb.listAllViewers();
        for (long vid : vids) {
            log.trace("filling holes for viewer " + vid);
            ViewerInfo vi = vb.retrieveCurrentViewerInfo(vid);
            List<Parameter> missingList = vi.listUnfilledValues();
            if(missingList.isEmpty()) continue;
            for (Parameter p : missingList) {
                Parameter sp = vb.findLastSnapshotValue(vi.id, p);
                if(sp.value != null) {
                    vi.modifyFromParameter(sp);
                }
            }
            vb.updateViewer(vi);
        }
        log.debug("hole filling complete.");
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
                    case "vsnapshot":

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
}
