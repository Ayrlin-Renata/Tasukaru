package com.ayrlin.tasukaru.services;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.ToString;

public interface TasukaruSecurityService {
    
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
     * Returns true if, on the specified platform, the user account of the viewer is modded in Tasukaru settings.
     * @param i an Identity representing the viewer.
     * @param plat the platform of the account to mod.
     * @return the number of recorded milliseconds of watchtime the user has.
     */
    public boolean isMod(Identity i, UserPlatform plat);
    /**
     * Use setIdentity() first to call this method without args. 
     * @return a value based on the default identity set by setIdentity().
     */
    public boolean isMod(UserPlatform plat);

    /**
     * Returns true if, on the specified platform, the user account of the viewer is modded in Tasukaru settings.
     * @param i an Identity representing the viewer.
     * @param plat the platform of the account to mod.
     * @param mod make the account a mod if true.
     * @return the number of recorded milliseconds of watchtime the user has.
     */
    public void setMod(Identity i, UserPlatform plat, boolean mod);
    /**
     * Use setIdentity() first to call this method without args. 
     * @param plat the platform of the account to mod.
     * @param mod make the account a mod if true.
     */
    public void setMod(UserPlatform plat, boolean mod);
}
