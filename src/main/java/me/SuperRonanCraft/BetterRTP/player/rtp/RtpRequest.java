package me.SuperRonanCraft.BetterRTP.player.rtp;

import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

/** Immutable input and configuration snapshot for one RTP attempt. */
public record RtpRequest(
        Player player,
        UUID playerId,
        CommandSender sender,
        RTP_TYPE type,
        RtpWorldSnapshot world,
        RtpPlayerOptions options) {

    public RtpRequest {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(options, "options");
    }

    public static RtpRequest from(WorldPlayer source) {
        Objects.requireNonNull(source, "source");
        Player player = Objects.requireNonNull(source.getPlayer(), "source.player");
        return new RtpRequest(
                player,
                player.getUniqueId(),
                source.getSendi(),
                source.getRtp_type(),
                RtpWorldSnapshot.from(source),
                RtpPlayerOptions.from(source.getPlayerInfo()));
    }
}
