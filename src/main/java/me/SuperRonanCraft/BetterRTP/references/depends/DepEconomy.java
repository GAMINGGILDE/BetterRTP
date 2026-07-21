package me.SuperRonanCraft.BetterRTP.references.depends;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.references.PermissionNode;
import me.SuperRonanCraft.BetterRTP.references.file.FileOther;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldPlayer;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class DepEconomy {

    private volatile Economy economy;
    private volatile int hungerCost;

    public Reservation reserve(WorldPlayer worldPlayer) {
        Player player = worldPlayer.getPlayer();
        int reservedHunger = applicableHungerCost(worldPlayer);
        if (player.getFoodLevel() < reservedHunger) {
            return Reservation.failed(Failure.HUNGER);
        }

        Economy provider = economy;
        double price = applicablePrice(worldPlayer, provider);
        if (price <= 0.0D) {
            return Reservation.success(player, null, 0.0D, reservedHunger);
        }

        try {
            EconomyResponse response = provider.withdrawPlayer(player, price);
            if (!response.transactionSuccess()) {
                return Reservation.failed(Failure.ECONOMY);
            }
            return Reservation.success(player, provider, price, reservedHunger);
        } catch (RuntimeException exception) {
            BetterRTP.getInstance().getLogger().log(
                    Level.WARNING, "Unable to reserve the RTP price for " + player.getName(), exception);
            return Reservation.failed(Failure.ECONOMY);
        }
    }

    public boolean hasBalance(WorldPlayer worldPlayer) {
        Economy provider = economy;
        double price = applicablePrice(worldPlayer, provider);
        if (price <= 0.0D) {
            return true;
        }
        try {
            return provider.getBalance(worldPlayer.getPlayer()) >= price;
        } catch (RuntimeException exception) {
            BetterRTP.getInstance().getLogger().log(Level.WARNING,
                    "Unable to check the RTP balance for " + worldPlayer.getPlayer().getName(), exception);
            return false;
        }
    }

    public boolean hasHunger(WorldPlayer worldPlayer) {
        return worldPlayer.getPlayer().getFoodLevel() >= applicableHungerCost(worldPlayer);
    }

    public void load() {
        FileOther.FILETYPE config = FileOther.FILETYPE.ECO;
        hungerCost = config.getBoolean("Hunger.Enabled")
                ? Math.max(0, config.getInt("Hunger.Honches"))
                : 0;
        economy = findEconomyProvider(config.getBoolean("Economy.Enabled"));
    }

    private Economy findEconomyProvider(boolean enabled) {
        if (!enabled || !BetterRTP.getInstance().getServer().getPluginManager().isPluginEnabled("Vault")) {
            return null;
        }
        RegisteredServiceProvider<Economy> registration = BetterRTP.getInstance()
                .getServer().getServicesManager().getRegistration(Economy.class);
        return registration == null ? null : registration.getProvider();
    }

    private double applicablePrice(WorldPlayer worldPlayer, Economy provider) {
        if (provider == null
                || !worldPlayer.getPlayerInfo().isTakeMoney()
                || PermissionNode.BYPASS_ECONOMY.check(worldPlayer.getPlayer())) {
            return 0.0D;
        }
        return Math.max(0, worldPlayer.getPrice());
    }

    private int applicableHungerCost(WorldPlayer worldPlayer) {
        Player player = worldPlayer.getPlayer();
        if (!worldPlayer.getPlayerInfo().isTakeHunger()
                || PermissionNode.BYPASS_HUNGER.check(player)
                || (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)) {
            return 0;
        }
        return hungerCost;
    }

    public enum Failure {
        NONE,
        ECONOMY,
        HUNGER
    }

    public static final class Reservation {
        private final Player player;
        private final Economy economy;
        private final double price;
        private final int hunger;
        private final Failure failure;
        private final AtomicBoolean refunded = new AtomicBoolean();

        private Reservation(Player player, Economy economy, double price, int hunger, Failure failure) {
            this.player = player;
            this.economy = economy;
            this.price = price;
            this.hunger = hunger;
            this.failure = failure;
        }

        private static Reservation success(Player player, Economy economy, double price, int hunger) {
            return new Reservation(player, economy, price, hunger, Failure.NONE);
        }

        private static Reservation failed(Failure failure) {
            return new Reservation(null, null, 0.0D, 0, failure);
        }

        public boolean successful() {
            return failure == Failure.NONE;
        }

        public Failure failure() {
            return failure;
        }

        public void commitHunger() {
            if (!successful() || hunger <= 0) {
                return;
            }
            player.setFoodLevel(Math.max(0, player.getFoodLevel() - hunger));
        }

        public void rollback() {
            if (!successful() || economy == null || price <= 0.0D || !refunded.compareAndSet(false, true)) {
                return;
            }
            try {
                EconomyResponse response = economy.depositPlayer(player, price);
                if (!response.transactionSuccess()) {
                    BetterRTP.getInstance().getLogger().warning(
                            "Unable to refund " + price + " after a failed RTP for " + player.getName()
                                    + ": " + response.errorMessage);
                }
            } catch (RuntimeException exception) {
                BetterRTP.getInstance().getLogger().log(Level.SEVERE,
                        "Unable to refund " + price + " after a failed RTP for " + player.getName(), exception);
            }
        }
    }
}
