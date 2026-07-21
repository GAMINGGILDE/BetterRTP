package me.SuperRonanCraft.BetterRTP.player.rtp;

import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldLocation;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/** Immutable input used while resolving the configured world for an RTP. */
public record RtpSetupRequest(
        World world,
        CommandSender sender,
        @Nullable Player player,
        boolean personalized,
        @Nullable List<String> biomes,
        @Nullable WorldLocation location,
        @Nullable RTP_TYPE type,
        RtpPlayerOptions playerOptions) {

    public RtpSetupRequest {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(playerOptions, "playerOptions");
        biomes = biomes == null ? null : List.copyOf(biomes);
    }

    public static RtpSetupRequest from(RTPSetupInformation source) {
        Objects.requireNonNull(source, "source");
        return new RtpSetupRequest(
                source.getWorld(),
                source.getSender(),
                source.getPlayer(),
                source.isPersonalized(),
                source.getBiomes(),
                source.getLocation(),
                source.getRtp_type(),
                RtpPlayerOptions.from(source.getPlayerInfo()));
    }

    public RtpSetupRequest withLocation(WorldLocation selectedLocation) {
        Objects.requireNonNull(selectedLocation, "selectedLocation");
        return new RtpSetupRequest(
                selectedLocation.getWorld(), sender, player, personalized,
                biomes, selectedLocation, type, playerOptions);
    }
}
