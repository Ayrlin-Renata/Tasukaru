package com.ayrlin.tasukaru.services;

import java.util.List;

import co.casterlabs.commons.functional.tuples.Triple;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.ToString;

public interface TasukaruInterop {

    /**
     * Represents a unique user account. A helper to make various forms of valid identification easier to use across all interop functions. 
     */
    @ToString
    @JsonClass(exposeAll = true)
    public class Identity {
        public String clID; //Satisfactory given alone. A casterlabs identifier, not yet implemented, DO NOT USE. this is NOT upid
        public User user; //Satisfactory given alone. A koi User, such as from KoiEvent.getStreamer() or RichMessageEvent.getSender()
        public String username; //Pair with platform. username or display name of an account.
        public UserPlatform platform; //Pair with username. an enum representing the streaming platform of the user account.
    }

    /**
     * Preemptively sets the primary identity to avoid extra computation and make it easier when calling multiple other service methods. 
     * @param i the identity
     */
    public void setIdentity(Identity i);

    /**
     * Retrieves the current number of points of the user.
     * @param i an Identity representing the viewer to check points of.
     * @return the number of points the user has.
     */
    public long checkPoints(Identity i);
    /**
     * Use setIdentity() first to call this method without args. 
     * @return a value based on the default identity set by setIdentity().
     */
    public long checkPoints();
    

    /**
     * Retrieves the current sum of watchtime of the user across all platforms.
     * @param i an Identity representing the viewer to check watchtime of.
     * @return the number of recorded milliseconds of watchtime the user has.
     */
    public long checkWatchtime(Identity i); 
    /**
     * Use setIdentity() first to call this method without args. 
     * @return a value based on the default identity set by setIdentity().
     */
    public long checkWatchtime();
    

    /**
     * Adds an amount of points to the current amount of points a user has.
     * @param i an Identity representing the viewer
     * @param amount the amount of points to add, use negatives to subtract
     */
    public void addPoints(Identity i, long amount);
    /**
     * Use setIdentity() first to call this method without identity args. 
     * @param amount the amount of points to add, use negatives to subtract
     * @return a value based on the default identity set by setIdentity().
     */
    public void addPoints(long amount);
    

    /**
     * Sets the amount of points for the user, ignoring previously earned points.
     * BE CAREFUL! You will have to look through the database to find this if you mess up.
     * @param i an Identity representing the viewer
     * @param amount the total amount of points for the user to have. 
     */
    public void setPoints(Identity i, long amount);
    /**
     * Use setIdentity() first to call this method without identity args. 
     * @param amount the total amount of points for the user to have. 
     * @return a value based on the default identity set by setIdentity().
     */
    public void setPoints(long amount);
    

    /**
     * Sets the lurking state of the user, activating lurk points bonuses/pricing
     * @param i an Identity representing the viewer
     * @param lurking true if the user is going AFK
     */
    public void setLurk(Identity i, boolean lurking);
    /**
     * Use setIdentity() first to call this method without identity args. 
     * @param lurking true if the user is going AFK
     * @return a value based on the default identity set by setIdentity().
     */
    public void setLurk(boolean lurking);

    /**
     * Retrieves an ordered list of viewers, according to how many points they have.  
     * @param count limits how many viewers to retrieve to the value of count
     * @param fromTop sets the order of the list. if true, the highest point viewer will have the smallest index in the list.
     * @return the list of triples representing the leaderboard.
     */
    public List<Triple<Long, String, Long>> getPointsLeaderboard(int count, boolean fromTop); 

    /**
     * Retrieves an ordered list of viewers, according to how much watchtime they have.  
     * @param count limits how many viewers to retrieve to the value of count
     * @param fromTop sets the order of the list. if true, the highest watchtime viewer will have the smallest index in the list.
     * @return the list of triples representing the leaderboard.
     */
    public List<Triple<Long, String, Long>> getWatchtimeLeaderboard(int count, boolean fromTop); 

    /**
     * Links a viewer's accounts across platforms to share points regardless of where they watch. 
     * Link requests must be verified in two directions, so both accounts must request.
     * Links cannot be made on the same platform.
     * Deprecated for future implementation, DONOTUSE!
     * @param sender the viewer making the link request
     * @param reciever the account being requested to be linked to
     * @return true if the request was sent successfully
     */
    @Deprecated
    public boolean accountLinkRequest(Identity sender, Identity reciever);

    /**
     * Unlinks a viewer's account on a specific platform.
     * Deprecated for future implementation, DONOTUSE!
     * @param sender the viewer making the link request
     * @param platform the platform of the account to unlink from the viewer.
     * @return true if the request was sent successfully
     */
    @Deprecated
    public boolean accountUnlinkRequest(Identity sender, String platform);

}
