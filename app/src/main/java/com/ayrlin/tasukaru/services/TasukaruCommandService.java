package com.ayrlin.tasukaru.services;

import java.util.List;

import com.ayrlin.tasukaru.Tasukaru;
import com.ayrlin.tasukaru.VBHandler;
import com.ayrlin.tasukaru.data.AccountInfo;
import com.ayrlin.tasukaru.data.ViewerInfo;
import com.ayrlin.tasukaru.data.handler.AccountHandler;
import com.ayrlin.tasukaru.data.handler.ViewerHandler;

import co.casterlabs.commons.functional.tuples.Triple;
import co.casterlabs.koi.api.types.user.User;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class TasukaruCommandService implements TasukaruInterop {

    private VBHandler vb;
    private ViewerHandler vh;
    private AccountHandler ah;
    private FastLogger log;

    public TasukaruCommandService() {
        this.vb = VBHandler.instance();
        this.vh = vb.getViewerHandler();
        this.ah = vb.getAccountHandler();
        this.log = Tasukaru.instance().getLogger();
        log.debug("tasukaru commmand service instantiated!");
    }

    @Override
    public long checkPoints(Identity i) {
        ViewerInfo vi = processIdentity(i);
        return (long) vi.get("points");
    }

    @Override
    public long checkWatchtime(Identity i) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkWatchtime'");
    }

    @Override
    public void addPoints(Identity i, long amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addPoints'");
    }

    @Override
    public void setPoints(Identity i, long amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setPoints'");
    }

    @Override
    public void setLurk(Identity i, boolean lurking) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setLurk'");
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

    private ViewerInfo processIdentity(Identity i) {
        ViewerInfo vi = null;
        if(i.clID != null) {
            //vi = vb.getByClID(i.getClID());
            log.warn("using unimplemented features (clId) in Identity!" + i);
        }
        User user = i.user;
        if(user != null) {
            vi = vh.findViewer(ah.getFromVB(ah.findAccountId(new AccountInfo(user))));
        } else if(i.username != null && i.platform != null) {
            vi = vh.findViewer(ah.getFromVB(ah.findAccountId(new AccountInfo()
                .set("username", i.username)
                .set("platform", i.platform.name()))));
        } else {
            log.severe("identity is empty!" + i);
        }
        if(vi == null) {
            log.severe("unable to find viewer based on Identity!" + i);
        }
        return vi;
    }
    
}
