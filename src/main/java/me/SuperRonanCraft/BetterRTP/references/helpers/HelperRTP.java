package me.SuperRonanCraft.BetterRTP.references.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.player.commands.types.CmdLocation;
import me.SuperRonanCraft.BetterRTP.player.rtp.RTP;
import me.SuperRonanCraft.BetterRTP.player.rtp.RTPSetupInformation;
import me.SuperRonanCraft.BetterRTP.player.rtp.RtpSetupRequest;
import me.SuperRonanCraft.BetterRTP.player.rtp.RTP_ERROR_REQUEST_REASON;
import me.SuperRonanCraft.BetterRTP.player.rtp.RTP_PlayerInfo;
import me.SuperRonanCraft.BetterRTP.player.rtp.RTP_TYPE;
import me.SuperRonanCraft.BetterRTP.references.PermissionCheck;
import me.SuperRonanCraft.BetterRTP.references.WarningHandler;
import me.SuperRonanCraft.BetterRTP.references.messages.Message_RTP;
import me.SuperRonanCraft.BetterRTP.references.messages.placeholder.Placeholders;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.PermissionGroup;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.RTPWorld;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.ResolvedRtpWorld;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.RtpWorldResolver;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WORLD_TYPE;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldLocation;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldPermissionGroup;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldPlayer;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;

public class HelperRTP {

    //Teleported and Sender are the same
    public static void tp(Player player,
                          World world,
                          List<String> biomes,
                          RTP_TYPE rtpType) {
        tp(player, player, world, biomes, rtpType);
    }

    //Teleported and Sender MAY be different
    public static void tp(Player player,
                          CommandSender sendi,
                          World world,
                          List<String> biomes,
                          RTP_TYPE rtpType) {
        tp(player, sendi, world, biomes, rtpType, false, false);
    }

    //
    public static void tp(Player player,
                          CommandSender sendi,
                          World world,
                          List<String> biomes,
                          RTP_TYPE rtpType,
                          boolean ignoreCooldown,
                          boolean ignoreDelay) {
        tp(player, sendi, world, biomes, rtpType, ignoreCooldown, ignoreDelay, null);
    }

    public static void tp(@NotNull Player player,
                          CommandSender sendi,
                          @Nullable World world,
                          List<String> biomes,
                          RTP_TYPE rtpType,
                          boolean ignoreCooldown,
                          boolean ignoreDelay,
                          @Nullable WorldLocation location) {
        tp(player, sendi, world, biomes, rtpType, location, new RTP_PlayerInfo(!ignoreDelay, true, !ignoreCooldown));
    }

    public static void tp(@NotNull Player player,
                          CommandSender sendi,
                          @Nullable World world,
                          List<String> biomes,
                          RTP_TYPE rtpType,
                          @Nullable WorldLocation location,
                          @NotNull RTP_PlayerInfo playerInfo) {
        World requestedWorld = world;
        AsyncHandler.syncAtEntity(player, () -> {
            World actualWorld = getActualWorld(player, requestedWorld, location);
            RTPSetupInformation setupInfo = new RTPSetupInformation(
                    actualWorld, sendi, player, true, biomes, rtpType, location, playerInfo);
            tpOnEntity(player, sendi, setupInfo);
        });
    }

    public static void tp(@NotNull Player player,
                          CommandSender sendi,
                          @NotNull RTPSetupInformation setup_info) {
        AsyncHandler.syncAtEntity(player, () -> tpOnEntity(player, sendi, setup_info));
    }

    private static void tpOnEntity(Player player, CommandSender sendi, RTPSetupInformation setup_info) {
        //RTP request cancelled reason
        WorldPlayer pWorld = getPlayerWorld(setup_info);
        RTP_ERROR_REQUEST_REASON cantReason = HelperRTP_Check.canRTP(player, sendi, pWorld, setup_info.getPlayerInfo());
        if (cantReason != null) {
            String msg = cantReason.getMsg().get(player, null);
            if (cantReason == RTP_ERROR_REQUEST_REASON.COOLDOWN) {
                msg = msg.replace(Placeholders.COOLDOWN.name, HelperDate.total(HelperRTP_Check.getCooldown(player, pWorld)));
                msg = msg.replace(Placeholders.TIME.name, HelperDate.total(HelperRTP_Check.getCooldown(player, pWorld)));
            }
            Message_RTP.sms(player, msg, pWorld);
            if (sendi != player)
                Message_RTP.sms(sendi, msg, pWorld);
            return;
        }

        //Start teleport sequence!
        BetterRTP.getInstance().getRTP().start(pWorld);
    }

    public static World getActualWorld(Player player,
                                       World world,
                                       @Nullable WorldLocation location) {
        if (world == null)
            world = player.getWorld();
        if (location != null)
            world = location.getWorld();
        if (BetterRTP.getInstance().getRTP().overriden.containsKey(world.getName()))
            world = Bukkit.getWorld(BetterRTP.getInstance().getRTP().overriden.get(world.getName()));
        return world;
    }

    public static World getActualWorld(Player player, World world) {
        return getActualWorld(player, world, null);
    }

    @Nullable
    public static WorldLocation getRandomLocation(CommandSender sender,
                                                  World world) {
        HashMap<String, RTPWorld> locations_permissible = CmdLocation.getLocations(sender, world);
        if (!locations_permissible.isEmpty()) {
            List<String> valuesList = new ArrayList<>(locations_permissible.keySet());
            String randomIndex = valuesList.get(new Random().nextInt(valuesList.size()));
            return (WorldLocation) locations_permissible.get(randomIndex);
        }
        return null;
    }

    public static WorldPlayer getPlayerWorld(RTPSetupInformation setup_info) {
        RtpSetupRequest setupRequest = RtpSetupRequest.from(setup_info);
        //Random Location
        if (setupRequest.location() == null
                && BetterRTP.getInstance().getSettings().isLocationEnabled()
                && BetterRTP.getInstance().getSettings().isUseLocationIfAvailable()) {
            WorldLocation worldLocation = HelperRTP.getRandomLocation(
                    setupRequest.sender(), setupRequest.world());
            if (worldLocation != null) {
                setupRequest = setupRequest.withLocation(worldLocation);
            }
            if (setupRequest.location() == null && BetterRTP.getInstance().getSettings().isDebug())
                WarningHandler.warn(WarningHandler.WARNING.USELOCATION_ENABLED_NO_LOCATION_AVAILABLE,
                        "This is not an error! UseLocationIfAvailable is set to `true`, but no location was found for "
                                + setupRequest.sender().getName() + "! Using world defaults! (Maybe they dont have permission?)");
        }

        RTPWorld configuredWorld;
        WorldPermissionGroup permissionGroup = null;
        String setupName = null;
        List<String> biomeOverride = setupRequest.biomes();

        // Location
        if (setupRequest.location() != null) {
            for (Map.Entry<String, RTPWorld> location_set : BetterRTP.getInstance().getRTP().getRTPworldLocations().entrySet()) {
                RTPWorld location = location_set.getValue();
                if (location == setupRequest.location()) {
                    setupName = location_set.getKey();
                    break;
                }
            }
            configuredWorld = setupRequest.location();
            biomeOverride = setupRequest.location().getBiomes();
        } else {
            permissionGroup = getGroup(setupRequest.player(), setupRequest.world());
            if (permissionGroup != null) {
                configuredWorld = permissionGroup;
            }
            else if (BetterRTP.getInstance().getRTP().getRTPcustomWorld().containsKey(setupRequest.world().getName())) {
                configuredWorld = BetterRTP.getInstance().getRTP().getRTPcustomWorld()
                        .get(setupRequest.world().getName());
            }
            else {
                configuredWorld = BetterRTP.getInstance().getRTP().getRTPdefaultWorld();
            }
        }

        ResolvedRtpWorld resolvedWorld = RtpWorldResolver.resolve(
                configuredWorld,
                setupRequest.world(),
                biomeOverride,
                BetterRTP.getInstance().getRTP().getRTPdefaultWorld().getMinRadius(),
                getWorldType(setupRequest.world()),
                setupName,
                permissionGroup);
        return new WorldPlayer(setupRequest, resolvedWorld);
    }

    public static WORLD_TYPE getWorldType(World world) {
        WORLD_TYPE world_type;
        RTP rtp = BetterRTP.getInstance().getRTP();
        if (rtp.world_type.containsKey(world.getName()))
            world_type = rtp.world_type.get(world.getName());
        else {
            world_type = WORLD_TYPE.NORMAL;
            rtp.world_type.put(world.getName(), world_type); //Defaults this so the error message isn't spammed
            WarningHandler.warn(WarningHandler.WARNING.NO_WORLD_TYPE_DECLARED, "Seems like the world `" + world.getName() + "` does not have a `WorldType` declared. " +
                    "Please add/fix this in the config.yml file! This world will be treated as an overworld! " +
                    "If this world is a nether world, configure it to NETHER (example: `- " + world.getName() + ": NETHER`", false);
        }
        return world_type;
    }

    public static WorldPermissionGroup getGroup(WorldPlayer pWorld) {
        return getGroup(pWorld.getPlayer(), pWorld.getWorld());
    }

    public static WorldPermissionGroup getGroup(@Nullable Player player, World world) {
        WorldPermissionGroup group = null;
        if (player != null)
            for (Map.Entry<String, PermissionGroup> permissionGroup : BetterRTP.getInstance().getRTP().getPermissionGroups().entrySet()) {
                for (Map.Entry<String, WorldPermissionGroup> worldPermission : permissionGroup.getValue().getWorlds().entrySet()) {
                    if (world.equals(worldPermission.getValue().getWorld())) {
                        if (PermissionCheck.getPermissionGroup(player, permissionGroup.getKey())) {
                            if (group != null) {
                                if (group.getPriority() < worldPermission.getValue().getPriority())
                                    continue;
                            }
                            group = worldPermission.getValue();
                        }
                    }
                }
            }
        return group;
    }
}
