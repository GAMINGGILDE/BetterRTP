package me.SuperRonanCraft.BetterRTP.references.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

final class DatabaseSchemaMigrator {

    static final int CURRENT_VERSION = 1;
    private static final DateTimeFormatter BACKUP_SUFFIX = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private DatabaseSchemaMigrator() {
    }

    static void migrate(Connection connection, Path databaseFile, Logger logger) throws SQLException, IOException {
        int sourceVersion = readVersion(connection);
        if (sourceVersion > CURRENT_VERSION) {
            throw new SQLException("Database schema version " + sourceVersion
                    + " is newer than supported version " + CURRENT_VERSION);
        }
        if (sourceVersion == CURRENT_VERSION) {
            return;
        }

        Path backup = backup(databaseFile, sourceVersion);
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            int version = sourceVersion;
            while (version < CURRENT_VERSION) {
                if (version == 0) {
                    migrateZeroToOne(connection);
                    version = 1;
                } else {
                    throw new SQLException("No migration exists for database schema version " + version);
                }
                setVersion(connection, version);
            }
            connection.commit();
            logger.info("Migrated SQLite schema from version " + sourceVersion + " to "
                    + CURRENT_VERSION + " (backup: " + backup.getFileName() + ")");
        } catch (SQLException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private static void migrateZeroToOne(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS betterrtp_schema_migrations ("
                    + "version INTEGER PRIMARY KEY, applied_at INTEGER NOT NULL)");
            statement.executeUpdate("INSERT OR REPLACE INTO betterrtp_schema_migrations "
                    + "(version, applied_at) VALUES (1, " + System.currentTimeMillis() + ")");
        }
    }

    private static int readVersion(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA user_version")) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    private static void setVersion(Connection connection, int version) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA user_version = " + version);
        }
    }

    private static Path backup(Path databaseFile, int sourceVersion) throws IOException {
        Path backup = databaseFile.resolveSibling(databaseFile.getFileName() + ".schema-v"
                + sourceVersion + ".bak");
        if (Files.exists(backup)) {
            String timestamp = BACKUP_SUFFIX.format(LocalDateTime.now());
            int counter = 0;
            do {
                String suffix = counter == 0 ? "" : "-" + counter;
                counter++;
                backup = databaseFile.resolveSibling(databaseFile.getFileName() + ".schema-v"
                        + sourceVersion + "." + timestamp + suffix + ".bak");
            } while (Files.exists(backup));
        }
        return Files.copy(databaseFile, backup, StandardCopyOption.COPY_ATTRIBUTES);
    }
}
