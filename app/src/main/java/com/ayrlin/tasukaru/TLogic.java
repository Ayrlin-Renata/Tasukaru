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
        log.debug("processing " + ei.uptype + " event for " + ei.action + " of " + ei.account.displayname);
        log.trace(ei);

        // find or add viewer
        int accountId = vb.findAccountId(ei.account);
        if (accountId == -2) {
            log.severe("aborting incoming event: error finding viewer id for event: \n" + ei);
            return false;
        } else if (accountId == -1) {
            accountId = vb.addAccount(ei.account);
            if(accountId < 0) {
                if(accountId == -1) {
                    //sqlerror
                    log.severe("error adding viewer, aborting incoming event: \n" + ei);
                    return false;
                }
                if(accountId == -2) {
                    //no latestSnapshot update for unexpected reasons
                    if(!vb.updateAccount(ei.account)) {
                        //failed retry
                        log.warn("added viewer missing latestSnapshot, silently continuing incoming event: \n" + ei);
                    }
                }
            }
        }
        if (accountId >= 0) {
            if(!vb.verifyCurrentAccountInfo(ei.account)) {
                ei.account.fillDefaults(vb.retrieveCurrentAccountInfo(ei.account.id));
                vb.updateAccount(ei.account);
            } 
            ei.snapshotId = vb.findLatestSnapshot(ei.account.id);
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

    public static void fillAccountTableHoles(FastLogger log, VBHandler vb) {
        //find holes
        List<Long> aids = vb.listAllAccounts();
        for (long aid : aids) {
            log.trace("filling holes for viewer " + aid);
            AccountInfo vi = vb.retrieveCurrentAccountInfo(aid);
            List<Parameter> missingList = vi.listUnfilledValues();
            if(missingList.isEmpty()) continue;
            for (Parameter p : missingList) {
                Parameter sp = vb.findLastSnapshotValue(vi.id, p);
                if(sp.value != null) {
                    vi.modifyFromParameter(sp);
                }
            }
            vb.updateAccount(vi);
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
