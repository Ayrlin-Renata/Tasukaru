package com.ayrlin.tasukaru.services;

import com.ayrlin.tasukaru.Tasukaru;
import com.ayrlin.tasukaru.VBHandler;
import com.ayrlin.tasukaru.data.AccountInfo;
import com.ayrlin.tasukaru.data.ViewerInfo;
import com.ayrlin.tasukaru.data.handler.AccountHandler;
import com.ayrlin.tasukaru.data.handler.ViewerHandler;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class TSecurity implements TasukaruSecurityService {

    private VBHandler vb;
    private ViewerHandler vh;
    private AccountHandler ah;
    private FastLogger log;
    private ViewerInfo viewer;
    private AccountInfo account;

    public TSecurity() {
        this.vb = VBHandler.instance();
        this.vh = vb.getViewerHandler();
        this.ah = vb.getAccountHandler();
        this.log = Tasukaru.instance().getLogger();
        log.debug("tasukaru security service instantiated!");
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
    public boolean isMod(Identity i, UserPlatform plat) {
        setIdentity(i);
        return isMod(plat);
    }

    @Override
    public boolean isMod(UserPlatform plat) {
        return (boolean) account.get("mod");
    }

    public boolean isMod(String plat) {
        return (boolean) account.get("mod");
    }

    @Override
    public void setMod(Identity i, UserPlatform plat, boolean mod) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setMod'");
    }

    @Override
    public void setMod(UserPlatform plat, boolean mod) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setMod'");
    }
    
}
