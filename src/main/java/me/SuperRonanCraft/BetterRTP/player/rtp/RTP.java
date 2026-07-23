package me.SuperRonanCraft.BetterRTP.player.rtp;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import lombok.Getter;
import me.SuperRonanCraft.BetterRTP.references.customEvents.RTP_SettingUpEvent;
import me.SuperRonanCraft.BetterRTP.references.file.FileOther;
import me.SuperRonanCraft.BetterRTP.references.helpers.HelperRTP;
import me.SuperRonanCraft.BetterRTP.references.helpers.HelperRTP_Check;
import me.SuperRonanCraft.BetterRTP.references.depends.DepEconomy;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.PermissionGroup;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.CooldownHandler;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueHandler;
import me.SuperRonanCraft.BetterRTP.references.settings.Settings;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.RTPWorld;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WORLD_TYPE;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldDefault;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldPlayer;

import java.util.function.Supplier;
import java.util.function.BooleanSupplier;
import java.util.logging.Logger;

public class RTP {

    private final DepEconomy economy;
    private final Settings pluginSettings;
    private final CooldownHandler cooldowns;
    private final Supplier<Logger> logger;
    private final Plugin eventOwner;
    private final Supplier<QueueHandler> queue;
    private final BooleanSupplier reloading;
    private final RTPLoader loader;
    @Getter private final RTPTeleport teleport;
    @Getter private final RTPSessionManager sessions = new RTPSessionManager();
    //Cache
    public final ConcurrentMap<String, String> overriden = new ConcurrentHashMap<>();
    private volatile RtpRuntimeSettings runtimeSettings = RtpRuntimeSettings.empty();
    public final ConcurrentMap<String, WORLD_TYPE> world_type = new ConcurrentHashMap<>();
    //Worlds
    @Getter private volatile WorldDefault RTPdefaultWorld = new WorldDefault();
    @Getter private final ConcurrentMap<String, RTPWorld> RTPcustomWorld = new ConcurrentHashMap<>();
    @Getter private final ConcurrentMap<String, RTPWorld> RTPworldLocations = new ConcurrentHashMap<>();
    @Getter private final ConcurrentMap<String, PermissionGroup> permissionGroups = new ConcurrentHashMap<>();

    /** Compatibility constructor for lifecycle-only use outside the running plugin. */
    public RTP() {
        this(null, null, null, () -> Logger.getLogger(RTP.class.getName()),
                null, () -> null, () -> false);
    }

    public RTP(
            DepEconomy economy,
            Settings pluginSettings,
            CooldownHandler cooldowns,
            Supplier<Logger> logger,
            Plugin eventOwner,
            Supplier<QueueHandler> queue) {
        this(economy, pluginSettings, cooldowns, logger, eventOwner, queue, () -> false);
    }

    public RTP(
            DepEconomy economy,
            Settings pluginSettings,
            CooldownHandler cooldowns,
            Supplier<Logger> logger,
            Plugin eventOwner,
            Supplier<QueueHandler> queue,
            BooleanSupplier reloading) {
        this.economy = economy;
        this.pluginSettings = pluginSettings;
        this.cooldowns = cooldowns;
        this.logger = logger;
        this.eventOwner = eventOwner;
        this.queue = queue;
        this.reloading = reloading;
        this.loader = new RTPLoader(pluginSettings, logger);
        this.teleport = new RTPTeleport(this);
    }

    public void load() {
        sessions.cancelAll();
        FileOther.FILETYPE config = FileOther.FILETYPE.CONFIG;
        RtpRuntimeSettings loadedRuntimeSettings =
                RtpRuntimeSettings.from(config.getConfig());
        loadConfigurationState();
        teleport.load(); //Load teleporting stuff
        runtimeSettings = loadedRuntimeSettings;
    }

    public void loadWorlds() { //Keeping this here because of the edit command
        WorldDefault loadedDefault = new WorldDefault();
        Map<String, RTPWorld> loaded = new HashMap<>();
        loader.loadWorlds(loadedDefault, loaded);
        RTPdefaultWorld = loadedDefault;
        replaceContents(RTPcustomWorld, loaded);
    }

    public void loadLocations() { //Keeping this here because of the edit command
        Map<String, RTPWorld> loaded = new HashMap<>();
        loader.loadLocations(loaded, RTPdefaultWorld);
        replaceContents(RTPworldLocations, loaded);
    }

    public void loadPermissionGroups() { //Keeping this here because of the edit command
        Map<String, PermissionGroup> loaded = new HashMap<>();
        loader.loadPermissionGroups(loaded, RTPdefaultWorld);
        replaceContents(permissionGroups, loaded);
    }

    public void start(RTPSetupInformation setup_info) {
        if (isReloading()) {
            return;
        }
        start(HelperRTP.getPlayerWorld(setup_info));
    }

    public void start(WorldPlayer pWorld) {
        if (isReloading()) {
            return;
        }
        RTP_SettingUpEvent setup = new RTP_SettingUpEvent(pWorld.getPlayer());
        Bukkit.getPluginManager().callEvent(setup);
        if (setup.isCancelled())
            return;
        rtp(pWorld.getSendi(), pWorld);
    }

    private void rtp(CommandSender sendi, WorldPlayer pWorld) {
        //Cooldown
        Player p = pWorld.getPlayer();
        RTPPlayer rtpPlayer = new RTPPlayer(this, pWorld);
        if (!sessions.register(rtpPlayer)) {
            return;
        }
        // Delaying? Else, just go
        if (rtpPlayer.getRequest().options().applyDelay()
                && HelperRTP_Check.applyDelay(pWorld.getPlayer())) {
            RtpRuntimeSettings current = runtimeSettings;
            new RTPDelay(sendi, rtpPlayer, current.delayTime(),
                    current.cancelOnMove(), current.cancelOnDamage());
        } else {
            if (!teleport.beforeTeleportInstant(sendi, p)) {
                rtpPlayer.randomlyTeleport(sendi);
            } else {
                rtpPlayer.cancel();
            }
        }
    }

    public void cancel(Player player) {
        sessions.cancel(player);
    }

    public void shutdown() {
        sessions.cancelAll();
    }

    public CompletableFuture<Void> shutdownForReload() {
        return sessions.cancelAllOnEntitySchedulers();
    }

    private boolean isReloading() {
        return reloading.getAsBoolean();
    }

    public List<String> getDisabledWorlds() {
        return runtimeSettings.disabledWorlds();
    }

    public List<String> getBlockList() {
        return runtimeSettings.blockedBlocks();
    }

    RtpRuntimeSettings runtimeSettings() {
        return runtimeSettings;
    }

    private void loadConfigurationState() {
        Map<String, String> loadedOverrides = new HashMap<>();
        Map<String, WORLD_TYPE> loadedWorldTypes = new HashMap<>();
        Map<String, RTPWorld> loadedCustomWorlds = new HashMap<>();
        Map<String, RTPWorld> loadedLocations = new HashMap<>();
        Map<String, PermissionGroup> loadedPermissionGroups = new HashMap<>();
        WorldDefault loadedDefault = new WorldDefault();

        loader.loadOverrides(loadedOverrides);
        loader.loadWorldTypes(loadedWorldTypes);
        loader.loadWorlds(loadedDefault, loadedCustomWorlds);
        loader.loadLocations(loadedLocations, loadedDefault);
        loader.loadPermissionGroups(loadedPermissionGroups, loadedDefault);

        RTPdefaultWorld = loadedDefault;
        replaceContents(overriden, loadedOverrides);
        replaceContents(world_type, loadedWorldTypes);
        replaceContents(RTPcustomWorld, loadedCustomWorlds);
        replaceContents(RTPworldLocations, loadedLocations);
        replaceContents(permissionGroups, loadedPermissionGroups);
    }

    private static <K, V> void replaceContents(
            ConcurrentMap<K, V> target, Map<K, V> replacement) {
        target.clear();
        target.putAll(replacement);
    }

    DepEconomy economy() { return economy; }

    Settings pluginSettings() { return pluginSettings; }

    CooldownHandler cooldowns() { return cooldowns; }

    Logger logger() { return logger.get(); }

    Plugin eventOwner() { return eventOwner; }

    QueueHandler queue() { return queue.get(); }
}
