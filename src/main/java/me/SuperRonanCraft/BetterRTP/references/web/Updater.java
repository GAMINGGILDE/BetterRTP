package me.SuperRonanCraft.BetterRTP.references.web;

import com.google.gson.JsonParser;
import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class Updater {

    private static final String RELEASE_API =
            "https://api.github.com/repos/GAMINGGILDE/BetterRTP/releases/latest";

    public static volatile String updatedVersion;
    private static volatile boolean updateAvailable;

    public Updater(BetterRTP plugin) {
        Logger logger = plugin.getLogger();
        String currentVersion = plugin.getPluginMeta().getVersion();
        updatedVersion = currentVersion;
        updateAvailable = false;

        AsyncHandler.async(() -> checkForUpdate(logger, currentVersion));
    }

    public static boolean isUpdateAvailable() {
        return updateAvailable;
    }

    private static void checkForUpdate(Logger logger, String currentVersion) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) URI.create(RELEASE_API).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
            connection.setRequestProperty("User-Agent", "BetterRTP-UpdateChecker");
            connection.setConnectTimeout(5_000);
            connection.setReadTimeout(5_000);

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IllegalStateException("GitHub returned HTTP " + status);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.UTF_8))) {
                String releaseVersion = JsonParser.parseReader(reader)
                        .getAsJsonObject()
                        .get("tag_name")
                        .getAsString();
                updatedVersion = ReleaseVersion.displayName(releaseVersion);
                updateAvailable = ReleaseVersion.compare(updatedVersion, currentVersion) > 0;
            }
        } catch (Exception ex) {
            logger.warning("Failed to check GitHub for a BetterRTP update: " + ex.getMessage());
            updatedVersion = currentVersion;
            updateAvailable = false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
