package me.SuperRonanCraft.BetterRTP.player.commands.types;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.player.commands.RTPCommand;
import me.SuperRonanCraft.BetterRTP.player.rtp.RTPSetupInformation;
import me.SuperRonanCraft.BetterRTP.references.PermissionNode;
import me.SuperRonanCraft.BetterRTP.references.helpers.HelperRTP;
import me.SuperRonanCraft.BetterRTP.references.messages.Message;
import me.SuperRonanCraft.BetterRTP.references.messages.Message_RTP;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueData;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueRange;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldPlayer;
import me.SuperRonanCraft.BetterRTP.references.web.LogUploader;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdQueue implements RTPCommand {

    public String getName() {
        return "queue";
    }

    public void execute(CommandSender sendi, String label, String[] args) {
        Player p = (Player) sendi;
        AsyncHandler.global(() -> {
            List<World> worlds;
            if (args.length > 1) {
                World world = Bukkit.getWorld(args[1]);
                worlds = world == null ? List.of() : List.of(world);
            } else {
                worlds = List.copyOf(Bukkit.getWorlds());
            }
            AsyncHandler.syncAtEntity(p, () -> prepareQueries(p, worlds, label, args));
        });
    }

    private void prepareQueries(Player player, List<World> worlds, String label, String[] args) {
        List<QueueWorldQuery> queries = worlds.stream()
                .map(world -> {
                    WorldPlayer worldPlayer = HelperRTP.getPlayerWorld(new RTPSetupInformation(
                            HelperRTP.getActualWorld(player, world), player, player, true));
                    return new QueueWorldQuery(
                            world.getName(), worldPlayer, QueueRange.from(worldPlayer));
                })
                .toList();
        AsyncHandler.async(() -> {
            List<String> info = new ArrayList<>();
            for (QueueWorldQuery query : queries) {
                info.addAll(queueGetWorld(query));
            }
            if (queries.size() != 1) {
                info.add("&eTotal of &a%amount% &egenerated locations"
                        .replace("%amount%", String.valueOf(info.size())));
            }
            AsyncHandler.syncAtEntity(player, () -> sendInfo(player, info, label, args));
        });
    }

    //World
    public static void sendInfo(CommandSender sendi, List<String> list, String label, String[] args) { //Send info
        boolean upload = Arrays.asList(args).contains("_UPLOAD_");
        list.add(0, "&e&m-----&6 BetterRTP &8| Queue &e&m-----");
        list.forEach(str -> list.set(list.indexOf(str), Message.color(str)));
        String cmd = "/" + label + " " + String.join(" ", args);
        if (!upload) {
            sendi.sendMessage(list.toArray(new String[0]));
            if (sendi instanceof Player) {
                Component component = Message.component("&7- &7Click to upload command log to &flogs.ronanplugins.com")
                        .clickEvent(ClickEvent.suggestCommand(cmd + " _UPLOAD_"))
                        .hoverEvent(HoverEvent.showText(Message.component("&6Suggested command&f: &7"
                                + "/betterrtp " + String.join(" ", args) + " _UPLOAD_")));
                sendi.sendMessage(component);
            } else {
                sendi.sendMessage("Execute `" + cmd + " _UPLOAD_`" + " to upload command log to https://logs.ronanplugins.com");
            }
        } else {
            list.add(0, "Command: " + cmd);
            list.forEach(str -> list.set(list.indexOf(str), Message.stripColor(str)));
            AsyncHandler.async(() -> {
                String key = LogUploader.post(list);
                if (key == null) {
                    Message.sms(sendi, new ArrayList<>(Collections.singletonList("&cAn error occured attempting to upload log!")), null);
                } else {
                    try {
                        String uploadKey = JsonParser.parseString(key).getAsJsonObject().get("key").getAsString();
                        Message.sms(sendi, Arrays.asList(" ", Message.getPrefix(Message_RTP.msg) + "&aLog uploaded! &fView&7: &6https://logs.ronanplugins.com/" + uploadKey), null);
                    } catch (JsonParseException | IllegalStateException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    private static List<String> queueGetWorld(QueueWorldQuery query) {
        List<String> info = new ArrayList<>();
        info.add("&eWorld: &6" + query.worldName());
        for (QueueData queue : BetterRTP.getInstance().getQueue()
                .applicable(query.worldPlayer(), query.range())) {
            String str = "&8- &7x= &b%x, &7z= &b%z";
            Location loc = queue.getLocation();
            str = str.replace("%x", String.valueOf(loc.getBlockX())).replace("%z", String.valueOf(loc.getBlockZ()));
            info.add(str);
        }
        return info;
    }

    private record QueueWorldQuery(
            String worldName, WorldPlayer worldPlayer, QueueRange range) {
    }

    public List<String> tabComplete(CommandSender sendi, String[] args) {
        List<String> info = new ArrayList<>();
        if (args.length == 2) {
            for (World world : Bukkit.getWorlds())
                if (world.getName().startsWith(args[1]))
                    info.add(world.getName());
        }
        return info;
    }

    @NotNull public PermissionNode permission() {
        return PermissionNode.ADMIN;
    }
}
