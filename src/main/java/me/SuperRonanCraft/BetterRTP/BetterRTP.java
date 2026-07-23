package me.SuperRonanCraft.BetterRTP;

import lombok.Getter;
import me.SuperRonanCraft.BetterRTP.player.PlayerInfo;
import me.SuperRonanCraft.BetterRTP.player.commands.Commands;
import me.SuperRonanCraft.BetterRTP.player.events.EventListener;
import me.SuperRonanCraft.BetterRTP.player.rtp.RTP;
import me.SuperRonanCraft.BetterRTP.references.Permissions;
import me.SuperRonanCraft.BetterRTP.references.RTPLogger;
import me.SuperRonanCraft.BetterRTP.references.WarningHandler;
import me.SuperRonanCraft.BetterRTP.references.database.DatabaseHandler;
import me.SuperRonanCraft.BetterRTP.references.depends.DepEconomy;
import me.SuperRonanCraft.BetterRTP.references.depends.DepPlaceholderAPI;
import me.SuperRonanCraft.BetterRTP.references.file.Files;
import me.SuperRonanCraft.BetterRTP.references.file.ConfigurationValidator;
import me.SuperRonanCraft.BetterRTP.references.file.FileOther;
import me.SuperRonanCraft.BetterRTP.references.helpers.HelperRTP;
import me.SuperRonanCraft.BetterRTP.references.invs.RTPInventories;
import me.SuperRonanCraft.BetterRTP.references.messages.Message_RTP;
import me.SuperRonanCraft.BetterRTP.references.messages.MessagesCore;
import me.SuperRonanCraft.BetterRTP.references.player.playerdata.PlayerDataManager;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.CooldownHandler;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueHandler;
import me.SuperRonanCraft.BetterRTP.references.settings.Settings;
import me.SuperRonanCraft.BetterRTP.references.web.Updater;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class BetterRTP extends JavaPlugin {
    @Getter private final Permissions perms = new Permissions();
    @Getter private final DepEconomy eco = new DepEconomy();
    @Getter private final Settings settings = new Settings();
    @Getter private final CooldownHandler cooldowns = new CooldownHandler();
    @Getter private final DatabaseHandler databaseHandler = new DatabaseHandler(settings::isQueueEnabled);
    @Getter private final Commands cmd = new Commands(this);
    @Getter private final RTP RTP = new RTP(
            eco, settings, cooldowns, this::getLogger, this, this::getQueue);
    private final EventListener listener = new EventListener();
    @Getter private static BetterRTP instance;
    @Getter private final Files files = new Files();
    @Getter private final RTPInventories invs = new RTPInventories();
    @Getter private final PlayerInfo pInfo = new PlayerInfo();
    @Getter private final PlayerDataManager playerDataManager = new PlayerDataManager();
    @Getter private final QueueHandler queue = new QueueHandler(
            settings::isQueueEnabled,
            databaseHandler.getDatabaseQueue(),
            this::getRTP,
            settings::getChunkLoadTimeoutSeconds,
            this::getLogger,
            HelperRTP::getWorldType,
            BetterRTP::debug);
    @Getter private final WarningHandler warningHandler = new WarningHandler();
    @Getter private boolean PlaceholderAPI;
    @Getter private final RTPLogger rtpLogger = new RTPLogger();
    private final AtomicBoolean reloading = new AtomicBoolean();

    @Override
    public void onEnable() {
        instance = this;
        registerDependencies();
        loadAll();
        if (!files.getType(FileOther.FILETYPE.CONFIG)
                .getBoolean("Settings.DisableUpdater")) {
            new Updater(this);
        }
        listener.registerEvents(this);
        queue.registerEvents(this);
        try {
            new DepPlaceholderAPI().register();
        } catch (NoClassDefFoundError e) {
            //No placeholder api :(
        }
    }

    @Override
    public void onDisable() {
        RTP.shutdown();
        pInfo.clearInvs();
        queue.unload();
        cooldowns.unload();
        databaseHandler.shutdown();
        rtpLogger.unload();
    }

    private void registerDependencies() {
        PlaceholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    @Override
    public boolean onCommand(CommandSender sendi, Command cmd, String label, String[] args) {
        if (reloading.get()) {
            Message_RTP.sms(sendi, "&eBetterRTP is currently reloading. Please try again shortly.");
            return true;
        }
        try {
            this.cmd.commandExecuted(sendi, label, args);
        } catch (RuntimeException exception) {
            getLogger().log(Level.SEVERE,
                    "Unable to execute /" + label + " for " + sendi.getName(), exception);
            Message_RTP.sms(sendi,
                    "&cERROR &7The command could not be completed. Check the server log for details.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return this.cmd.onTabComplete(sender, args);
    }

    public void reload(CommandSender sendi) {
        if (!reloading.compareAndSet(false, true)) {
            Message_RTP.sms(sendi, "&eBetterRTP is already reloading.");
            return;
        }
        try {
            RTP.shutdownForReload().whenComplete((ignored, cancellationFailure) ->
                    AsyncHandler.global(() -> continueReload(sendi, cancellationFailure)));
        } catch (RuntimeException exception) {
            failReload(sendi, exception);
        }
    }

    public boolean isReloading() {
        return reloading.get();
    }

    private void continueReload(CommandSender sender, Throwable cancellationFailure) {
        try {
            if (cancellationFailure != null) {
                getLogger().log(
                        Level.WARNING,
                        "One or more RTP sessions could not be cleanly cancelled",
                        cancellationFailure);
            }
            queue.unload();
            invs.closeAllOnEntitySchedulers().whenComplete((ignored, inventoryFailure) ->
                    AsyncHandler.global(() -> finishReload(sender, inventoryFailure)));
        } catch (RuntimeException exception) {
            failReload(sender, exception);
        }
    }

    private void finishReload(CommandSender sender, Throwable inventoryFailure) {
        try {
            if (inventoryFailure != null) {
                getLogger().log(
                        Level.WARNING,
                        "One or more BetterRTP inventories could not be cleanly closed",
                        inventoryFailure);
            }
            loadAll();
            AsyncHandler.syncAtSender(sender, () -> MessagesCore.RELOAD.send(sender));
        } catch (RuntimeException exception) {
            failReload(sender, exception);
        } finally {
            reloading.set(false);
        }
    }

    private void failReload(CommandSender sender, RuntimeException exception) {
        getLogger().log(Level.SEVERE, "Unable to reload BetterRTP", exception);
        AsyncHandler.syncAtSender(sender, () -> Message_RTP.sms(sender,
                "&cBetterRTP could not be reloaded. Check the server log."));
        reloading.set(false);
    }

    //(Re)Load all plugin systems/files/cache
    private void loadAll() {
        playerDataManager.clear();
        AsyncHandler.asyncFuture(files::loadAll).join();
        ConfigurationValidator.validateAll(this);
        settings.load();
        cooldowns.load();
        databaseHandler.load();
        AsyncHandler.asyncFuture(() -> rtpLogger.setup(this)).join();
        invs.load();
        RTP.load();
        cmd.load();
        listener.load();
        eco.load();
        perms.register();
        queue.load();
    }

    public static void debug(String str) {
        getInstance().getLogger().info(str);
    }
}
