package me.SuperRonanCraft.BetterRTP.player.rtp;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.SuperRonanCraft.BetterRTP.references.customEvents.RTP_CancelledEvent;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.concurrent.atomic.AtomicBoolean;

class RTPDelay implements Listener {
    private ScheduledTask task;
    private final boolean cancelOnMove, cancelOnDamage;
    private final RTPPlayer rtp;
    private final AtomicBoolean cancelled = new AtomicBoolean();

    RTPDelay(CommandSender sendi, RTPPlayer rtp, int delay, boolean cancelOnMove, boolean cancelOnDamage) {
        this.cancelOnMove = cancelOnMove;
        this.cancelOnDamage = cancelOnDamage;
        this.rtp = rtp;
        delay(sendi, delay);
    }

    private void delay(CommandSender sendi, int delay) {
        if (!rtp.runtime().getTeleport().beforeTeleportDelay(rtp.getPlayer(), delay)) {
            task = AsyncHandler.syncLaterAtEntity(
                    rtp.getPlayer(),
                    run(sendi, this),
                    () -> {
                        HandlerList.unregisterAll(this);
                        rtp.cancel();
                    },
                    delay * 20L);
            rtp.track(task);
            if (cancelOnMove || cancelOnDamage)
                Bukkit.getPluginManager().registerEvents(this, rtp.runtime().eventOwner());
        } else {
            rtp.cancel();
        }
    }

    @EventHandler
    void event(PlayerMoveEvent e) {
        if (cancelOnMove)
            if (e.getPlayer().equals(rtp.getPlayer()) &&
                (e.getTo() != null &&
                        (e.getTo().getBlockX() != e.getFrom().getBlockX() ||
                        e.getTo().getBlockY() != e.getFrom().getBlockY() ||
                        e.getTo().getBlockZ() != e.getFrom().getBlockZ()))
            ) {
                cancel();
            }
    }

    @EventHandler
    void event(EntityDamageEvent e) {
        if (cancelOnDamage)
            if (e.getEntity() instanceof Player){
                if (e.getEntity().equals(rtp.getPlayer()))
                    cancel();
            }
    }

    private void cancel() {
        if (!cancelled.compareAndSet(false, true)) {
            return;
        }
        if (task != null)
            task.cancel();
        HandlerList.unregisterAll(this);
        rtp.runtime().getTeleport().cancelledTeleport(rtp.getPlayer());
        rtp.cancel();
        Bukkit.getServer().getPluginManager().callEvent(new RTP_CancelledEvent(rtp.getPlayer()));
    }

    private Runnable run(final CommandSender sendi, final RTPDelay cls) {
        return () -> {
            HandlerList.unregisterAll(cls);
            if (rtp.isActive())
                rtp.randomlyTeleport(sendi);
        };
    }
}
