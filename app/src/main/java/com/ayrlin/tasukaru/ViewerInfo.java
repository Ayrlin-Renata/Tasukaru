package com.ayrlin.tasukaru;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.User.UserRoles;

public class ViewerInfo {
    public int id = -1; // actual SQL table id
    public String userId; // similar to koi.api.types.user.User.id
    public String channelId;
    public String platform;
    public String UPID;
    public List<UserRoles> roles;
    public List<String> badges;
    public String color;
    public String username;
    public String displayname;
    public String bio;
    public String link;
    public String imageLink;
    public long followersCount;
    public long subCount;
    public long watchtime;
    public long tskrpoints;

    public ViewerInfo() {

    }

    public ViewerInfo(User user) {
        this.userId = user.getId();
        this.channelId = user.getChannelId();
        this.platform = user.getPlatform().name();
        this.UPID = user.getUPID();
        this.roles = user.getRoles();
        this.badges = user.getBadges();
        this.color = user.getColor();
        this.username = user.getUsername();
        this.displayname = user.getDisplayname();
        this.bio = user.getBio();
        this.link = user.getLink();
        this.imageLink = user.getImageLink();
        this.followersCount = user.getFollowersCount();
        this.subCount = user.getSubCount();
    }

    public ViewerInfo id(int id) {
        this.id = id;
        return this;
    }

    public ViewerInfo userId(String userId) {
        this.userId = userId;
        return this;
    }

    public ViewerInfo channelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public ViewerInfo platform(String platformname) {
        this.platform = platformname;
        return this;
    }

    public ViewerInfo upid(String upid) {
        this.UPID = upid;
        return this;
    }

    // comma delimited pls
    public ViewerInfo roles(String roles) {
        return rolesFromStringList(Arrays.asList(roles.split(",", -1)));
    }

    // darn java wont let me overload roles with different typed lists
    public ViewerInfo rolesFromStringList(List<String> roles) {
        return roles(roles.stream()
                .map(UserRoles::valueOf)
                .collect(Collectors.toList()));
    }

    public ViewerInfo roles(List<UserRoles> roles) {
        this.roles = roles;
        return this;
    }

    // comma delimited pls
    public ViewerInfo badges(String badges) {
        return badges(Arrays.asList(badges.split(",", -1)));
    }

    public ViewerInfo badges(List<String> badges) {
        this.badges = badges;
        return this;
    }

    public ViewerInfo color(String color) {
        this.color = color;
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

    public ViewerInfo bio(String bio) {
        this.bio = bio;
        return this;
    }

    public ViewerInfo link(String link) {
        this.link = link;
        return this;
    }

    public ViewerInfo imageLink(String imageLink) {
        this.imageLink = imageLink;
        return this;
    }

    public ViewerInfo followersCount(long followersCount) {
        this.followersCount = followersCount;
        return this;
    }

    public ViewerInfo subCount(long subCount) {
        this.subCount = subCount;
        return this;
    }

    public ViewerInfo watchtime(long watchtime) {
        this.watchtime = watchtime;
        return this;
    }

    public ViewerInfo tskrpoints(long tskrpoints) {
        this.tskrpoints = tskrpoints;
        return this;
    }

    public String getRoles() {
        return roles.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    public String getBadges() {
        return badges.stream().map(Object::toString).collect(Collectors.joining(","));
    }

}
