package me.SuperRonanCraft.BetterRTP.references.database;

import lombok.NonNull;
import me.SuperRonanCraft.BetterRTP.BetterRTP;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public abstract class SQLite {

    private static final String db_file_name = "database";
    List<String> tables;
    private volatile boolean loaded;

    private final DATABASE_TYPE type;

    public SQLite(DATABASE_TYPE type) {
        this.type = type;
    }

    public abstract List<String> getTables();

    // SQL creation stuff
    public Connection getSQLConnection() {
        return getLocal();
    }

    private Connection getLocal() {
        File dataFolder = databaseFile();
        if (!dataFolder.exists()){
            try {
                dataFolder.getParentFile().mkdir();
                dataFolder.createNewFile();
            } catch (IOException exception) {
                BetterRTP.getInstance().getLogger().log(
                        Level.SEVERE, "File write error: " + dataFolder.getPath(), exception);
                return null;
            }
        }
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
        } catch (SQLException ex) {
            BetterRTP.getInstance().getLogger().log(Level.SEVERE, "SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            BetterRTP.getInstance().getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it Ronan...");
        }
        return null;
    }

    public void load() {
        loaded = false;
        tables = getTables();

        // Don't do anything is no columns to generate
        if (tables.isEmpty()) {
            loaded = true;
            return;
        }

        SQLiteExecutor.executor().submit(() -> {
            Connection connection = getSQLConnection();
            if (connection == null) {
                BetterRTP.getInstance().getLogger().severe("Unable to open the BetterRTP database");
                return;
            }
            try {
                DatabaseSchemaMigrator.migrate(
                        connection, databaseFile().toPath(), BetterRTP.getInstance().getLogger());
                try (Statement statement = connection.createStatement()) {
                    for (String table : tables) {
                        statement.executeUpdate(getCreateTable(table));
                        Set<String> existingColumns = getExistingColumns(connection, table);
                        for (Enum<?> column : getColumns(type)) {
                            String columnName = getColumnName(type, column);
                            if (!existingColumns.contains(columnName.toLowerCase())) {
                                statement.executeUpdate("ALTER TABLE `" + table + "` ADD COLUMN `"
                                        + columnName + "` " + getColumnType(type, column));
                            }
                        }
                        BetterRTP.debug("Database " + type.name() + ":" + table + " configured and loaded!");
                    }
                }
                loaded = true;
            } catch (SQLException | IOException exception) {
                BetterRTP.getInstance().getLogger().log(
                        Level.SEVERE, "Unable to migrate or initialize database " + type, exception);
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException exception) {
                        BetterRTP.getInstance().getLogger().log(
                                Level.WARNING, "Unable to close the BetterRTP database", exception);
                    }
                }
            }
        });
    }

    private File databaseFile() {
        return new File(BetterRTP.getInstance().getDataFolder().getPath()
                + File.separator + "data", db_file_name + ".db");
    }

    private Set<String> getExistingColumns(Connection connection, String table) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA table_info(`" + table + "`)")) {
            while (result.next()) {
                columns.add(result.getString("name").toLowerCase());
            }
        }
        return columns;
    }

    private String getCreateTable(String table) {
        String str = "CREATE TABLE IF NOT EXISTS `" + table + "` (";
        Enum<?>[] columns = getColumns(type);
        for (Enum<?> c : columns) {
            String _name = getColumnName(type, c);
            String _type = getColumnType(type, c);
            str = str.concat("`" + _name + "` " + _type);
            if (c.equals(columns[columns.length - 1]))
                str = str.concat(")");
            else
                str = str.concat(", ");
        }
        //System.out.println("MySQL column string: `" + str + "`");
        return str;
    }

    private Enum<?>[] getColumns(DATABASE_TYPE type) {
        switch (type) {
            case CHUNK_DATA: return DatabaseChunkData.COLUMNS.values();
            case PLAYERS: return DatabasePlayers.COLUMNS.values();
            case QUEUE: return DatabaseQueue.COLUMNS.values();
            case COOLDOWN:
            default: return DatabaseCooldowns.COLUMNS.values();
        }
    }

    private String getColumnName(DATABASE_TYPE type, Enum<?> column) {
        switch (type) {
            case CHUNK_DATA: return ((DatabaseChunkData.COLUMNS) column).name;
            case PLAYERS: return ((DatabasePlayers.COLUMNS) column).name;
            case QUEUE: return ((DatabaseQueue.COLUMNS) column).name;
            case COOLDOWN:
            default: return ((DatabaseCooldowns.COLUMNS) column).name;
        }
    }

    private String getColumnType(DATABASE_TYPE type, Enum<?> column) {
        switch (type) {
            case CHUNK_DATA: return ((DatabaseChunkData.COLUMNS) column).type;
            case PLAYERS: return ((DatabasePlayers.COLUMNS) column).type;
            case QUEUE: return ((DatabaseQueue.COLUMNS) column).type;
            case COOLDOWN:
            default: return ((DatabaseCooldowns.COLUMNS) column).type;
        }
    }

    //Processing
    protected boolean sqlUpdate(String statement, @NonNull List<Object> params) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = true;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(statement);
            Iterator<Object> it = params.iterator();
            int paramIndex = 1;
            while (it.hasNext()) {
                ps.setObject(paramIndex, it.next());
                paramIndex++;
            }
            ps.executeUpdate();
        } catch (SQLException ex) {
            BetterRTP.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
            success = false;
        } finally {
            close(ps, null, conn);
        }
        return success;
    }

    boolean sqlUpdate(List<String> statement1, List<List<Object>> params1) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = true;
        try {
            conn = getSQLConnection();
            for (int i = 0; i < statement1.size(); i++) {
                String statement = statement1.get(i);
                List<Object> params = params1.get(i);
                if (ps == null)
                    ps = conn.prepareStatement(statement);
                else
                    ps.addBatch(statement);
                if (params != null) {
                    Iterator<Object> it = params.iterator();
                    int paramIndex = 1;
                    while (it.hasNext()) {
                        ps.setObject(paramIndex, it.next());
                        paramIndex++;
                    }
                }
            }
            assert ps != null;
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            BetterRTP.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
            success = false;
        } finally {
            close(ps, null, conn);
        }
        return success;
    }

    public void initialize() { //Let in console know if its all setup or not
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + tables.get(0) + " WHERE " + getColumnName(type, getColumns(type)[0]) + " = 0");

            rs = ps.executeQuery();
        } catch (SQLException ex) {
            BetterRTP.getInstance().getLogger().log(Level.SEVERE, "Unable to retrieve connection", ex);
        } finally {
            close(ps, rs, conn);
        }
    }

    protected void close(PreparedStatement ps, ResultSet rs, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException ex) {
            Error.close(BetterRTP.getInstance(), ex);
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public enum DATABASE_TYPE {
        PLAYERS,
        COOLDOWN,
        QUEUE,
        CHUNK_DATA,
    }
}
