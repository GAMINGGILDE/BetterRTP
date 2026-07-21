package me.SuperRonanCraft.BetterRTP.references.web;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.util.logging.Logger;

public class Updater {

    public static String updatedVersion = BetterRTP.getInstance().getPluginMeta().getVersion();

    public Updater(BetterRTP pl) {
        Logger logger = pl.getLogger();
        String currentVersion = pl.getPluginMeta().getVersion();
        AsyncHandler.async(() -> {
            try {
                URLConnection con = URI.create(getUrl() + project()).toURL().openConnection();
                con.setConnectTimeout(5_000);
                con.setReadTimeout(5_000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                updatedVersion = reader.readLine();
            } catch (Exception ex) {
                logger.warning("Failed to check for a BetterRTP update");
                updatedVersion = currentVersion;
            }
        });
    }

    private String getUrl() {
        return "https://api.spigotmc.org/legacy/update.php?resource=";
    }

    private String project() {
        return "36081";
    }
}
