package me.SuperRonanCraft.BetterRTP.references.helpers;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.player.commands.types.CmdEdit;
import me.SuperRonanCraft.BetterRTP.references.file.FileOther;
import me.SuperRonanCraft.BetterRTP.references.messages.Message_RTP;
import me.SuperRonanCraft.BetterRTP.references.messages.MessagesCore;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WORLD_TYPE;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        } catch (Exception e) {
            e.printStackTrace();
            MessagesCore.EDIT_ERROR.send(sendi);
            return false;
        }

        if (value == null) {
            MessagesCore.EDIT_ERROR.send(sendi);
            return false;
        }
        YamlConfiguration config = file.getConfig();

        List<Map<String, Object>> map = mutableMapList(config.getMapList(path));
        boolean found = false;
        for (Map<String, Object> entry : map) {
            if (entry.containsKey(field)) {
                found = true;
                Object existingValues = entry.get(field);
                Map<String, Object> values = existingValues instanceof Map<?, ?> rawValues
                        ? mutableStringMap(rawValues)
                        : new LinkedHashMap<>();
                values.put(cmd.get(), value);
                entry.put(field, values);
                Message_RTP.sms(sendi,
                        MessagesCore.EDIT_SET.get(sendi, null)
                                .replace("%type%", cmd.get())
                                .replace("%value%", val));
                break;
            }
        }
        if (!found) {
            Map<String, Object> map2 = new HashMap<>();
            Map<String, Object> values = new HashMap<>();
            values.put(cmd.get(), value);
            map2.put(field, values);
            map.add(map2);
        }

        config.set(path, map);

        saveAsync(file, config);
        return true;
    }

    public static void editPermissionGroup(CommandSender sendi, CmdEdit.RTP_CMD_EDIT_SUB cmd, String group, String world, String val) {
        Object value;
        try {
            value = cmd.getResult(val);
        } catch (Exception e) {
            e.printStackTrace();
            MessagesCore.EDIT_ERROR.send(sendi);
            return;
        }

        if (value == null) {
            MessagesCore.EDIT_ERROR.send(sendi);
            return;
        }

        String path = "PermissionGroup.Groups";
        FileOther.FILETYPE file = FileOther.FILETYPE.CONFIG;
        YamlConfiguration config = file.getConfig();

        List<Map<String, Object>> map = mutableMapList(config.getMapList(path));
        for (Map<String, Object> m : map)
            for (Map.Entry<String, Object> entry : m.entrySet()) {
                String _group = entry.getKey();
                if (_group.equals(group)) {
                    BetterRTP.getInstance().getLogger().info("Group: " + group);
                    Object _value = entry.getValue();
                    if (!(_value instanceof List<?> worldLists)) {
                        continue;
                    }
                    List<Object> mutableWorldLists = new ArrayList<>(worldLists);
                    for (int i = 0; i < mutableWorldLists.size(); i++) {
                        Object worldList = mutableWorldLists.get(i);
                        BetterRTP.getInstance().getLogger().info("World: " + worldList.toString());
                        if (!(worldList instanceof Map<?, ?> rawWorlds)) {
                            continue;
                        }
                        Map<String, Object> worlds = mutableStringMap(rawWorlds);
                        for (Map.Entry<String, Object> worldFields : worlds.entrySet()) {
                            BetterRTP.getInstance().getLogger().info("Hash: " + worldFields);
                            if (world.equals(worldFields.getKey())) {
                                if (!(worldFields.getValue() instanceof Map<?, ?> rawValues)) {
                                    continue;
                                }
                                Map<String, Object> values = mutableStringMap(rawValues);
                                values.put(cmd.get(), value);
                                worldFields.setValue(values);
                                Message_RTP.sms(sendi,
                                        MessagesCore.EDIT_SET.get(sendi, null)
                                                .replace("%type%", cmd.get())
                                                .replace("%value%", val));
                            }
                        }
                        mutableWorldLists.set(i, worlds);
                    }
                    entry.setValue(mutableWorldLists);
                }
            }
        /*if (!found) {
            Map<Object, Object> map2 = new HashMap<>();
            Map<Object, Object> values = new HashMap<>();
            values.put(cmd.get(), value);
            map2.put(world, values);
            map.add(map2);
        }*/
        //if (!found)
        //    return;

        config.set(path, map);

        saveAsync(file, config);
        BetterRTP.getInstance().getRTP().loadPermissionGroups();
    }

    public static void editDefault(CommandSender sendi, CmdEdit.RTP_CMD_EDIT_SUB cmd, String val) {
        Object value;
        try {
            value = cmd.getResult(val);
        } catch (Exception e) {
            e.printStackTrace();
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

        saveAsync(file, config);
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
            type = WORLD_TYPE.valueOf(val.toUpperCase());
        } catch (Exception e) {
            //e.printStackTrace();
            MessagesCore.EDIT_ERROR.send(sendi);
            return;
        }

        FileOther.FILETYPE file = FileOther.FILETYPE.CONFIG;
        YamlConfiguration config = file.getConfig();

        List<Map<?, ?>> world_map = config.getMapList("WorldType");
        List<Map<?, ?>> removeList = new ArrayList<>();
        for (Map<?, ?> m : world_map) {
            for (Map.Entry<?, ?> entry : m.entrySet()) {
                if (entry.getKey().equals(world))
                    removeList.add(m);
            }
        }
        for (Map<?, ?> o : removeList)
            world_map.remove(o);
        Map<String, String> newIndex = new HashMap<>();
        newIndex.put(world, type.name());
        world_map.add(newIndex);
        config.set("WorldType", world_map);

        saveAsync(file, config);
        BetterRTP.getInstance().getRTP().load();
        Message_RTP.sms(sendi,
                MessagesCore.EDIT_SET.get(sendi, null)
                        .replace("%type%", CmdEdit.RTP_CMD_EDIT.WORLD_TYPE.name())
                        .replace("%value%", val));
    }

    public static void editOverride(CommandSender sendi, String world, String val) {

        FileOther.FILETYPE file = FileOther.FILETYPE.CONFIG;
        YamlConfiguration config = file.getConfig();

        List<Map<?, ?>> world_map = config.getMapList("Overrides");
        List<Map<?, ?>> removeList = new ArrayList<>();
        for (Map<?, ?> m : world_map) {
            for (Map.Entry<?, ?> entry : m.entrySet()) {
                if (entry.getKey().equals(world))
                    removeList.add(m);
            }
        }
        for (Map<?, ?> o : removeList)
            world_map.remove(o);
        if (!val.equals("REMOVE_OVERRIDE")) {
            Map<String, String> newIndex = new HashMap<>();
            newIndex.put(world, val);
            world_map.add(newIndex);
        } else {
            val = "(removed override)";
        }
        config.set("Overrides", world_map);

        saveAsync(file, config);
        BetterRTP.getInstance().getRTP().load();
        Message_RTP.sms(sendi,
                MessagesCore.EDIT_SET.get(sendi, null)
                        .replace("%type%", CmdEdit.RTP_CMD_EDIT.OVERRIDE.name())
                        .replace("%value%", val));
    }

    public static void editBlacklisted(CommandSender sendi, String block, boolean add) {

        FileOther.FILETYPE file = FileOther.FILETYPE.CONFIG;
        YamlConfiguration config = file.getConfig();

        List<String> world_map = config.getStringList("BlacklistedBlocks");
        List<String> removeList = new ArrayList<>();
        for (String m : world_map) {
            if (m.equals(block)) {
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

        saveAsync(file, config);
        BetterRTP.getInstance().getRTP().load();
        Message_RTP.sms(sendi,
                MessagesCore.EDIT_SET.get(sendi, null)
                        .replace("%type%", CmdEdit.RTP_CMD_EDIT.BLACKLISTEDBLOCKS.name())
                        .replace("%value%", block));
    }

    private static List<Map<String, Object>> mutableMapList(List<Map<?, ?>> source) {
        List<Map<String, Object>> result = new ArrayList<>();
        source.stream().map(HelperRTP_EditWorlds::mutableStringMap).forEach(result::add);
        return result;
    }

    private static Map<String, Object> mutableStringMap(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(String.valueOf(key), value));
        return result;
    }

    private static void saveAsync(FileOther.FILETYPE file, YamlConfiguration config) {
        String contents = config.saveToString();
        java.util.logging.Logger logger = BetterRTP.getInstance().getLogger();
        AsyncHandler.async(() -> {
            try {
                Files.writeString(file.getFile().toPath(), contents, StandardCharsets.UTF_8);
            } catch (IOException exception) {
                logger.log(
                        java.util.logging.Level.SEVERE, "Unable to save " + file.fileName(), exception);
            }
        });
    }

}
