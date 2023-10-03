package com.ayrlin.tasukaru.services;

import java.util.List;

import com.ayrlin.tasukaru.Tasukaru;
import com.ayrlin.tasukaru.VBHandler;
import com.ayrlin.tasukaru.data.AccountInfo;
import com.ayrlin.tasukaru.data.EventInfo;
import com.ayrlin.tasukaru.data.ViewerInfo;
import com.ayrlin.tasukaru.data.EventInfo.Source;
import com.ayrlin.tasukaru.data.handler.AccountHandler;
import com.ayrlin.tasukaru.data.handler.ViewerHandler;

import co.casterlabs.commons.functional.tuples.Triple;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class TasukaruCommandService implements TasukaruCmdSrv {

    private VBHandler vb;
    private ViewerHandler vh;
    private AccountHandler ah;
    private FastLogger log;
    private ViewerInfo viewer;
    private AccountInfo account;

    public TasukaruCommandService() {
        this.vb = VBHandler.instance();
        this.vh = vb.getViewerHandler();
        this.ah = vb.getAccountHandler();
        this.log = Tasukaru.instance().getLogger();
        log.debug("tasukaru commmand service instantiated!");
    }

    
    @Override
    public void setIdentity(Identity i) {
        if(i.clID != null) {
            //vi = vb.getByClID(i.getClID());
            log.warn("using unimplemented features (clId) in Identity!" + i);
        }
        User user = i.user;
        if(user != null) {
            account = ah.getFromVB(ah.findAccountId(new AccountInfo(user)));
        } else if(i.username != null && i.platform != null) {
            account = ah.getFromVB(ah.findAccountId(new AccountInfo()
                .set("username", i.username)
                .set("platform", i.platform.name())));
        } else {
            log.severe("identity is empty! Halting setIdentity() \n" + i);
            return;
        }
        viewer = vh.findViewer(account);
        if(viewer == null) {
            log.severe("unable to find viewer based on Identity!" + i);
        }
    }

    public void setIdentity(String json) {
        log.trace("command service recieved identity: \n" + json);

        Identity i;
        try {
            i = Rson.DEFAULT.fromJson(json, new TypeToken<Identity>() {});
        } catch (Exception e) {
            log.severe("unable to deserialize Identity during command call: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        setIdentity(i);
    }


    @Override
    public long checkPoints(Identity i) {
        setIdentity(i);
        return checkPoints();
    }

    @Override
    public long checkPoints() {
        return (long) viewer.get("points");
    }


    @Override
    public long checkWatchtime(Identity i) {
        setIdentity(i);
        return checkWatchtime();
    }

    @Override
    public long checkWatchtime() {
        return (long) viewer.get("watchtime");
    }


    @Override
    public void addPoints(Identity i, long amount) {
        setIdentity(i);
        addPoints(amount);
    }

    @Override
    public void addPoints(long amount) {
        EventInfo ei = new EventInfo()
                .setViewer(viewer)
                .setAccount(account)
                .set("sid", vb.findLatestSnapshot((long) account.get("id")));
        vh.addPoints(ei, amount, Source.COMMAND);
    }

    @Override
    public void setPoints(Identity i, long amount) {
        setIdentity(i);
        setPoints(amount);
    }

    @Override
    public void setPoints(long amount) {
        addPoints((0 - (long) viewer.get("points")) + amount);
    }

    @Override
    public void setLurk(Identity i, boolean lurking) {
        setIdentity(i);
        setLurk(lurking);
    }

    @Override
    public void setLurk(boolean lurking) {
        viewer.set("lurking", false);
        vh.updateToVB(viewer);
    }

    @Override
    public List<Triple<Long, String, Long>> getPointsLeaderboard(int count, boolean fromTop) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPointsLeaderboard'");
    }

    @Override
    public List<Triple<Long, String, Long>> getWatchtimeLeaderboard(int count, boolean fromTop) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWatchtimeLeaderboard'");
    }

    @Override
    public boolean accountLinkRequest(Identity sender, Identity reciever) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'accountLinkRequest'");
    }

    @Override
    public boolean accountUnlinkRequest(Identity sender, String platform) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'accountUnlinkRequest'");
    }
    
}
