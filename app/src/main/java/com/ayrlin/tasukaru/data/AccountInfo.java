package com.ayrlin.tasukaru.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.Param;
import com.ayrlin.tasukaru.Tasukaru;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.koi.api.types.user.User.UserRoles;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import lombok.ToString;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@ToString
public class AccountInfo {
    public static final int INT_DEFAULT = -1;
    public static final String STRING_DEFAULT = "";

    private static final int DEFAULT_id = INT_DEFAULT; // actual SQL table id
    private static final int DEFAULT_vid = INT_DEFAULT; 
    private static final int DEFAULT_latestSnapshot = INT_DEFAULT;
    private static final String DEFAULT_userId = STRING_DEFAULT; // similar to koi.api.types.user.User.id
    private static final String DEFAULT_channelId = STRING_DEFAULT;
    private static final UserPlatform DEFAULT_platform = null;
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

    public int id; // actual SQL table id
    public long vid;
    public int latestSnapshot;
    public String userId; // similar to koi.api.types.user.User.id
    public String channelId;
    public UserPlatform platform;
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

    public AccountInfo() {
        this.id = DEFAULT_id;
        this.vid = DEFAULT_vid;
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
    }

    public AccountInfo(User user) {
        this();
        this.userId = user.getId();
        this.channelId = user.getChannelId();
        this.platform = user.getPlatform();
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

    public AccountInfo(AccountInfo ai) {
        this();
        this.id(ai.id)
            .vid(ai.vid)
            .latestSnapshot(ai.latestSnapshot)
            .userId(ai.userId)
            .channelId(ai.channelId)
            .platform(ai.platform)
            .upid(ai.UPID)
            .roles(ai.roles)
            .badges(ai.badges)
            .color(ai.color)
            .username(ai.username)
            .displayname(ai.displayname)
            .bio(ai.bio)
            .link(ai.link)
            .imageLink(ai.imageLink)
            .followersCount(ai.followersCount)
            .subCount(ai.subCount);
    }

    public AccountInfo id(int id) {
        this.id = id;
        return this;
    }

    public AccountInfo vid(long vid) {
        this.vid = vid;
        return this;
    }

    public AccountInfo latestSnapshot(int latestSnapshot) {
        this.latestSnapshot = latestSnapshot;
        return this;
    } 

    public AccountInfo userId(String userId) {
        this.userId = userId;
        return this;
    }

    public AccountInfo channelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public AccountInfo platform(String platformName) {
        return platform(UserPlatform.valueOf(platformName));
    }
    public AccountInfo platform(UserPlatform platform) {
        this.platform = platform;
        return this;
    }

    public AccountInfo upid(String upid) {
        this.UPID = upid;
        return this;
    }

    // comma delimited pls
    public AccountInfo roles(String roles) {
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

    public AccountInfo roles(List<UserRoles> roles) {
        if(roles == null) {
            FastLogger.logStatic(LogLevel.WARNING, "VI was passed null roles, silently continuing.");
            return this;
        }
        this.roles = roles;
        return this;
    }

    // comma delimited pls
    public AccountInfo badges(String badges) {
        return badges(Arrays.asList(badges.split(",", -1)));
    }

    public AccountInfo badges(List<String> badges) {
        this.badges = badges;
        return this;
    }

    public AccountInfo color(String color) {
        this.color = color;
        return this;
    }

    public AccountInfo username(String username) {
        this.username = username;
        return this;
    }

    public AccountInfo displayname(String displayname) {
        this.displayname = displayname;
        return this;
    }

    public AccountInfo bio(String bio) {
        this.bio = bio;
        return this;
    }

    public AccountInfo link(String link) {
        this.link = link;
        return this;
    }

    public AccountInfo imageLink(String imageLink) {
        this.imageLink = imageLink;
        return this;
    }

    public AccountInfo followersCount(long followersCount) {
        this.followersCount = followersCount;
        return this;
    }

    public AccountInfo subCount(long subCount) {
        this.subCount = subCount;
        return this;
    }

    public String getPlatform() {
        return platform.name();
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
    public AccountInfo fillDefaults(AccountInfo base) {
        if(this.id == DEFAULT_id) this.id = base.id;
        if(this.vid == DEFAULT_vid) this.vid = base.vid;
        if(this.latestSnapshot == DEFAULT_latestSnapshot) this.latestSnapshot = base.latestSnapshot;
        if(this.userId.isEmpty()) this.userId = base.userId;
        if(this.channelId.isEmpty()) this.channelId = base.channelId;
        if(this.platform == null) this.platform = base.platform;
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
    public boolean notContradictory(AccountInfo other) {
        boolean similar = true;
        if((this.id != other.id && this.id != DEFAULT_id && other.id != DEFAULT_id)
                || (this.vid != other.vid && this.vid != DEFAULT_vid && other.vid != DEFAULT_vid)
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

    @Override
    public boolean equals(Object o) {
        AccountInfo other;
        if(o instanceof AccountInfo) {
            other = (AccountInfo) o;
        } else {
            Tasukaru.instance().getLogger().warn("AccountInfo compared to non-AccountInfo Object!\nAccountInfo:\n" + this + "\nObject:\n" + o.toString());
            return false;
        }
        if((this.id != other.id)
                || (this.vid != other.vid)
                || (this.latestSnapshot != other.latestSnapshot)
                || (!this.userId.equals(other.userId))
                || (!this.channelId.equals(other.channelId))
                || (!this.platform.equals(other.platform))
                || (!this.UPID.equals(other.UPID))
                || (!this.roles.equals(other.roles))
                || (!this.badges.equals(other.badges))
                || (!this.color.equals(other.color))
                || (!this.username.equals(other.username))
                || (!this.displayname.equals(other.displayname))
                || (!this.bio.equals(other.bio))
                || (!this.link.equals(other.link))
                || (!this.imageLink.equals(other.imageLink))
                || (this.followersCount != other.followersCount)
                || (this.subCount != other.subCount)) {
            return false;
        }
        return true;
    }

    public List<Param> listUnfilledValues() {
        List<Param> params = new ArrayList<>();
        if(id == DEFAULT_id) params.add(new Param(DataType.INT, "id", DEFAULT_id));
        if(vid == DEFAULT_vid) params.add(new Param(DataType.INT, "vid", DEFAULT_vid));
        if(latestSnapshot == DEFAULT_latestSnapshot) params.add(new Param(DataType.INT, "latestSnapshot", DEFAULT_latestSnapshot));
        if(userId.equals(DEFAULT_userId)) params.add(new Param(DataType.STRING, "userId", DEFAULT_userId));
        if(channelId.equals(DEFAULT_channelId) || channelId.equals(String.valueOf(INT_DEFAULT))) params.add(new Param(DataType.STRING, "channelId", DEFAULT_channelId));
        if(platform.equals(DEFAULT_platform)) params.add(new Param(DataType.STRING, "platform", DEFAULT_platform));
        if(UPID.equals(DEFAULT_UPID)) params.add(new Param(DataType.STRING, "UPID", DEFAULT_UPID));
        if(roles.equals(DEFAULT_roles)) params.add(new Param(DataType.STRING, "roles", DEFAULT_roles));
        if(badges.equals(DEFAULT_badges)) params.add(new Param(DataType.STRING, "badges", DEFAULT_badges));
        if(color.equals(DEFAULT_color)) params.add(new Param(DataType.STRING, "color", DEFAULT_color));
        if(username.equals(DEFAULT_username)) params.add(new Param(DataType.STRING, "username", DEFAULT_username));
        if(displayname.equals(DEFAULT_displayname)) params.add(new Param(DataType.STRING, "displayname", DEFAULT_displayname));
        if(bio.equals(DEFAULT_bio)) params.add(new Param(DataType.STRING, "bio", DEFAULT_bio));
        if(link.equals(DEFAULT_link)) params.add(new Param(DataType.STRING, "link", DEFAULT_link));
        if(imageLink.equals(DEFAULT_imageLink)) params.add(new Param(DataType.STRING, "imageLink", DEFAULT_imageLink));
        if(followersCount == DEFAULT_followersCount) params.add(new Param(DataType.INT, "followersCount", DEFAULT_followersCount));
        if(subCount == DEFAULT_subCount) params.add(new Param(DataType.INT, "subCount", DEFAULT_subCount));

        FastLogger.logStatic(LogLevel.TRACE, "unfilled values for viewer " + id + ": \n" + params);
        return params;
    }

    public void modifyFromParameter(Param sp) {
        FastLogger.logStatic(LogLevel.TRACE, "Modifying by parameter " + sp + " for viewer " + id);
        if(sp.type == DataType.INT) {
            long value = Long.parseLong(String.valueOf(sp.value));
            switch(sp.column) {
                case "id" : id((int) value); break;
                case "vid" : vid((long) value); break;
                case "latestSnapshot" : latestSnapshot((int) value); break;
                case "followersCount" : followersCount((long) value); break;
                case "subCount" : subCount((long) value); break;
                default:
                    FastLogger.logStatic(LogLevel.SEVERE, "unknown INT column " + sp.column + " while modifying VI: " + this);
            }
        } else if(sp.type == DataType.STRING) {
            String value = (String) sp.value;
            switch(sp.column) {
                case "userId" : userId((String) value); break;
                case "channelId" : channelId((String) value); break;
                case "platform" : platform(UserPlatform.valueOf(value)); break;
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