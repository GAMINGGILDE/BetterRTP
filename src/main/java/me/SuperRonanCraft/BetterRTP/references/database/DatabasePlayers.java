package me.SuperRonanCraft.BetterRTP.references.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import me.SuperRonanCraft.BetterRTP.BetterRTP;

public class DatabasePlayers extends SQLite {

    public DatabasePlayers() {
        super(DATABASE_TYPE.PLAYERS);
    }

    @Override
    public List<String> getTables() {
        List<String> list = new ArrayList<>();
        list.add("Players");
        return list;
    }

    public enum COLUMNS {
        UUID("uuid", "varchar(32) PRIMARY KEY"),
        //COOLDOWN DATA
        COUNT("count", "long"),
        LAST_COOLDOWN_DATE("last_rtp_date", "long"),
        ;

        public final String name;
        public final String type;

        COLUMNS(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    public PlayerRecord getData(UUID uuid) {
        try {
            return SQLiteExecutor.executor().submit(() -> readData(uuid)).get();
        } catch (Exception exception) {
            BetterRTP.getInstance().getLogger().log(Level.SEVERE, "Unable to load RTP player data", exception);
            return new PlayerRecord(0, 0L);
        }
    }

    private PlayerRecord readData(UUID uuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + tables.get(0) + " WHERE " + COLUMNS.UUID.name + " = ?");
            ps.setString(1, uuid.toString());
            rs = ps.executeQuery();
            if (rs.next()) {
                return new PlayerRecord(
                        Math.toIntExact(rs.getLong(COLUMNS.COUNT.name)),
                        rs.getLong(COLUMNS.LAST_COOLDOWN_DATE.name));
            }
        } catch (SQLException ex) {
            BetterRTP.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            close(ps, rs, conn);
        }
        return new PlayerRecord(0, 0L);
    }

    //Set a player Cooldown
    public void setData(UUID uuid, int rtpCount, long globalCooldown) {
        try {
            SQLiteExecutor.executor().submit(() -> {
                String sql = "INSERT OR REPLACE INTO " + tables.get(0) + " ("
                    + COLUMNS.UUID.name + ", "
                    + COLUMNS.COUNT.name + ", "
                    + COLUMNS.LAST_COOLDOWN_DATE.name + ") VALUES(?, ?, ?)";
                List<Object> params = new ArrayList<Object>() {{
                    add(uuid.toString());
                    add(rtpCount);
                    add(globalCooldown);
                }};
                sqlUpdate(sql, params);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public record PlayerRecord(int rtpCount, long globalCooldown) {
    }
}
