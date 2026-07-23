package me.SuperRonanCraft.BetterRTP.references.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseSchemaMigratorTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void versionsLegacyDatabaseWithoutLosingData() throws Exception {
        Path database = temporaryDirectory.resolve("database.db");
        String jdbcUrl = "jdbc:sqlite:" + database;
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE legacy_data (id INTEGER PRIMARY KEY, value TEXT)");
            statement.executeUpdate("INSERT INTO legacy_data (id, value) VALUES (1, 'retained')");
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            DatabaseSchemaMigrator.migrate(connection, database, Logger.getAnonymousLogger());

            try (Statement statement = connection.createStatement();
                 ResultSet version = statement.executeQuery("PRAGMA user_version")) {
                assertTrue(version.next());
                assertEquals(DatabaseSchemaMigrator.CURRENT_VERSION, version.getInt(1));
            }
            try (Statement statement = connection.createStatement();
                 ResultSet data = statement.executeQuery("SELECT value FROM legacy_data WHERE id = 1")) {
                assertTrue(data.next());
                assertEquals("retained", data.getString(1));
            }
            try (Statement statement = connection.createStatement();
                 ResultSet migration = statement.executeQuery(
                         "SELECT version FROM betterrtp_schema_migrations")) {
                assertTrue(migration.next());
                assertEquals(1, migration.getInt(1));
            }
        }

        assertTrue(Files.exists(temporaryDirectory.resolve("database.db.schema-v0.bak")));
    }
}
