package me.SuperRonanCraft.BetterRTP.player.events;

import me.SuperRonanCraft.BetterRTP.references.invs.RTPInventories;
import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.references.player.HelperPlayer;
import me.SuperRonanCraft.BetterRTP.references.player.playerdata.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Objects;

public class Click {

    static void click(InventoryClickEvent e) {
        if (!validClick(e))
            return;
        e.setCancelled(true);
        handler(e);
    }

    static private void handler(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        PlayerData data = HelperPlayer.getData(player);
        RTPInventories menus = BetterRTP.getInstance().getInvs();
        if (data.getMenu().getInvType() == null
                || menus.getInv(data.getMenu().getInvType()) == null) {
            BetterRTP.getInstance().getLogger().warning(
                    "Ignoring an inventory click with missing BetterRTP menu state for "
                            + player.getName());
            return;
        }
        menus.getInv(data.getMenu().getInvType()).clickEvent(e);
    }

    static private boolean validClick(InventoryClickEvent e) {
        //Not a player, or Not our inventory
        if (!(e.getWhoClicked() instanceof Player) || e.isCancelled())
            return false;
            // Item is clicked
        else if (e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR))
            return false;
        else if (e.getWhoClicked() instanceof Player player) {
            // Clicks the inventory
            PlayerData data = HelperPlayer.getData(player);
            Inventory menuInventory = data.getMenu().getInv();
            if (!Objects.equals(e.getInventory(), menuInventory))
                return false;
                // Clicks their own inventory
            else if (!Objects.equals(e.getClickedInventory(), menuInventory)) {
                e.setCancelled(true);
                return false;
            }
        }
        return true;
    }
}
