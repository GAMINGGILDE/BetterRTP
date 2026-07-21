package me.SuperRonanCraft.BetterRTP.player.rtp;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.player.rtp.effects.RTPEffect_Titles;
import me.SuperRonanCraft.BetterRTP.player.rtp.effects.RTPEffects;
import me.SuperRonanCraft.BetterRTP.references.PermissionNode;
import me.SuperRonanCraft.BetterRTP.references.customEvents.RTP_TeleportEvent;
import me.SuperRonanCraft.BetterRTP.references.customEvents.RTP_TeleportPostEvent;
import me.SuperRonanCraft.BetterRTP.references.customEvents.RTP_TeleportPreEvent;
import me.SuperRonanCraft.BetterRTP.references.messages.MessagesCore;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldPlayer;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;

public class RTPTeleport {

    private final RTPEffects effects = new RTPEffects();

    void load() {
        effects.load();
    }

    void sendPlayer(final CommandSender sendi, final RTPPlayer session, final Location location) {
        Player p = session.getPlayer();
        WorldPlayer wPlayer = session.getWorldPlayer();
        int attempts = session.getAttempts();
        RTP_TYPE type = session.getType();
        Location oldLoc = p.getLocation();
        loadingTeleport(p, sendi); //Send loading message to player who requested
        try {
            RTP_TeleportEvent event = new RTP_TeleportEvent(p, location, wPlayer.getWorldtype());
            getPl().getServer().getPluginManager().callEvent(event);
            Location loc = event.getLocation();
            CompletableFuture<Boolean> teleport = session.track(AsyncHandler.teleportAsync(p, loc)
                    .orTimeout(getPl().getSettings().getTeleportTimeoutSeconds(), TimeUnit.SECONDS));
            teleport.whenComplete((success, throwable) -> {
                if (throwable != null) {
                    getPl().getLogger().log(Level.WARNING,
                            "Unable to teleport " + p.getName() + " asynchronously", throwable);
                    finishFailedTeleport(p, session);
                    return;
                }
                if (!Boolean.TRUE.equals(success)) {
                    getPl().getLogger().warning("Asynchronous teleport failed for " + p.getName());
                    finishFailedTeleport(p, session);
                    return;
                }
                AsyncHandler.syncAtEntity(
                        p,
                        () -> {
                            try {
                                afterTeleport(p, loc, wPlayer, attempts, oldLoc, type);
                                notifyRequester(sendi, p, loc, wPlayer, attempts);
                                if (type == RTP_TYPE.JOIN
                                        && BetterRTP.getInstance().getSettings().isRtpOnFirstJoin_SetAsRespawn()) {
                                    p.setRespawnLocation(loc, true);
                                }
                            } finally {
                                session.completeSuccessfully();
                            }
                        },
                        () -> AsyncHandler.sync(session::finish));
            });
        } catch (Exception e) {
            session.finish();
            getPl().getLogger().log(Level.WARNING, "Unable to start teleport for " + p.getName(), e);
        }
    }

    private void finishFailedTeleport(Player player, RTPPlayer session) {
        AsyncHandler.syncAtEntity(
                player,
                session::finish,
                () -> AsyncHandler.sync(session::finish));
    }

    private void notifyRequester(CommandSender sender, Player teleportedPlayer, Location location,
                                 WorldPlayer worldPlayer, int attempts) {
        if (sender == teleportedPlayer) {
            return;
        }
        Runnable notification = () -> sendSuccessMsg(
                sender, teleportedPlayer.getName(), location, worldPlayer, false, attempts);
        if (sender instanceof Player requestingPlayer) {
            AsyncHandler.syncAtEntity(requestingPlayer, notification);
        } else {
            AsyncHandler.sync(notification);
        }
    }

    //Effects

    public void afterTeleport(Player p, Location loc, WorldPlayer wPlayer, int attempts, Location oldLoc, RTP_TYPE type) {
        //Only a successful rtp should run this OR '/rtp test'
        effects.getSounds().playTeleport(p);
        effects.getParticles().display(p);
        effects.getPotions().giveEffects(p);
        effects.getTitles().showTitle(RTPEffect_Titles.RTP_TITLE_TYPE.TELEPORT, p, loc, attempts, 0);
        if (effects.getTitles().sendMsg(RTPEffect_Titles.RTP_TITLE_TYPE.TELEPORT))
            sendSuccessMsg(p, p.getName(), loc, wPlayer, true, attempts);
        getPl().getServer().getPluginManager().callEvent(new RTP_TeleportPostEvent(p, loc, oldLoc, wPlayer, type));
    }

    public boolean beforeTeleportInstant(CommandSender sendi, Player p) {
        RTP_TeleportPreEvent event = new RTP_TeleportPreEvent(p);
        getPl().getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            effects.getSounds().playDelay(p);
            effects.getTitles().showTitle(RTPEffect_Titles.RTP_TITLE_TYPE.NODELAY, p, p.getLocation(), 0, 0);
            if (effects.getTitles().sendMsg(RTPEffect_Titles.RTP_TITLE_TYPE.NODELAY))
                MessagesCore.SUCCESS_TELEPORT.send(sendi);
        }
        return event.isCancelled();
    }

    public boolean beforeTeleportDelay(Player p, int delay) { //Only Delays should call this
        RTP_TeleportPreEvent event = new RTP_TeleportPreEvent(p);
        getPl().getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            effects.getSounds().playDelay(p);
            effects.getTitles().showTitle(RTPEffect_Titles.RTP_TITLE_TYPE.DELAY, p, p.getLocation(), 0, delay);
            if (effects.getTitles().sendMsg(RTPEffect_Titles.RTP_TITLE_TYPE.DELAY))
                MessagesCore.DELAY.send(p, delay);
        }
        return event.isCancelled();
    }

    public void cancelledTeleport(Player p) { //Only Delays should call this
        effects.getTitles().showTitle(RTPEffect_Titles.RTP_TITLE_TYPE.CANCEL, p, p.getLocation(), 0, 0);
        if (effects.getTitles().sendMsg(RTPEffect_Titles.RTP_TITLE_TYPE.CANCEL))
            MessagesCore.MOVED.send(p);
    }

    private void loadingTeleport(Player p, CommandSender sendi) {
        effects.getTitles().showTitle(RTPEffect_Titles.RTP_TITLE_TYPE.LOADING, p, p.getLocation(), 0, 0);
        if (effects.getTitles().sendMsg(RTPEffect_Titles.RTP_TITLE_TYPE.LOADING) && sendStatusMessage()) { //Show msg if enabled or if not same player
            if (p == sendi)
                MessagesCore.SUCCESS_LOADING.send(sendi);
            MessagesCore.SUCCESS_LOADING.send(p);
        }
    }

    public void failedTeleport(Player p, CommandSender sendi) {
        effects.getTitles().showTitle(RTPEffect_Titles.RTP_TITLE_TYPE.FAILED, p, p.getLocation(), 0, 0);
        if (effects.getTitles().sendMsg(RTPEffect_Titles.RTP_TITLE_TYPE.FAILED))
            if (p == sendi)
                MessagesCore.FAILED_NOTSAFE.send(p, BetterRTP.getInstance().getRTP().maxAttempts);
            else
                MessagesCore.OTHER_NOTSAFE.send(sendi, Arrays.asList(
                        BetterRTP.getInstance().getRTP().maxAttempts,
                        p.getName()));
    }

    private void sendSuccessMsg(CommandSender sendi, String player, Location loc, WorldPlayer wPlayer, boolean sameAsPlayer, int attempts) {
        if (sameAsPlayer) {
            if (wPlayer.getPrice() == 0 || PermissionNode.BYPASS_ECONOMY.check(sendi))
                MessagesCore.SUCCESS_BYPASS.send(sendi, Arrays.asList(loc, attempts));
            else
                MessagesCore.SUCCESS_PAID.send(sendi, Arrays.asList(loc, wPlayer, attempts));
        } else
            MessagesCore.OTHER_SUCCESS.send(sendi, Arrays.asList(loc, player, attempts));
    }

    private boolean sendStatusMessage() {
        return getPl().getSettings().isStatusMessages();
    }

    private BetterRTP getPl() {
        return BetterRTP.getInstance();
    }
}
