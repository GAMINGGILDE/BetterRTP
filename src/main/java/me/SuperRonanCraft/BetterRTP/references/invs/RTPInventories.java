package me.SuperRonanCraft.BetterRTP.references.invs;

import me.SuperRonanCraft.BetterRTP.references.invs.enums.RTPInventory_Defaults;
import me.SuperRonanCraft.BetterRTP.BetterRTP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class RTPInventories {

    private final HashMap<RTP_INV_SETTINGS, RTPInventory_Defaults> invs = new HashMap<>();

    public void load() {
        invs.clear();
        for (RTP_INV_SETTINGS type : RTP_INV_SETTINGS.values()) {
            type.load(type);
            invs.put(type, type.getInv());
        }
    }

    public void closeAll() {
        AsyncHandler.global(() -> closeAllOnEntitySchedulers());
    }

    public CompletableFuture<Void> closeAllOnEntitySchedulers() {
        BetterRTP main = BetterRTP.getInstance();
        CompletableFuture<?>[] closures = Bukkit.getOnlinePlayers().stream()
                .map(player -> AsyncHandler.entityFuture(
                        player,
                        () -> {
                    if (main.getPInfo().playerExists(player)) {
                        player.closeInventory();
                    }
                        },
                        () -> { }))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(closures)
                .whenComplete((ignored, throwable) -> main.getPInfo().clearInvs());
    }

    public RTPInventory_Defaults getInv(RTP_INV_SETTINGS type) {
        return invs.get(type);
    }
}
