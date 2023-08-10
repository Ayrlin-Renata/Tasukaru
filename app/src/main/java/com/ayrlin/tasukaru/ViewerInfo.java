package com.ayrlin.tasukaru;

import co.casterlabs.koi.api.types.user.User;

public class ViewerInfo {
    public int userId;
    public String username;
    public String displayname;
    public String platuserid;
    public String upid;
    public String platform;
    public int watchtime;
    public int tskrpoints;

    public ViewerInfo() {

    }

    public ViewerInfo(User user) {
        this.username(user.getUsername())
                .displayname(user.getDisplayname())
                .platuserid(user.getId())
                .upid(user.getUPID())
                .platform(user.getPlatform().getStr());
    }

    public ViewerInfo userId(int userId) {
        this.userId = userId;
        return this;
    }

    public ViewerInfo username(String username) {
        this.username = username;
        return this;
    }

    public ViewerInfo displayname(String displayname) {
        this.displayname = displayname;
        return this;
    }

    public ViewerInfo platuserid(String platuserid) {
        this.platuserid = platuserid;
        return this;
    }

    public ViewerInfo upid(String upid) {
        this.upid = upid;
        return this;
    }

    public ViewerInfo platform(String platform) {
        this.platform = platform;
        return this;
    }

    public ViewerInfo watchtime(int watchtime) {
        this.watchtime = watchtime;
        return this;
    }

    public ViewerInfo tskrpoints(int tskrpoints) {
        this.tskrpoints = tskrpoints;
        return this;
    }

}
