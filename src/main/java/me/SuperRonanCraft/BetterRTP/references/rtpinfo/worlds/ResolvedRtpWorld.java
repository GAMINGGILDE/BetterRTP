package me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds;

import me.SuperRonanCraft.BetterRTP.player.commands.RTP_SETUP_TYPE;
import me.SuperRonanCraft.BetterRTP.player.rtp.RtpWorldSnapshot;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/** Result of selecting and normalizing the world settings for one request. */
public record ResolvedRtpWorld(
        RtpWorldSnapshot settings,
        RTP_SETUP_TYPE setupType,
        @Nullable String setupName,
        @Nullable WorldPermissionGroup permissionGroup) {

    public ResolvedRtpWorld {
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(setupType, "setupType");
    }
}
