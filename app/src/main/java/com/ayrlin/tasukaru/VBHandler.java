package com.ayrlin.tasukaru;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ayrlin.sqlutil.ActiveResult;
import com.ayrlin.sqlutil.SQLUtil;
import com.ayrlin.sqlutil.query.AlterTableQuery;
import com.ayrlin.sqlutil.query.SelectQuery;
import com.ayrlin.sqlutil.query.data.DataType;
import com.ayrlin.sqlutil.query.data.OpParam;
import com.ayrlin.sqlutil.query.data.Param;
import com.ayrlin.sqlutil.query.data.OpParam.Op;
import com.ayrlin.sqlutil.query.data.Col;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class VBHandler {
    public static final String vbVersion = "1.0.0";

    private static Connection con;
    private static boolean initd = false;
    
    private FastLogger log;
    private String tDir;

    public VBHandler(FastLogger fl, String td) {
        log = fl;
        tDir = td;
    }

    public void run() {
        try {
            getConnection();
            initialise();
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e,"exception during run init");
        }
    }

    private void getConnection() {
        // sqlite driver
        try {
            Class.forName("org.sqlite.JDBC");
            String conPath = "jdbc:sqlite://" + tDir + "ViewerBase.db";
            log.debug("Attempting to connect to DB at: " + conPath);
            con = DriverManager.getConnection(conPath);
            con.setAutoCommit(true);
        } catch (ClassNotFoundException e) {
            log.severe("couldnt find JDBC");
            e.printStackTrace();
        } catch (SQLException e) {
            log.warn("couldnt connect to the viewerbase DB");
            SQLUtil.SQLExHandle(e,"exception while connecting to DB");
        }
    }

    private void initialise() throws SQLException {
        if (!initd) {
            initd = true;

            String checkSQLBegin = "SELECT name FROM sqlite_master WHERE type='table' AND name='";
            String checkSQLEnd = "'";

            // META
            if (!con.createStatement()
                    .executeQuery(checkSQLBegin + "meta" + checkSQLEnd)
                    .next()) {
                log.warn("No Meta table found, creating new Meta table.");
                createMetaTable();
            }
            String checkVersion = "SELECT value FROM `meta` WHERE property = \"version\";";
            ResultSet versionCheckResult = con.createStatement().executeQuery(checkVersion);
            if(!versionCheckResult.next()) {
                log.severe("malformed meta table has no version");
            } else {
                String readVersion = versionCheckResult.getString("value"); 
                if(!readVersion.equals(vbVersion)) {
                    log.warn("Mismatched versions! Database reports as version " + readVersion + ", plugin version is " + vbVersion + "!");
                    //TODO update DB to current version, including backup
                }
            }

            // ACCOUNT SNAPSHOT
            if (!con.createStatement()
                    .executeQuery(checkSQLBegin + "snapshots" + checkSQLEnd)
                    .next()) {
                log.warn("No Account Snapshot table found, creating new Account Snapshot table.");
                createSnapshotTable();
            }

            // ACCOUNTS
            if (!con.createStatement()
                    .executeQuery(checkSQLBegin + "accounts" + checkSQLEnd)
                    .next()) {
                log.warn("No Accounts table found, creating new Accounts table.");
                createAccountTable();
            }

            // VIEWERS
            if (!con.createStatement()
                    .executeQuery(checkSQLBegin + "viewers" + checkSQLEnd)
                    .next()) {
                log.warn("No Viewers table found, creating new Viewers table.");
                createViewersTable();
            }

            // HISTORY
            if (!con.createStatement()
                    .executeQuery(checkSQLBegin + "history" + checkSQLEnd)
                    .next()) {
                log.warn("No History table found, creating new History table.");
                createHistoryTable();
            }
        }
    }

    private void createMetaTable() throws SQLException {
        // meta table definition
        con.createStatement().executeUpdate("CREATE TABLE `meta` ("
                + "property TEXT PRIMARY KEY,"
                + "value TEXT"
                + ");");
        con.createStatement().executeUpdate("insert into meta values(\"version\",\"" + vbVersion + "\");");
    }

    private void createSnapshotTable() throws SQLException {
        // snapshots table definition
        con.createStatement().executeUpdate("CREATE TABLE `snapshots` ("
                + "id INTEGER PRIMARY KEY,"
                + "aid INTEGER NOT NULL,"
                + "userId TEXT," // koi.api.types.user.User.id
                + "channelId TEXT,"
                + "platform TEXT," // koi.api.types.user.User.platform.name
                + "UPID TEXT,"
                + "roles TEXT," // list UserRoles
                + "badges TEXT," // list String
                + "color TEXT,"
                + "username TEXT,"
                + "displayname TEXT,"
                + "bio TEXT,"
                + "link TEXT,"
                + "imageLink TEXT,"
                + "followersCount INTEGER,"
                + "subCount INTEGER,"
                + "FOREIGN KEY (aid) REFERENCES `accounts` (id)"
                + ");");
    }

    private void createAccountTable() throws SQLException {
        // accounts table definition
        con.createStatement().executeUpdate("CREATE TABLE `accounts` (" 
                + "id INTEGER PRIMARY KEY,"
                + "latestSnapshot INTEGER,"
                + "userId TEXT," // koi.api.types.user.User.id
                + "channelId TEXT,"
                + "platform TEXT," // koi.api.types.user.User.platform.name
                + "UPID TEXT,"
                + "roles TEXT," // list UserRoles
                + "badges TEXT," // list String
                + "color TEXT,"
                + "username TEXT,"
                + "displayname TEXT,"
                + "bio TEXT,"
                + "link TEXT,"
                + "imageLink TEXT,"
                + "followersCount INTEGER,"
                + "subCount INTEGER,"
                + "FOREIGN KEY (latestSnapshot) REFERENCES `snapshots` (id)"
                + ");");
    }

    private void createViewersTable() throws SQLException {
        // accounts table definition
        con.createStatement().executeUpdate("CREATE TABLE `viewers` (" 
                + "id INTEGER PRIMARY KEY,"
                + "watchtime INTEGER,"
                + "points INTEGER"
                + ");");
    }

    private void createHistoryTable() throws SQLException {
        // history table definition
        con.createStatement().executeUpdate("CREATE TABLE `history` ("
                + "id INTEGER PRIMARY KEY,"
                + "aid INTEGER NOT NULL," // foreign key accounts
                + "sid INTEGER NOT NULL," // foreign key snapshots
                + "uptype TEXT NOT NULL," // present, absent, technical
                + "action TEXT,"
                + "value INTEGER,"
                + "timestamp TEXT,"
                + "streamstate TEXT,"
                + "FOREIGN KEY (aid) REFERENCES `accounts` (id)"
                + "FOREIGN KEY (sid) REFERENCES `snapshots` (id)"
                + ");");
    }

    ///////////////// VB ACTIONS //////////////////

    public int addAccount(AccountInfo vi) {
        if (con == null) {
            getConnection();
        }
        log.debug("Adding Account: \n" + vi);

        List<Param> params = new ArrayList<>();
        params.add(new Param(DataType.INT, "latestSnapshot", -1)); //default
        params.add(new Param(DataType.STRING, "userId", vi.userId));
        params.add(new Param(DataType.STRING, "channelId", vi.channelId));
        params.add(new Param(DataType.STRING, "platform", vi.platform));
        params.add(new Param(DataType.STRING, "UPID", vi.UPID));

        params.add(new Param(DataType.STRING, "roles", vi.getRoles()));
        params.add(new Param(DataType.STRING, "badges", vi.getBadges()));
        params.add(new Param(DataType.STRING, "color", vi.color));
        params.add(new Param(DataType.STRING, "username", vi.username));
        params.add(new Param(DataType.STRING, "displayname", vi.displayname));
        params.add(new Param(DataType.STRING, "bio", vi.bio));
        params.add(new Param(DataType.STRING, "link", vi.link));
        params.add(new Param(DataType.STRING, "imageLink", vi.imageLink));
        params.add(new Param(DataType.INT, "followersCount", vi.followersCount));
        params.add(new Param(DataType.INT, "subCount", vi.subCount));

        long key = SQLUtil.insert(con, "accounts", params);
        if(key < 0) {
            log.severe("failed to add Account: \n" + vi);
            return -1;
        } 

        //add snapshot after Account for foreign key
        vi.id((int)key);
        log.trace("added Account id: " + vi.id);

        if(!updateAccount(vi)) { //will also handle snapshot
            log.severe("failed to update Account latestSnapshot while adding Account: \n" + vi);
            return -2;
        }

        return vi.id;
    }

    public boolean updateAccount(AccountInfo vi) {
        if (con == null) {
            getConnection();
        }
        log.debug("Updating Account info: " + vi);
        
        addSnapshot(vi);
        log.trace("Inserting new latestSnapshot into update: " + vi.latestSnapshot);
        
        List<Param> setParams = new ArrayList<>();
        setParams.add(new Param(DataType.INT, "latestSnapshot", vi.latestSnapshot));
        setParams.add(new Param(DataType.STRING, "userId", vi.userId));
        setParams.add(new Param(DataType.STRING, "channelId", vi.channelId));
        setParams.add(new Param(DataType.STRING, "platform", vi.platform));
        setParams.add(new Param(DataType.STRING, "UPID", vi.UPID));

        setParams.add(new Param(DataType.STRING, "roles", vi.getRoles()));
        setParams.add(new Param(DataType.STRING, "badges", vi.getBadges()));
        setParams.add(new Param(DataType.STRING, "color", vi.color));
        setParams.add(new Param(DataType.STRING, "username", vi.username));
        setParams.add(new Param(DataType.STRING, "displayname", vi.displayname));
        setParams.add(new Param(DataType.STRING, "bio", vi.bio));
        setParams.add(new Param(DataType.STRING, "link", vi.link));
        setParams.add(new Param(DataType.STRING, "imageLink", vi.imageLink));
        setParams.add(new Param(DataType.INT, "followersCount", vi.followersCount));
        setParams.add(new Param(DataType.INT, "subCount", vi.subCount));
        
        List<OpParam> whereParams = new ArrayList<>();
        whereParams.add(new OpParam(DataType.INT, "id", Op.EQUAL, vi.id));
        
        if(!SQLUtil.update(con, "accounts", setParams, whereParams)) {
            log.severe("failed to update Account: \n" + vi);
            return false;
        } 
        return true;
    }

    /**
     * Creates a latestSnapshot and adds it to vi
     * @param vi
     * @return the snapshot id, equal to vi.latestSnapshot
     */
    public int addSnapshot(AccountInfo vi) {
        if (con == null) {
            getConnection();
        }
        log.debug("Adding account snapshot for Account: \n" + vi);

        if (vi.id <= 0) {
            int aid = findAccountId(vi);
            if (aid < 0) {
                log.severe("Failed to add account snapshot due to lack of id for Account: \n" + vi);
                return -1;
            } else {
                vi.id = aid;
            }
        }

        List<Param> params = new ArrayList<>();
        params.add(new Param(DataType.INT, "aid", vi.id));
        params.add(new Param(DataType.STRING, "userId", vi.userId));
        params.add(new Param(DataType.STRING, "channelId", vi.channelId));
        params.add(new Param(DataType.STRING, "platform", vi.platform));
        params.add(new Param(DataType.STRING, "UPID", vi.UPID));

        params.add(new Param(DataType.STRING, "roles", vi.getRoles()));
        params.add(new Param(DataType.STRING, "badges", vi.getBadges()));
        params.add(new Param(DataType.STRING, "color", vi.color));
        params.add(new Param(DataType.STRING, "username", vi.username));
        params.add(new Param(DataType.STRING, "displayname", vi.displayname));
        params.add(new Param(DataType.STRING, "bio", vi.bio));
        params.add(new Param(DataType.STRING, "link", vi.link));
        params.add(new Param(DataType.STRING, "imageLink", vi.imageLink));
        params.add(new Param(DataType.INT, "followersCount", vi.followersCount));
        params.add(new Param(DataType.INT, "subCount", vi.subCount));

        long key = SQLUtil.insert(con, "snapshots", params);
        if(key < 0) {
            log.severe("Failed to add account snapshot for Account: \n" + vi);
            return -1;
        }

        vi.latestSnapshot = (int)key;

        EventInfo ei = new EventInfo()
                .account(vi)
                .snapshotId(vi.latestSnapshot)
                .uptype("technical")
                .action("snapshot");
        addHistory(ei);

        return vi.latestSnapshot;
    }

    public int addHistory(EventInfo ei) {
        if (con == null) {
            getConnection();
        }
        if (ei.timestamp == null) {
            ei.timestamp = new java.sql.Timestamp(new java.util.Date().getTime());
        }
        log.debug("Adding history for event: \n" + ei);

        List<Param> params = new ArrayList<>();
        params.add(new Param(DataType.INT, "aid", ei.account.id));
        params.add(new Param(DataType.INT, "sid", ei.snapshotId));
        params.add(new Param(DataType.STRING, "uptype", ei.uptype));
        params.add(new Param(DataType.STRING, "action", ei.action));
        params.add(new Param(DataType.INT, "value", ei.value));
        params.add(new Param(DataType.STRING, "streamState", ei.streamState));
        params.add(new Param(DataType.TIMESTAMP, "timestamp", ei.timestamp));

        long key = SQLUtil.insert(con, "history", params);
        if(key < 0) {
            log.severe("failed to addHistory() for event: \n" + ei);
        }
        return (int)key;
    }

    /**
     * @return AccountId if Account exists,
     *         -1 if successfully found no Account,
     *         -2 if errored
     */
    public int findAccountId(AccountInfo vi) {
        if (con == null) {
            getConnection();
        }
        log.trace("searching for id of Account: \n" + vi);

        List<OpParam> whereList = new ArrayList<>();

        // determine most reliable info
        if (!vi.UPID.isEmpty()) {
            whereList.add(new OpParam(DataType.STRING, "UPID", Op.EQUAL, vi.UPID));
        } else if (!vi.platform.isEmpty()) {
            whereList.add(new OpParam(DataType.STRING, "platform", Op.EQUAL, vi.platform));
            if (!vi.userId.isEmpty()) {
                whereList.add(new OpParam(DataType.STRING, "userId", Op.EQUAL, vi.userId));
            } else if (!vi.username.isEmpty()) {
                whereList.add(new OpParam(DataType.STRING, "username", Op.EQUAL, vi.username));
            } else if (!vi.link.isEmpty()) {
                whereList.add(new OpParam(DataType.STRING, "link", Op.EQUAL, vi.link));
            } else if (!vi.displayname.isEmpty()) {
                whereList.add(new OpParam(DataType.STRING, "displayname", Op.EQUAL, vi.displayname));
            } else {
                //abort
                log.warn("abort finding Account: \n" + vi);
                return -2;
            }
        } else {
            log.warn("abort finding Account: \n" + vi);
            return -2;
        }

        int accountId;
        ActiveResult ar = new SelectQuery().select("id").from("accounts").where(whereList).execute(con);
        try {
            if (!ar.rs.next() || ar.rs.getInt("id") <= 0 ) {
                log.warn("unable to find Account: \n" + vi);
                return -1;
            }
            accountId = ar.rs.getInt("id");
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e,"SQLException while finding Account: " + vi);
            return -2;
        } finally { ar.close(); }

        vi.id(accountId);
        log.debug("found Account id " + accountId + " for Account: \n" + vi);
        return accountId;
    }

    public AccountInfo retrieveCurrentAccountInfo(long id) {
        if (con == null) {
            getConnection();
        }
        log.trace("retrieving current Account info.");

        ActiveResult ar = new SelectQuery()
                .select("*")
                .from("accounts")
                .where(SQLUtil.qol(DataType.INT, "id", Op.EQUAL, id))
                .execute(con);
        ResultSet rs = ar.rs;
        AccountInfo ci = new AccountInfo();
        try {
            if (!rs.next()) {
                log.warn("unable to find Account with id: \n" + id);
                return null;
            }

            ci.id((int)rs.getLong("id"))
                    .latestSnapshot((int)rs.getLong("latestSnapshot"))
                    .userId(rs.getString("userId"))
                    .channelId(rs.getString("channelId"))
                    .platform(rs.getString("platform"))
                    .upid(rs.getString("upid"))
                    .roles(rs.getString("roles"))
                    .badges(rs.getString("badges"))
                    .color(rs.getString("color"))
                    .username(rs.getString("username"))
                    .displayname(rs.getString("displayname"))
                    .bio(rs.getString("bio"))
                    .link(rs.getString("link"))
                    .imageLink(rs.getString("imageLink"))
                    .followersCount((int)rs.getLong("followersCount"))
                    .subCount((int)rs.getLong("subCount"));
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e,"SQLException while retrieving Account info with id: \n" + id);
        } finally { ar.close(); }
        log.debug("retrieved current Account info: \n" + ci);
        return ci;
    }

    public boolean verifyCurrentAccountInfo(AccountInfo vi) {
        log.trace("verifying current Account info for Account: \n" + vi);
        AccountInfo ci = retrieveCurrentAccountInfo(vi.id);
        boolean result = vi.similar(ci);
        log.debug("Account info is " + (result? "" : "not") + " current for Account: \n" + vi + " with current info: \n" + ci);
        return result;
    }

    public List<Long> listAllAccounts() {
        List<Long> ids = new ArrayList<>();
        if (con == null) {
            getConnection();
        }
        log.trace("listing all Accounts");

        ActiveResult ar = new SelectQuery().select("id").from("accounts").execute(con);
        try {
            while(ar.rs.next()) {
                ids.add(ar.rs.getLong("id"));
            }
        } catch(SQLException e) {
            SQLUtil.SQLExHandle(e,"SQLException while selecting all Account ids");
        } finally { ar.close(); }

        log.debug("all Accounts list: " + ids);
        return ids;
    }

    public int findLatestSnapshot(int aid) {
        if (con == null) {
            getConnection();
        }
        log.trace("searching for id of latest snapshot for Account with id: " + aid);

        int sid = -1;
        ActiveResult ar = new SelectQuery()
                .select("latestSnapshot")
                .from("accounts")
                .where(SQLUtil.qol(DataType.INT, "id", Op.EQUAL, aid))
                .execute(con);
        try {
            if (!ar.rs.next()) {
                log.warn("unable to find Account latestSnapshot for Account with id: " + aid);
                return -1;
            }
            sid = ar.rs.getInt("latestSnapshot");
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e,"SQLException while searching for Account latest snapshot: " + aid);
        } finally { ar.close(); }

        log.debug("id of latest snapshot is "+ sid + " for Account with id " + aid);
        return sid;
    }

    public Param findLastSnapshotValue(int aid, Param toFind) {
        if (con == null) {
            getConnection();
        }
        log.trace("finding last value of " + toFind + " for Account " + aid + " in snapshot records.");

        Object value = null;
        if(toFind.type == DataType.INT) value = AccountInfo.INT_DEFAULT;
        if(toFind.type == DataType.STRING) value = AccountInfo.STRING_DEFAULT;

        ActiveResult ar = new SelectQuery()
                .select(SQLUtil.qpl(toFind))
                .from("snapshots")
                .where(SQLUtil.qol(DataType.INT,"aid", Op.EQUAL, aid))
                .orderBy("id").desc()
                .execute(con);
        try {
            while(ar.rs.next()) {
                if(toFind.type == DataType.INT) {
                    long result = ar.rs.getLong(toFind.column);
                    if(result == AccountInfo.INT_DEFAULT) {
                        continue;
                    } else {
                        value = result;
                        break;
                    }
                } else if(toFind.type == DataType.STRING) {
                    String result = ar.rs.getString(toFind.column);
                    //log.debug("lastValue contender: ", result);
                    if(result == null || result.equals(AccountInfo.STRING_DEFAULT)) {
                        continue;
                    } else {
                        if(toFind.column == "roles") { //special case due to serialization, if this becomes regular need to add DataType.SERIALIZED
                            if(result.equals("[]")) continue;
                        } else if(toFind.column == "channelId") { //special case due to for some reason API returning int values
                            if(result.equals(String.valueOf(AccountInfo.INT_DEFAULT))) continue;
                        }
                        value = result;
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            SQLUtil.SQLExHandle(e, "SQLException while finding last value of " + toFind + " for Account " + aid + " in snapshot records.");
        } finally { ar.close(); }

        Param result = new Param(toFind.type, toFind.column, value);
        log.debug("last value of " + toFind + " for Account " + aid + " in snapshot records: \n" + result);
        return result;
    }

    public void checkAddCols(FastLogger log, List<String> cols) {
        if(cols.isEmpty()) {
            log.severe("no supported platforms specified while updating viewer columns!");
            return;
        }
        List<String> vtcols = SQLUtil.retrieveColumnNames(con,"viewers").stream()
                .map(col -> col.column)
                .collect(Collectors.toList());
        if(vtcols.containsAll(cols)) {
            log.debug("all platforms already available.");
            return;
        } else {
            for (String cname : cols) {
                if(vtcols.contains(cname)) continue;
                boolean executed = new AlterTableQuery()
                        .alterTable("viewers")
                        .addColumn(new Col(cname, DataType.INT).references("accounts(id)"))
                        .execute(con);
                if(!executed) { 
                    log.warn("while adding platform column " + cname + ", AlterTableQuery claims to have not altered the table. such claims may be greatly exaggerated."); 
                }
            }
        }
    }
}