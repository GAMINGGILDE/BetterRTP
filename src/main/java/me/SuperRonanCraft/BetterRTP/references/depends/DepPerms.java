package me.SuperRonanCraft.BetterRTP.references.depends;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;

public class DepPerms {
    public Permission p = null;

    public boolean hasPerm(String perm, CommandSender sendi) {
        //sendi.sendMessage(perm);
        if (p != null)
            return p.has(sendi, perm);
        return sendi.hasPermission(perm);
    }

    public void register() {
        p = null;
        if (BetterRTP.getInstance().getServer().getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Permission> permissionProvider = BetterRTP.getInstance().getServer()
                    .getServicesManager().getRegistration(Permission.class);
            if (permissionProvider != null) {
                p = permissionProvider.getProvider();
            }
        }
    }
}
