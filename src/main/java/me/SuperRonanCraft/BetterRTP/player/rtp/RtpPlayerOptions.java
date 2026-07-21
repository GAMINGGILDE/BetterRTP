package me.SuperRonanCraft.BetterRTP.player.rtp;

import java.util.Objects;

/** Immutable player-specific switches for one RTP request. */
public record RtpPlayerOptions(
        boolean applyDelay,
        boolean applyCooldown,
        boolean checkCooldown,
        boolean takeMoney,
        boolean takeHunger) {

    public static RtpPlayerOptions from(RTP_PlayerInfo source) {
        Objects.requireNonNull(source, "source");
        return new RtpPlayerOptions(
                source.isApplyDelay(),
                source.isApplyCooldown(),
                source.isCheckCooldown(),
                source.isTakeMoney(),
                source.isTakeHunger());
    }

    public RTP_PlayerInfo toLegacyPlayerInfo() {
        return new RTP_PlayerInfo(
                applyDelay, applyCooldown, checkCooldown, takeMoney, takeHunger);
    }
}
