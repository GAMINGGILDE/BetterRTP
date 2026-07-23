package me.SuperRonanCraft.BetterRTP.references.database;

import lombok.Getter;
import lombok.NonNull;
import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueData;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueHandler;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueLimits;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueRange;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueRepository;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueuePosition;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.RTPWorld;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.function.BooleanSupplier;

public class DatabaseQueue extends SQLite implements QueueRepository {

    private final BooleanSupplier queueEnabled;

    @Deprecated(forRemoval = false)
    public DatabaseQueue() {
        this(QueueHandler::isEnabled);
    }

    public DatabaseQueue(BooleanSupplier queueEnabled) {
        super(DATABASE_TYPE.QUEUE);
        this.queueEnabled = queueEnabled;
    }

    @Override
    public List<String> getTables() {
        List<String> list = new ArrayList<>();
        list.add("Queue");
        return list;
    }

    public enum COLUMNS {
        ID("id", "integer PRIMARY KEY AUTOINCREMENT"),
        //Location Data
        X("x", "long"),
        Z("z", "long"),
        WORLD("world", "varchar(32)"),
        GENERATED("generated", "long")
        ;

        public final String name;
        public final String type;

        COLUMNS(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    @Override public void load() {
        if (queueEnabled.getAsBoolean())
            super.load();
    }

    @Override
    public List<QueueData> findInRange(QueueRange range) {
        return queryInRange(range);
    }

    /** @deprecated Use {@link #findInRange(QueueRange)}. */
    @Deprecated(forRemoval = false)
    public List<QueueData> getInRange(QueueRangeData range) {
        return queryInRange(range.toRange());
    }

    private List<QueueData> queryInRange(QueueRange range) {
        final List<QueueData> queueDataList = new ArrayList<>();
        try {
            SQLiteExecutor.executor().submit(() -> {
                Connection conn = null;
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    conn = getSQLConnection();
                    ps = conn.prepareStatement("SELECT * FROM " + tables.get(0) + " WHERE "
                            + COLUMNS.WORLD.name + " = ? AND "
                            + COLUMNS.X.name + " BETWEEN ? AND ? AND "
                            + COLUMNS.Z.name + " BETWEEN ? AND ? "
                            + "ORDER BY RANDOM() LIMIT ?");
                    ps.setString(1, range.worldName());
                    ps.setInt(2, range.xLow());
                    ps.setInt(3, range.xHigh());
                    ps.setInt(4, range.zLow());
                    ps.setInt(5, range.zHigh());
                    ps.setInt(6, QueueLimits.DATABASE_FETCH_LIMIT);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        long x = rs.getLong(COLUMNS.X.name);
                        long z = rs.getLong(COLUMNS.Z.name);
                        int id = rs.getInt(COLUMNS.ID.name);
                        long generated = rs.getLong(COLUMNS.GENERATED.name);
                        queueDataList.add(new QueueData(
                                new Location(range.world(), x, 69, z), generated, id));
                    }
                } catch (SQLException ex) {
                    BetterRTP.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
                } finally {
                    close(ps, rs, conn);
                }
            }).get();
        } catch (Exception exception) {
            BetterRTP.getInstance().getLogger().log(
                    Level.SEVERE, "Unable to query RTP queue entries", exception);
        }
        return queueDataList;
    }

    /** Atomically reserves a queue row. Only one concurrent caller can succeed. */
    public boolean claim(int databaseId) {
        try {
            return SQLiteExecutor.executor().submit(() -> {
                String sql = "DELETE FROM " + tables.get(0) + " WHERE " + COLUMNS.ID.name + " = ?";
                try (Connection connection = getSQLConnection()) {
                    if (connection == null) {
                        return false;
                    }
                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                        statement.setInt(1, databaseId);
                        return statement.executeUpdate() == 1;
                    }
                } catch (SQLException exception) {
                    BetterRTP.getInstance().getLogger().log(
                            Level.SEVERE, Errors.sqlConnectionExecute(), exception);
                    return false;
                }
            }).get();
        } catch (Exception exception) {
            BetterRTP.getInstance().getLogger().log(Level.SEVERE, "Unable to reserve an RTP queue entry", exception);
            return false;
        }
    }

    //Set a queue to save
    @Override
    public QueueData save(QueuePosition position) {
        return addQueue(
                position.toLocation(),
                position.worldName(),
                position.blockX(),
                position.blockZ());
    }

    public QueueData addQueue(Location loc, String worldName, int blockX, int blockZ) {
        try {
            return SQLiteExecutor.executor().submit(() -> {
                String sql = "INSERT INTO " + tables.get(0) + " ("
                        + COLUMNS.X.name + ", "
                        + COLUMNS.Z.name + ", "
                        + COLUMNS.WORLD.name + ", "
                        + COLUMNS.GENERATED.name + ") VALUES(?, ?, ?, ?)";
                List<Object> params = List.of(
                        blockX, blockZ, worldName, System.currentTimeMillis());
                int databaseId = createQueue(sql, params);
                return databaseId >= 0 ? new QueueData(loc, System.currentTimeMillis(), databaseId) : null;
            }).get();
        } catch (Exception exception) {
            BetterRTP.getInstance().getLogger().log(
                    Level.SEVERE, "Unable to store an RTP queue entry", exception);
            return null;
        }
    }

    private int createQueue(String statement, @NonNull List<Object> params) {
        Connection conn = null;
        PreparedStatement ps = null;
        int id = -1;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
            Iterator<Object> it = params.iterator();
            int paramIndex = 1;
            while (it.hasNext()) {
                ps.setObject(paramIndex, it.next());
                paramIndex++;
            }
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException ex) {
            BetterRTP.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            close(ps, null, conn);
        }
        return id;
    }

    public boolean removeLocation(String worldName, int blockX, int blockZ) {
        try {
            return SQLiteExecutor.executor().submit(() -> {
                String sql = "DELETE FROM " + tables.get(0) + " WHERE "
                        + COLUMNS.X.name + " = ? AND "
                        + COLUMNS.Z.name + " = ? AND "
                        + COLUMNS.WORLD.name + " = ?";
                List<Object> params = List.of(blockX, blockZ, worldName);
                return sqlUpdate(sql, params);
            }).get();
        } catch (Exception exception) {
            BetterRTP.getInstance().getLogger().log(
                    Level.SEVERE, "Unable to remove an RTP queue entry", exception);
            return false;
        }
    }

    @Override
    public boolean remove(QueuePosition position) {
        return removeLocation(
                position.worldName(),
                position.blockX(),
                position.blockZ());
    }

    @Getter
    public static class QueueRangeData {

        int xLow, xHigh;
        int zLow, zHigh;
        World world;
        String worldName;

        public QueueRangeData(RTPWorld rtpWorld) {
            this(QueueRange.from(rtpWorld));
        }

        public QueueRangeData(QueueRange range) {
            this.xLow = range.xLow();
            this.xHigh = range.xHigh();
            this.zLow = range.zLow();
            this.zHigh = range.zHigh();
            this.world = range.world();
            this.worldName = range.worldName();
        }

        public QueueRange toRange() {
            return new QueueRange(xLow, xHigh, zLow, zHigh, world, worldName);
        }
    }
}
