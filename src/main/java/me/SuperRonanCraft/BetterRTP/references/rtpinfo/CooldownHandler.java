package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lombok.Getter;
import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.references.database.DatabaseCooldowns;
import me.SuperRonanCraft.BetterRTP.references.database.DatabaseHandler;
import me.SuperRonanCraft.BetterRTP.references.database.DatabasePlayers;
import me.SuperRonanCraft.BetterRTP.references.file.FileOther;
import me.SuperRonanCraft.BetterRTP.references.player.HelperPlayer;
import me.SuperRonanCraft.BetterRTP.references.player.playerdata.PlayerData;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldPlayer;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;

public class CooldownHandler {

    @Getter boolean enabled, loaded, cooldownByWorld;
    @Getter private int defaultCooldownTime; //Global Cooldown timer
    private int lockedAfter; //Rtp's before being locked
    private final Set<UUID> downloading = ConcurrentHashMap.newKeySet();
    private final AtomicLong generation = new AtomicLong();

    public void load() {
        long runId = generation.incrementAndGet();
        FileOther.FILETYPE config = FileOther.FILETYPE.CONFIG;
        enabled = config.getBoolean("Settings.Cooldown.Enabled");
        downloading.clear();
        loaded = false;
        if (enabled) {
            defaultCooldownTime = config.getInt("Settings.Cooldown.Time");
            BetterRTP.debug("Cooldown = " + defaultCooldownTime);
            lockedAfter = config.getInt("Settings.Cooldown.LockAfter");
            cooldownByWorld = config.getBoolean("Settings.Cooldown.PerWorld");
        }
        queueDownload(runId);
    }

    public void unload() {
        generation.incrementAndGet();
        downloading.clear();
        loaded = false;
    }

    private void queueDownload(long runId) {
        AsyncHandler.asyncLater(() -> {
            if (generation.get() != runId) {
                return;
            }
            if (cooldownByWorld && !DatabaseHandler.getCooldowns().isLoaded()) {
               queueDownload(runId);
               return;
            }
            if (!DatabaseHandler.getPlayers().isLoaded()) {
               queueDownload(runId);
               return;
            }
            AsyncHandler.sync(() -> {
                if (generation.get() != runId) {
                    return;
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    AsyncHandler.syncAtEntity(player, () -> loadPlayer(player, runId));
                }
                loaded = true;
            });
        }, 10L);
    }

    public void add(Player player, World world) {
        if (!enabled) return;
        PlayerData playerData = getData(player);
        if (cooldownByWorld) {
            Map<String, CooldownData> cooldowns = playerData.getCooldowns();
            String worldName = world.getName();
            CooldownData data = cooldowns.getOrDefault(worldName, new CooldownData(player.getUniqueId(), 0L));
            playerData.setRtpCount(playerData.getRtpCount() + 1);
            data.setTime(System.currentTimeMillis());
            playerData.setGlobalCooldown(data.getTime());
            cooldowns.put(worldName, data);
            savePlayer(playerData, worldName, data);
        } else
            add(player);
    }

    private void add(Player player) {
        if (!enabled) return;
        PlayerData playerData = getData(player);
        playerData.setRtpCount(playerData.getRtpCount() + 1);
        playerData.setGlobalCooldown(System.currentTimeMillis());
        savePlayer(playerData, null, null);
    }

    @Nullable
    public CooldownData get(Player p, World world) {
        PlayerData data = getData(p);
        if (cooldownByWorld) {
            Map<String, CooldownData> cooldownData = getData(p).getCooldowns();
            if (data != null)
                return cooldownData.getOrDefault(world.getName(), null);
        } else if (data.getGlobalCooldown() > 0) {
            return new CooldownData(p.getUniqueId(), data.getGlobalCooldown());
        }
        return null;
    }

    public long timeLeft(CommandSender sendi, CooldownData data, WorldPlayer pWorld) {
        return CooldownPolicy.remainingMillis(
                data.getTime(), pWorld.getCooldown(), System.currentTimeMillis());
    }

    public boolean locked(Player player) {
        return CooldownPolicy.isLocked(getData(player).getRtpCount(), lockedAfter);
    }

    private void savePlayer(PlayerData playerData, @Nullable String worldName, @Nullable CooldownData data) {
        UUID uuid = playerData.getUuid();
        int rtpCount = playerData.getRtpCount();
        long globalCooldown = playerData.getGlobalCooldown();
        AsyncHandler.async(() -> {
                if (worldName != null && data != null && getDatabaseWorlds() != null) {
                    getDatabaseWorlds().setCooldown(worldName, data);
                }
                DatabaseHandler.getPlayers().setData(uuid, rtpCount, globalCooldown);
            });
    }

    public void loadPlayer(Player player) {
        loadPlayer(player, generation.get());
    }

    private void loadPlayer(Player player, long runId) {
        if (!isEnabled()) {
          return;
        }

        PlayerData playerData = getData(player);
        if (playerData == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        downloading.add(uuid);
        AsyncHandler.sync(() -> {
            if (generation.get() != runId) {
                downloading.remove(uuid);
                return;
            }
            java.util.List<String> worldNames = Bukkit.getWorlds().stream().map(World::getName).toList();
            AsyncHandler.async(() -> {
                if (generation.get() != runId) {
                    downloading.remove(uuid);
                    return;
                }
                Map<String, CooldownData> cooldowns = new java.util.HashMap<>();
                if (getDatabaseWorlds() != null) {
                    for (String worldName : worldNames) {
                        CooldownData cooldown = getDatabaseWorlds().getCooldown(uuid, worldName);
                        if (cooldown != null) {
                            cooldowns.put(worldName, cooldown);
                        }
                    }
                }
                DatabasePlayers.PlayerRecord storedPlayer = DatabaseHandler.getPlayers().getData(uuid);
                AsyncHandler.syncAtEntity(player, () -> {
                    try {
                        if (generation.get() != runId) {
                            return;
                        }
                        PlayerData current = getData(player);
                        if (current.getUuid().equals(uuid)) {
                            current.getCooldowns().putAll(cooldowns);
                            current.setRtpCount(storedPlayer.rtpCount());
                            current.setGlobalCooldown(storedPlayer.globalCooldown());
                        }
                    } finally {
                        downloading.remove(uuid);
                    }
                }, () -> downloading.remove(uuid));
            });
        });
    }

    public boolean loadedPlayer(Player player) {
        return !downloading.contains(player.getUniqueId());
    }

    @Nullable
    private DatabaseCooldowns getDatabaseWorlds() {
        if (cooldownByWorld)
            return DatabaseHandler.getCooldowns();
        return null;
    }

    private PlayerData getData(Player p) {
        return HelperPlayer.getData(p);
    }
}
