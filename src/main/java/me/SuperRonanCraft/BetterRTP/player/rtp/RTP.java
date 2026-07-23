package me.SuperRonanCraft.BetterRTP.player.rtp;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import lombok.Getter;
import me.SuperRonanCraft.BetterRTP.BetterRTP;
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
import java.util.logging.Logger;

public class RTP {

    private final DepEconomy economy;
    private final Settings pluginSettings;
    private final CooldownHandler cooldowns;
    private final Supplier<Logger> logger;
    private final Plugin eventOwner;
    private final Supplier<QueueHandler> queue;
    @Getter private final RTPTeleport teleport;
    @Getter private final RTPSessionManager sessions = new RTPSessionManager();
    //Cache
    public final ConcurrentMap<String, String> overriden = new ConcurrentHashMap<>();
    @Getter volatile List<String> disabledWorlds = List.of(), blockList = List.of();
    volatile int maxAttempts, delayTime;
    volatile boolean cancelOnMove, cancelOnDamage;
    public final ConcurrentMap<String, WORLD_TYPE> world_type = new ConcurrentHashMap<>();
    //Worlds
    @Getter private final WorldDefault RTPdefaultWorld = new WorldDefault();
    @Getter private final ConcurrentMap<String, RTPWorld> RTPcustomWorld = new ConcurrentHashMap<>();
    @Getter private final ConcurrentMap<String, RTPWorld> RTPworldLocations = new ConcurrentHashMap<>();
    @Getter private final ConcurrentMap<String, PermissionGroup> permissionGroups = new ConcurrentHashMap<>();

    /** Compatibility constructor for lifecycle-only use outside the running plugin. */
    public RTP() {
        this(null, null, null, () -> Logger.getLogger(RTP.class.getName()), null, () -> null);
    }

    public RTP(
            DepEconomy economy,
            Settings pluginSettings,
            CooldownHandler cooldowns,
            Supplier<Logger> logger,
            Plugin eventOwner,
            Supplier<QueueHandler> queue) {
        this.economy = economy;
        this.pluginSettings = pluginSettings;
        this.cooldowns = cooldowns;
        this.logger = logger;
        this.eventOwner = eventOwner;
        this.queue = queue;
        this.teleport = new RTPTeleport(this);
    }

    public void load() {
        sessions.cancelAll();
        FileOther.FILETYPE config = FileOther.FILETYPE.CONFIG;
        disabledWorlds = List.copyOf(config.getStringList("DisabledWorlds"));
        maxAttempts = config.getInt("Settings.MaxAttempts");
        delayTime = config.getInt("Settings.Delay.Time");
        cancelOnMove = config.getBoolean("Settings.Delay.CancelOnMove");
        cancelOnDamage = config.getBoolean("Settings.Delay.CancelOnDamage");
        blockList = List.copyOf(config.getStringList("BlacklistedBlocks"));
        //Overrides
        RTPLoader.loadOverrides(overriden);
        //WorldType
        RTPLoader.loadWorldTypes(world_type);
        //Worlds & CustomWorlds
        loadWorlds();
        //Locations
        loadLocations();
        //Permissions
        loadPermissionGroups();
        teleport.load(); //Load teleporting stuff
    }

    public void loadWorlds() { //Keeping this here because of the edit command
        RTPLoader.loadWorlds(RTPdefaultWorld, RTPcustomWorld);
    }

    public void loadLocations() { //Keeping this here because of the edit command
        RTPLoader.loadLocations(RTPworldLocations);
    }

    public void loadPermissionGroups() { //Keeping this here because of the edit command
        RTPLoader.loadPermissionGroups(permissionGroups);
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
            new RTPDelay(sendi, rtpPlayer, delayTime, cancelOnMove, cancelOnDamage);
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
        return eventOwner instanceof BetterRTP plugin && plugin.isReloading();
    }

    DepEconomy economy() { return economy; }

    Settings pluginSettings() { return pluginSettings; }

    CooldownHandler cooldowns() { return cooldowns; }

    Logger logger() { return logger.get(); }

    Plugin eventOwner() { return eventOwner; }

    QueueHandler queue() { return queue.get(); }
}
