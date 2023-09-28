package com.ayrlin.tasukaru.data.info;

import java.util.ArrayList;
import java.util.List;

import co.casterlabs.koi.api.types.user.User.UserRoles;
import co.casterlabs.rakurai.json.TypeToken;

public class RolesListInfo extends ListInfo<UserRoles> {
    public RolesListInfo() {
        this("");
    }
    public RolesListInfo(String name) {
        super(new ArrayList<UserRoles>(), name);
        this.type = new TypeToken<Info<List<UserRoles>>>() {};
        this.datatype = new TypeToken<List<UserRoles>>() {};
    }
}