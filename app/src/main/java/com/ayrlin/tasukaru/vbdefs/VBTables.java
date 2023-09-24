package com.ayrlin.tasukaru.vbdefs;

import com.ayrlin.sqlutil.query.data.Col;
import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.Table;

public class VBTables {

// DONT ADD ANYTHING THAT ISNT TABLES >.>
public static Table meta = new Table("meta")
        .col(new Col("property", DataType.STRING).primaryKey())
        .col(new Col("value", DataType.STRING));

public static Table viewers =  new Table("viewers")
        .col(new Col("id", DataType.INT).primaryKey())
        .col(new Col("clId", DataType.STRING))
        .col(new Col("clName", DataType.STRING))
        .col(new Col("fallbackName", DataType.STRING))
        .col(new Col("watchtime", DataType.INT))
        .col(new Col("points", DataType.INT));

public static Table accounts =  new Table("accounts")
        .col(new Col("id", DataType.INT).primaryKey())
        .col(new Col("vid", DataType.STRING).references("viewers(id)"))
        .col(new Col("latestSnapshot", DataType.INT).references("snapshots(id)"))
        .col(new Col("userId", DataType.STRING))
        .col(new Col("channelId", DataType.STRING))
        .col(new Col("platform", DataType.STRING))
        .col(new Col("UPID", DataType.STRING))
        .col(new Col("roles", DataType.STRING))
        .col(new Col("badges", DataType.STRING))
        .col(new Col("color", DataType.STRING))
        .col(new Col("username", DataType.STRING))
        .col(new Col("displayname", DataType.STRING))
        .col(new Col("bio", DataType.STRING))
        .col(new Col("link", DataType.STRING))
        .col(new Col("imageLink", DataType.STRING))
        .col(new Col("followersCount", DataType.INT))
        .col(new Col("subCount", DataType.INT));       

public static Table snapshots =  new Table("snapshots")
        .col(new Col("id", DataType.INT).primaryKey())
        .col(new Col("aid", DataType.INT).references("accounts(id)"))
        .col(new Col("vid", DataType.INT).references("viewers(id)"))
        .col(new Col("userId", DataType.STRING))
        .col(new Col("channelId", DataType.STRING))
        .col(new Col("platform", DataType.STRING))
        .col(new Col("UPID", DataType.STRING))
        .col(new Col("roles", DataType.STRING))
        .col(new Col("badges", DataType.STRING))
        .col(new Col("color", DataType.STRING))
        .col(new Col("username", DataType.STRING))
        .col(new Col("displayname", DataType.STRING))
        .col(new Col("bio", DataType.STRING))
        .col(new Col("link", DataType.STRING))
        .col(new Col("imageLink", DataType.STRING))
        .col(new Col("followersCount", DataType.INT))
        .col(new Col("subCount", DataType.INT));

public static Table history =  new Table("history")
        .col(new Col("id", DataType.INT).primaryKey())
        .col(new Col("aid", DataType.INT).references("accounts(id)"))
        .col(new Col("sid", DataType.INT).references("snapshots(id)"))
        .col(new Col("uptype", DataType.STRING)) // present, absent, technical
        .col(new Col("action", DataType.STRING))
        .col(new Col("value", DataType.INT))
        .col(new Col("event", DataType.STRING))
        .col(new Col("timestamp", DataType.STRING))
        .col(new Col("streamstate", DataType.STRING))
        .col(new Col("processed", DataType.STRING));
}