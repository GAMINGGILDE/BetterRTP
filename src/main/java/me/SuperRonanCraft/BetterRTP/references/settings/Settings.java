package me.SuperRonanCraft.BetterRTP.references.settings;

import me.SuperRonanCraft.BetterRTP.references.file.FileOther;

/**
 * Provides the current immutable configuration snapshot while preserving the
 * public getters used by BetterRTP add-ons.
 */
public class Settings {

    private final SoftDepends depends = new SoftDepends();
    private volatile SettingsSnapshot snapshot = SettingsSnapshot.empty();

    public void load() {
        SettingsSnapshot loaded = SettingsSnapshot.from(
                FileOther.FILETYPE.CONFIG.getConfig(),
                FileOther.FILETYPE.LOCATIONS.getConfig(),
                FileOther.FILETYPE.PLACEHOLDERS.getConfig());
        depends.load(loaded.debug());
        snapshot = loaded;
    }

    public SettingsSnapshot snapshot() {
        return snapshot;
    }

    public boolean isDebug() { return snapshot.debug(); }
    public boolean isDelayEnabled() { return snapshot.delayEnabled(); }
    public int getDelayTime() { return snapshot.delayTime(); }
    public boolean isRtpOnFirstJoin_Enabled() { return snapshot.rtpOnFirstJoinEnabled(); }
    public String getRtpOnFirstJoin_World() { return snapshot.rtpOnFirstJoinWorld(); }
    public boolean isRtpOnFirstJoin_SetAsRespawn() {
        return snapshot.rtpOnFirstJoinSetAsRespawn();
    }
    public boolean isStatusMessages() { return snapshot.statusMessages(); }
    public boolean isLocationEnabled() { return snapshot.locationEnabled(); }
    public boolean isUseLocationIfAvailable() { return snapshot.useLocationIfAvailable(); }
    public boolean isLocationNeedPermission() { return snapshot.locationNeedPermission(); }
    public boolean isUseLocationsInSameWorld() { return snapshot.useLocationsInSameWorld(); }
    public boolean isPermissionGroupEnabled() { return snapshot.permissionGroupEnabled(); }
    public boolean isQueueEnabled() { return snapshot.queueEnabled(); }
    public int getChunkLoadTimeoutSeconds() { return snapshot.chunkLoadTimeoutSeconds(); }
    public int getTeleportTimeoutSeconds() { return snapshot.teleportTimeoutSeconds(); }
    public String getPlaceholder_true() { return snapshot.placeholderTrue(); }
    public String getPlaceholder_nopermission() { return snapshot.placeholderNoPermission(); }
    public String getPlaceholder_cooldown() { return snapshot.placeholderCooldown(); }
    public String getPlaceholder_balance() { return snapshot.placeholderBalance(); }
    public String getPlaceholder_hunger() { return snapshot.placeholderHunger(); }
    public String getPlaceholder_timeDays() { return snapshot.placeholderTimeDays(); }
    public String getPlaceholder_timeHours() { return snapshot.placeholderTimeHours(); }
    public String getPlaceholder_timeMinutes() { return snapshot.placeholderTimeMinutes(); }
    public String getPlaceholder_timeSeconds() { return snapshot.placeholderTimeSeconds(); }
    public String getPlaceholder_timeZero() { return snapshot.placeholderTimeZero(); }
    public String getPlaceholder_timeInf() { return snapshot.placeholderTimeInfinite(); }
    public String getPlaceholder_timeSeparator_middle() {
        return snapshot.placeholderTimeSeparatorMiddle();
    }
    public String getPlaceholder_timeSeparator_last() {
        return snapshot.placeholderTimeSeparatorLast();
    }

    public SoftDepends getsDepends() {
        return depends;
    }
}
