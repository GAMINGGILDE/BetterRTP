package me.SuperRonanCraft.BetterRTP.references.settings;

import lombok.Getter;
import me.SuperRonanCraft.BetterRTP.references.file.FileOther;

public class Settings {

    @Getter private volatile boolean debug;
    @Getter private volatile boolean delayEnabled;
    @Getter private volatile int delayTime;
    @Getter private volatile boolean rtpOnFirstJoin_Enabled;
    @Getter private volatile String rtpOnFirstJoin_World;
    @Getter private volatile boolean rtpOnFirstJoin_SetAsRespawn;
    @Getter private volatile boolean statusMessages; //Send more information about rtp
    //Dependencies
    private final SoftDepends depends = new SoftDepends();
    @Getter private volatile boolean locationEnabled;
    @Getter private volatile boolean useLocationIfAvailable;
    @Getter private volatile boolean locationNeedPermission;
    @Getter private volatile boolean useLocationsInSameWorld;
    @Getter private volatile boolean permissionGroupEnabled;
    @Getter private volatile boolean queueEnabled;
    @Getter private volatile int chunkLoadTimeoutSeconds;
    @Getter private volatile int teleportTimeoutSeconds;
    //Placeholders
    @Getter private volatile String placeholder_true;
    @Getter private volatile String placeholder_nopermission;
    @Getter private volatile String placeholder_cooldown;
    @Getter private volatile String placeholder_balance;
    @Getter private volatile String placeholder_hunger;
    @Getter private volatile String placeholder_timeDays;
    @Getter private volatile String placeholder_timeHours;
    @Getter private volatile String placeholder_timeMinutes;
    @Getter private volatile String placeholder_timeSeconds;
    @Getter private volatile String placeholder_timeZero;
    @Getter private volatile String placeholder_timeInf;
    @Getter private volatile String placeholder_timeSeparator_middle;
    @Getter private volatile String placeholder_timeSeparator_last;


    public void load() { //Load Settings
        FileOther.FILETYPE config = FileOther.FILETYPE.CONFIG;
        debug = config.getBoolean("Settings.Debugger");
        delayEnabled = config.getBoolean("Settings.Delay.Enabled");
        delayTime = config.getInt("Settings.Delay.Time");
        rtpOnFirstJoin_Enabled = config.getBoolean("Settings.RtpOnFirstJoin.Enabled");
        rtpOnFirstJoin_World = config.getString("Settings.RtpOnFirstJoin.World");
        rtpOnFirstJoin_SetAsRespawn = config.getBoolean("Settings.RtpOnFirstJoin.SetAsRespawn");
        statusMessages = config.getBoolean("Settings.StatusMessages");
        permissionGroupEnabled = config.getBoolean("PermissionGroup.Enabled");
        queueEnabled = config.getBoolean("Settings.Queue.Enabled");
        chunkLoadTimeoutSeconds = Math.max(1, config.getInt("Settings.Timeouts.ChunkLoadSeconds"));
        teleportTimeoutSeconds = Math.max(1, config.getInt("Settings.Timeouts.TeleportSeconds"));
        locationEnabled = FileOther.FILETYPE.LOCATIONS.getBoolean("Enabled");
        useLocationIfAvailable = FileOther.FILETYPE.LOCATIONS.getBoolean("UseLocationIfAvailable");
        locationNeedPermission = FileOther.FILETYPE.LOCATIONS.getBoolean("RequirePermission");
        useLocationsInSameWorld = FileOther.FILETYPE.LOCATIONS.getBoolean("UseLocationsInSameWorld");
        //Placeholders
        placeholder_true = FileOther.FILETYPE.PLACEHOLDERS.getString("Config.CanRTP.Success");
        placeholder_nopermission = FileOther.FILETYPE.PLACEHOLDERS.getString("Config.CanRTP.NoPermission");
        placeholder_cooldown = FileOther.FILETYPE.PLACEHOLDERS.getString("Config.CanRTP.Cooldown");
        placeholder_balance = FileOther.FILETYPE.PLACEHOLDERS.getString("Config.CanRTP.Price");
        placeholder_hunger = FileOther.FILETYPE.PLACEHOLDERS.getString("Config.CanRTP.Hunger");
        placeholder_timeDays = FileOther.FILETYPE.PLACEHOLDERS.getString("Config.TimeFormat.Days");
        placeholder_timeHours = FileOther.FILETYPE.PLACEHOLDERS.getString("Config.TimeFormat.Hours");
        placeholder_timeMinutes = FileOther.FILETYPE.PLACEHOLDERS.getString("Config.TimeFormat.Minutes");
        placeholder_timeSeconds = FileOther.FILETYPE.PLACEHOLDERS.getString("Config.TimeFormat.Seconds");
        placeholder_timeZero = FileOther.FILETYPE.PLACEHOLDERS.getString("Config.TimeFormat.ZeroAll");
        placeholder_timeInf = FileOther.FILETYPE.PLACEHOLDERS.getString("Config.TimeFormat.Infinite");
        placeholder_timeSeparator_middle = FileOther.FILETYPE.PLACEHOLDERS.getString("Config.TimeFormat.Separator.Middle");
        placeholder_timeSeparator_last = FileOther.FILETYPE.PLACEHOLDERS.getString("Config.TimeFormat.Separator.Last");
        depends.load();
    }

    public SoftDepends getsDepends() {
        return depends;
    }
}
