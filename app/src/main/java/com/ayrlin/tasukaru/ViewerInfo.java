package com.ayrlin.tasukaru;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.ayrlin.sqlutil.query.Parameter;
import com.ayrlin.sqlutil.query.Parameter.DataType;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.User.UserRoles;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import lombok.ToString;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@ToString
public class ViewerInfo {
    public static final int INT_DEFAULT = -1;
    public static final String STRING_DEFAULT = "";

    private static final int DEFAULT_id = INT_DEFAULT; // actual SQL table id
    private static final int DEFAULT_latestSnapshot = INT_DEFAULT;
    private static final String DEFAULT_userId = STRING_DEFAULT; // similar to koi.api.types.user.User.id
    private static final String DEFAULT_channelId = STRING_DEFAULT;
    private static final String DEFAULT_platform = STRING_DEFAULT;
    private static final String DEFAULT_UPID = STRING_DEFAULT;
    private static final List<UserRoles> DEFAULT_roles = new ArrayList<>();
    private static final List<String> DEFAULT_badges = new ArrayList<>();
    private static final String DEFAULT_color = STRING_DEFAULT;
    private static final String DEFAULT_username = STRING_DEFAULT;
    private static final String DEFAULT_displayname = STRING_DEFAULT;
    private static final String DEFAULT_bio = STRING_DEFAULT;
    private static final String DEFAULT_link = STRING_DEFAULT;
    private static final String DEFAULT_imageLink = STRING_DEFAULT;
    private static final long DEFAULT_followersCount = INT_DEFAULT;
    private static final long DEFAULT_subCount = INT_DEFAULT;
    private static final long DEFAULT_watchtime = INT_DEFAULT;
    private static final long DEFAULT_tskrpoints = INT_DEFAULT;

    public int id; // actual SQL table id
    public int latestSnapshot;
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
        this.id = DEFAULT_id;
        this.latestSnapshot = DEFAULT_latestSnapshot;
        this.userId = DEFAULT_userId;
        this.channelId = DEFAULT_channelId;
        this.platform = DEFAULT_platform;
        this.UPID = DEFAULT_UPID;
        this.roles = DEFAULT_roles;
        this.badges = DEFAULT_badges;
        this.color = DEFAULT_color;
        this.username = DEFAULT_username;
        this.displayname = DEFAULT_displayname;
        this.bio = DEFAULT_bio;
        this.link = DEFAULT_link;
        this.imageLink = DEFAULT_imageLink;
        this.followersCount = DEFAULT_followersCount;
        this.subCount = DEFAULT_subCount;
        this.watchtime = DEFAULT_watchtime;
        this.tskrpoints = DEFAULT_tskrpoints;
    }

    public ViewerInfo(User user) {
        this();
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
        if(imageLink == null) {imageLink = DEFAULT_imageLink;} //workaround for null return
        this.followersCount = user.getFollowersCount();
        this.subCount = user.getSubCount();
    }

    public ViewerInfo id(int id) {
        this.id = id;
        return this;
    }

    public ViewerInfo latestSnapshot(int latestSnapshot) {
        this.latestSnapshot = latestSnapshot;
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
        if(roles.isEmpty()) roles = "[]";
        List<UserRoles> ur = null;
        try {
            ur = Rson.DEFAULT.fromJson(roles, new TypeToken<List<UserRoles>>() {});
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Exception while deserializing UserRoles.");
            e.printStackTrace();
        }
        return roles(ur);
    }

    public ViewerInfo roles(List<UserRoles> roles) {
        if(roles == null) {
            FastLogger.logStatic(LogLevel.WARNING, "VI was passed null roles, silently continuing.");
            return this;
        }
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
        return Rson.DEFAULT.toJson(roles).toString();
    }

    public String getBadges() {
        return badges.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    /**
     * fills any default values of this VI with values from base
     * @param base
     * @return this
     */
    public ViewerInfo fillDefaults(ViewerInfo base) {
        if(this.id == DEFAULT_id) this.id = base.id;
        if(this.latestSnapshot == DEFAULT_latestSnapshot) this.latestSnapshot = base.latestSnapshot;
        if(this.userId.isEmpty()) this.userId = base.userId;
        if(this.channelId.isEmpty()) this.channelId = base.channelId;
        if(this.platform.isEmpty()) this.platform = base.platform;
        if(this.UPID.isEmpty()) this.UPID = base.UPID;
        if(this.roles.isEmpty()) this.roles(base.roles);
        if(this.badges.isEmpty()) this.badges(base.badges);
        if(this.color.isEmpty()) this.color = base.color;
        if(this.username.isEmpty()) this.username = base.username;
        if(this.displayname.isEmpty()) this.displayname = base.displayname;
        if(this.bio.isEmpty()) this.bio = base.bio;
        if(this.link.isEmpty()) this.link = base.link;
        if(this.imageLink.isEmpty()) this.imageLink = base.imageLink;
        if(this.followersCount == DEFAULT_followersCount) this.followersCount = base.followersCount;
        if(this.subCount == DEFAULT_subCount) this.subCount = base.subCount;

        return this;
    }

    /**
     * compares all semi-permanent info, ignoring comparisons where either side has default values
     * @param other
     * @return
     */
    public boolean similar(ViewerInfo other) {
        boolean similar = true;
        if((this.id != other.id && this.id != DEFAULT_id && other.id != DEFAULT_id)
                || (this.latestSnapshot != other.latestSnapshot && this.latestSnapshot != DEFAULT_latestSnapshot && other.latestSnapshot != DEFAULT_latestSnapshot)
                || (!this.userId.equals(other.userId) && !this.userId.equals(DEFAULT_userId) && !other.userId.equals(DEFAULT_userId))
                || (!this.channelId.equals(other.channelId) && !this.channelId.equals(DEFAULT_channelId) && !other.channelId.equals(DEFAULT_channelId))
                || (!this.platform.equals(other.platform) && !this.platform.equals(DEFAULT_platform) && !other.platform.equals(DEFAULT_platform))
                || (!this.UPID.equals(other.UPID) && !this.UPID.equals(DEFAULT_UPID) && !other.UPID.equals(DEFAULT_UPID))
                || (!this.roles.equals(other.roles) && !this.roles.equals(DEFAULT_roles) && !other.roles.equals(DEFAULT_roles))
                || (!this.badges.equals(other.badges) && !this.badges.equals(DEFAULT_badges) && !other.badges.equals(DEFAULT_badges))
                || (!this.color.equals(other.color) && !this.color.equals(DEFAULT_color) && !other.color.equals(DEFAULT_color))
                || (!this.username.equals(other.username) && !this.username.equals(DEFAULT_username) && !other.username.equals(DEFAULT_username))
                || (!this.displayname.equals(other.displayname) && !this.displayname.equals(DEFAULT_displayname) && !other.displayname.equals(DEFAULT_displayname))
                || (!this.bio.equals(other.bio) && !this.bio.equals(DEFAULT_bio) && !other.bio.equals(DEFAULT_bio))
                || (!this.link.equals(other.link) && !this.link.equals(DEFAULT_link) && !other.link.equals(DEFAULT_link))
                || (!this.imageLink.equals(other.imageLink) && !this.imageLink.equals(DEFAULT_imageLink) && !other.imageLink.equals(DEFAULT_imageLink))
                || (this.followersCount != other.followersCount && this.followersCount != DEFAULT_followersCount && other.followersCount != DEFAULT_followersCount)
                || (this.subCount != other.subCount && this.subCount != DEFAULT_subCount && other.subCount != DEFAULT_subCount)) {
            similar = false; 
        }
        return similar;
    }

    public List<Parameter> listUnfilledValues() {
        List<Parameter> params = new ArrayList<>();
        if(id == DEFAULT_id) params.add(new Parameter(DataType.INT, "id", DEFAULT_id));
        if(latestSnapshot == DEFAULT_latestSnapshot) params.add(new Parameter(DataType.INT, "latestSnapshot", DEFAULT_latestSnapshot));
        if(userId.equals(DEFAULT_userId)) params.add(new Parameter(DataType.STRING, "userId", DEFAULT_userId));
        if(channelId.equals(DEFAULT_channelId) || channelId.equals(String.valueOf(INT_DEFAULT))) params.add(new Parameter(DataType.STRING, "channelId", DEFAULT_channelId));
        if(platform.equals(DEFAULT_platform)) params.add(new Parameter(DataType.STRING, "platform", DEFAULT_platform));
        if(UPID.equals(DEFAULT_UPID)) params.add(new Parameter(DataType.STRING, "UPID", DEFAULT_UPID));
        if(roles.equals(DEFAULT_roles)) params.add(new Parameter(DataType.STRING, "roles", DEFAULT_roles));
        if(badges.equals(DEFAULT_badges)) params.add(new Parameter(DataType.STRING, "badges", DEFAULT_badges));
        if(color.equals(DEFAULT_color)) params.add(new Parameter(DataType.STRING, "color", DEFAULT_color));
        if(username.equals(DEFAULT_username)) params.add(new Parameter(DataType.STRING, "username", DEFAULT_username));
        if(displayname.equals(DEFAULT_displayname)) params.add(new Parameter(DataType.STRING, "displayname", DEFAULT_displayname));
        if(bio.equals(DEFAULT_bio)) params.add(new Parameter(DataType.STRING, "bio", DEFAULT_bio));
        if(link.equals(DEFAULT_link)) params.add(new Parameter(DataType.STRING, "link", DEFAULT_link));
        if(imageLink.equals(DEFAULT_imageLink)) params.add(new Parameter(DataType.STRING, "imageLink", DEFAULT_imageLink));
        if(followersCount == DEFAULT_followersCount) params.add(new Parameter(DataType.INT, "followersCount", DEFAULT_followersCount));
        if(subCount == DEFAULT_subCount) params.add(new Parameter(DataType.INT, "subCount", DEFAULT_subCount));
        if(watchtime == DEFAULT_watchtime) params.add(new Parameter(DataType.INT, "watchtime", DEFAULT_watchtime));
        if(tskrpoints == DEFAULT_tskrpoints) params.add(new Parameter(DataType.INT, "tskrpoints", DEFAULT_tskrpoints));

        FastLogger.logStatic(LogLevel.TRACE, "unfilled values for viewer " + id + ": \n" + params);
        return params;
    }

    public void modifyFromParameter(Parameter sp) {
        FastLogger.logStatic(LogLevel.TRACE, "Modifying by parameter " + sp + " for viewer " + id);
        if(sp.type == DataType.INT) {
            long value = Long.parseLong(String.valueOf(sp.value));
            switch(sp.column) {
                case "id" : id((int) value); break;
                case "latestSnapshot" : latestSnapshot((int) value); break;
                case "followersCount" : followersCount((int) value); break;
                case "subCount" : subCount((int) value); break;
                case "watchtime" : watchtime((int) value); break;
                case "tskrpoints" : tskrpoints((int) value); break;
                default:
                    FastLogger.logStatic(LogLevel.SEVERE, "unknown INT column " + sp.column + " while modifying VI: " + this);
            }
        } else if(sp.type == DataType.STRING) {
            String value = (String) sp.value;
            switch(sp.column) {
                case "userId" : userId((String) value); break;
                case "channelId" : channelId((String) value); break;
                case "platform" : platform((String) value); break;
                case "UPID" : upid((String) value); break;
                case "roles" : roles((String) value); break;
                case "badges" : badges((String) value); break;
                case "color" : color((String) value); break;
                case "username" : username((String) value); break;
                case "displayname" : displayname((String) value); break;
                case "bio" : bio((String) value); break;
                case "link" : link((String) value); break;
                case "imageLink" : imageLink((String) value); break;
                default:
                    FastLogger.logStatic(LogLevel.SEVERE, "unknown STRING column " + sp.column + " while modifying VI: " + this);
            }
        }
    }

}
