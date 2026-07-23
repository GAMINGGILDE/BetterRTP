package me.SuperRonanCraft.BetterRTP.references.helpers;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.player.commands.types.CmdEdit;
import me.SuperRonanCraft.BetterRTP.references.file.FileOther;
import me.SuperRonanCraft.BetterRTP.references.messages.Message_RTP;
import me.SuperRonanCraft.BetterRTP.references.messages.MessagesCore;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WORLD_TYPE;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HelperRTP_EditWorlds {

    public static void editCustomWorld(CommandSender sendi, CmdEdit.RTP_CMD_EDIT_SUB cmd, String world, String val) {
        String path = "CustomWorlds";
        if (editSingleMap(sendi, cmd, world, val, path, FileOther.FILETYPE.CONFIG))
            BetterRTP.getInstance().getRTP().loadWorlds();
    }

    public static void editLocation(CommandSender sendi, CmdEdit.RTP_CMD_EDIT_SUB cmd, String location, String val) {
        String path = "Locations";
        if (editSingleMap(sendi, cmd, location, val, path, FileOther.FILETYPE.LOCATIONS))
            BetterRTP.getInstance().getRTP().loadLocations();
    }

    private static boolean editSingleMap(CommandSender sendi, CmdEdit.RTP_CMD_EDIT_SUB cmd, String field, String val, String path, FileOther.FILETYPE file) {
        Object value;
        try {
            value = cmd.getResult(val);
        } catch (RuntimeException exception) {
            BetterRTP.getInstance().getLogger().log(
                    java.util.logging.Level.WARNING,
                    "Unable to parse edited default-world value '" + val + "'", exception);
            MessagesCore.EDIT_ERROR.send(sendi);
            return false;
        }

        if (value == null) {
            MessagesCore.EDIT_ERROR.send(sendi);
            return false;
        }
        file.setValue(path + "." + field + "." + cmd.get(), value);
        file.save();
        Message_RTP.sms(sendi,
                MessagesCore.EDIT_SET.get(sendi, null)
                        .replace("%type%", cmd.get())
                        .replace("%value%", val));
        return true;
    }

    public static void editPermissionGroup(CommandSender sendi, CmdEdit.RTP_CMD_EDIT_SUB cmd, String group, String world, String val) {
        Object value;
        try {
            value = cmd.getResult(val);
        } catch (RuntimeException exception) {
            BetterRTP.getInstance().getLogger().log(
                    java.util.logging.Level.WARNING,
                    "Unable to parse edited custom-world value '" + val + "'", exception);
            MessagesCore.EDIT_ERROR.send(sendi);
            return;
        }

        if (value == null) {
            MessagesCore.EDIT_ERROR.send(sendi);
            return;
        }

        FileOther.FILETYPE file = FileOther.FILETYPE.CONFIG;
        file.setValue("PermissionGroup.Groups." + group + "." + world + "." + cmd.get(), value);
        file.save();
        Message_RTP.sms(sendi,
                MessagesCore.EDIT_SET.get(sendi, null)
                        .replace("%type%", cmd.get())
                        .replace("%value%", val));
        BetterRTP.getInstance().getRTP().loadPermissionGroups();
    }

    public static void editDefault(CommandSender sendi, CmdEdit.RTP_CMD_EDIT_SUB cmd, String val) {
        Object value;
        try {
            value = cmd.getResult(val);
        } catch (RuntimeException exception) {
            BetterRTP.getInstance().getLogger().log(
                    java.util.logging.Level.WARNING,
                    "Unable to parse edited location value '" + val + "'", exception);
            MessagesCore.EDIT_ERROR.send(sendi);
            return;
        }

        if (value == null) {
            MessagesCore.EDIT_ERROR.send(sendi);
            return;
        }

        FileOther.FILETYPE file = FileOther.FILETYPE.CONFIG;
        YamlConfiguration config = file.getConfig();

        config.set("Default." + cmd.get(), value);

        file.save();
        BetterRTP.getInstance().getRTP().loadWorlds();
        Message_RTP.sms(sendi,
                MessagesCore.EDIT_SET.get(sendi, null)
                        .replace("%type%", cmd.get())
                        .replace("%value%", val));
    }

    public static void editWorldtype(CommandSender sendi, String world, String val) {
        //sendi.sendMessage("Editting worldtype for world " + world + " to " + val);
        WORLD_TYPE type;
        try {
            type = WORLD_TYPE.valueOf(val.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            MessagesCore.EDIT_ERROR.send(sendi);
            return;
        }

        FileOther.FILETYPE file = FileOther.FILETYPE.CONFIG;
        file.setValue("WorldType." + world, type.name());
        file.save();
        BetterRTP.getInstance().getRTP().load();
        Message_RTP.sms(sendi,
                MessagesCore.EDIT_SET.get(sendi, null)
                        .replace("%type%", CmdEdit.RTP_CMD_EDIT.WORLD_TYPE.name())
                        .replace("%value%", val));
    }

    public static void editOverride(CommandSender sendi, String world, String val) {

        FileOther.FILETYPE file = FileOther.FILETYPE.CONFIG;
        if (!val.equals("REMOVE_OVERRIDE")) {
            file.setValue("Overrides." + world, val);
        } else {
            file.setValue("Overrides." + world, null);
            val = "(removed override)";
        }
        file.save();
        BetterRTP.getInstance().getRTP().load();
        Message_RTP.sms(sendi,
                MessagesCore.EDIT_SET.get(sendi, null)
                        .replace("%type%", CmdEdit.RTP_CMD_EDIT.OVERRIDE.name())
                        .replace("%value%", val));
    }

    public static void editBlacklisted(CommandSender sendi, String block, boolean add) {

        Material material = Material.matchMaterial(block);
        if (material == null || !material.isBlock()) {
            MessagesCore.EDIT_ERROR.send(sendi);
            return;
        }
        block = material.name();

        FileOther.FILETYPE file = FileOther.FILETYPE.CONFIG;
        YamlConfiguration config = file.getConfig();

        List<String> world_map = config.getStringList("BlacklistedBlocks");
        List<String> removeList = new ArrayList<>();
        for (String m : world_map) {
            if (m.equalsIgnoreCase(block)) {
                removeList.add(m);
            }
        }
        for (String o : removeList)
            world_map.remove(o);
        if (add) {
            world_map.add(block);
        } else {
            block = "(removed " + block + ")";
        }
        config.set("BlacklistedBlocks", world_map);

        file.save();
        BetterRTP.getInstance().getRTP().load();
        Message_RTP.sms(sendi,
                MessagesCore.EDIT_SET.get(sendi, null)
                        .replace("%type%", CmdEdit.RTP_CMD_EDIT.BLACKLISTEDBLOCKS.name())
                        .replace("%value%", block));
    }

}
