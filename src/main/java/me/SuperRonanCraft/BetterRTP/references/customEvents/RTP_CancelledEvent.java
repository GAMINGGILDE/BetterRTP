package me.SuperRonanCraft.BetterRTP.references.customEvents;

import org.bukkit.entity.Player;

public class RTP_CancelledEvent extends RTPEvent { //Called when a delayed rtp is cancelled cause player moved

    Player p;
    public RTP_CancelledEvent(Player p) {
        this.p = p;
    }

    public Player getPlayer() {
        return p;
    }
}
