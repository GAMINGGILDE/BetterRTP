package me.SuperRonanCraft.BetterRTP.references.customEvents;

import me.SuperRonanCraft.BetterRTP.player.rtp.RTPPlayer;
import me.SuperRonanCraft.BetterRTP.player.rtp.RtpRequest;
import me.SuperRonanCraft.BetterRTP.player.rtp.RtpWorldSnapshot;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.RTPWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.Nullable;

//Called when an rtp is finding a valid location
public class RTP_FindLocationEvent extends RTPEvent implements Cancellable {

    Player p;
    RTPWorld world;
    Location loc; //Used to force a location into find event
    int attempts;
    boolean cancelled;
    RtpRequest request;

    public RTP_FindLocationEvent(RTPPlayer rtpPlayer) {
        this.p = rtpPlayer.getPlayer();
        this.world = rtpPlayer.getWorldPlayer();
        this.attempts = rtpPlayer.getAttempts();
        this.request = rtpPlayer.getRequest();
    }

    //A location can be pushed in if a developer wants to inject a custom location
    //Safe block code will still be run!
    public void setLocation(Location loc) {
        this.loc = loc;
    }

    @Nullable
    public Location getLocation() {
        return loc;
    }

    public RTPWorld getWorld() {
        return world;
    }

    public Player getPlayer() {
        return p;
    }

    public RtpRequest getRequest() {
        return request;
    }

    public RtpWorldSnapshot getWorldSnapshot() {
        return request.world();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
