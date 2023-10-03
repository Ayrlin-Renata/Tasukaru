package com.ayrlin.tasukaru.vbdefs;

import com.ayrlin.sqlutil.query.data.Col;
import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.Table;
import com.ayrlin.tasukaru.data.info.*;

public class VBTables {

// DONT ADD ANYTHING THAT ISNT TABLES >.>
public static Table meta = new Table("meta")
        .col(new Col("property", DataType.STRING).default_(StringInfo.STRING_DEFAULT).primaryKey())
        .col(new Col("value", DataType.STRING).default_(StringInfo.STRING_DEFAULT));

public static Table viewers =  new Table("viewers")
        .col(new Col("id", DataType.LONG).primaryKey())
        .col(new Col("clid", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("clname", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("fallbackname", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("watchtime", DataType.LONG).default_(LongInfo.LONG_DEFAULT))
        .col(new Col("points", DataType.LONG).default_(LongInfo.LONG_DEFAULT))
        .col(new Col("lurking", DataType.BOOL).default_(BoolInfo.BOOL_DEFAULT));

public static Table accounts =  new Table("accounts")
        .col(new Col("id", DataType.LONG).primaryKey())
        .col(new Col("vid", DataType.STRING).default_(StringInfo.STRING_DEFAULT).references("viewers(id)"))
        .col(new Col("latestsnapshot", DataType.LONG).default_(LongInfo.LONG_DEFAULT).references("snapshots(id)"))
        .col(new Col("userid", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("channelid", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("platform", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("upid", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("roles", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("badges", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("color", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("username", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("displayname", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("bio", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("link", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("imagelink", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("followerscount", DataType.LONG).default_(LongInfo.LONG_DEFAULT))
        .col(new Col("subcount", DataType.LONG).default_(LongInfo.LONG_DEFAULT))
        .col(new Col("mod", DataType.BOOL).default_(BoolInfo.BOOL_DEFAULT));

public static Table snapshots =  new Table("snapshots")
        .col(new Col("id", DataType.LONG).primaryKey())
        .col(new Col("aid", DataType.LONG).default_(LongInfo.LONG_DEFAULT).references("accounts(id)"))
        .col(new Col("vid", DataType.LONG).default_(LongInfo.LONG_DEFAULT).references("viewers(id)"))
        .col(new Col("userid", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("channelid", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("platform", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("upid", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("roles", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("badges", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("color", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("username", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("displayname", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("bio", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("link", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("imagelink", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("followerscount", DataType.LONG).default_(LongInfo.LONG_DEFAULT))
        .col(new Col("subcount", DataType.LONG).default_(LongInfo.LONG_DEFAULT))
        .col(new Col("mod", DataType.BOOL).default_(BoolInfo.BOOL_DEFAULT));

public static Table history =  new Table("history")
        .col(new Col("id", DataType.LONG).primaryKey())
        .col(new Col("aid", DataType.LONG).default_(LongInfo.LONG_DEFAULT).references("accounts(id)"))
        .col(new Col("sid", DataType.LONG).default_(LongInfo.LONG_DEFAULT).references("snapshots(id)"))
        .col(new Col("uptype", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("action", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("value", DataType.DOUBLE).default_(RealInfo.REAL_DEFAULT))
        .col(new Col("origin", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("streamstate", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("timestamp", DataType.LONG).default_(LongInfo.LONG_DEFAULT))
        .col(new Col("event", DataType.STRING).default_(StringInfo.STRING_DEFAULT))
        .col(new Col("processed", DataType.BOOL).default_(BoolInfo.BOOL_DEFAULT));
}
