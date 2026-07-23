package me.SuperRonanCraft.BetterRTP.player.events;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.player.commands.RTPCommandType;
import me.SuperRonanCraft.BetterRTP.references.PermissionNode;
import me.SuperRonanCraft.BetterRTP.references.file.FileOther;
import me.SuperRonanCraft.BetterRTP.references.messages.Message;
import me.SuperRonanCraft.BetterRTP.references.messages.Message_RTP;
import me.SuperRonanCraft.BetterRTP.references.messages.MessagesCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.List;

class Interact {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private boolean enabled;
    private String title, coloredTitle;

    void load() {
        String pre = "Settings.";
        FileOther.FILETYPE file = BetterRTP.getInstance().getFiles().getType(FileOther.FILETYPE.SIGNS);
        enabled = file.getBoolean(pre + "Enabled");
        title = file.getString(pre + "Title");
        coloredTitle = Message.color(title);
    }

    void event(PlayerInteractEvent e) {
        if (enabled && e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK && isSign(e.getClickedBlock())) {
            Sign sign = (Sign) e.getClickedBlock().getState();
            SignSide side = sign.getTargetSide(e.getPlayer());
            if (serialize(side.line(0)).equals(coloredTitle)) {
                String signCommand = cmd(side.lines());
                String command = signCommand.split(" ")[0];
                if (command.isEmpty() || command.equalsIgnoreCase("rtp")) {
                    action(e.getPlayer(), null);
                    return;
                } else
                    for (RTPCommandType cmd : RTPCommandType.values())
                        if (command.equalsIgnoreCase(cmd.name())) {
                            action(e.getPlayer(), signCommand.split(" "));
                            return;
                        }
                Message_RTP.sms(e.getPlayer(), "&cError! &7Command &a"
                        + Arrays.toString(signCommand.split(" ")) + "&7 does not exist! Defaulting command to /rtp!");
            }
        }
    }

    void createSign(SignChangeEvent e) {
        if (enabled && PermissionNode.SIGN_CREATE.check(e.getPlayer())) {
            String line = serialize(e.line(0));
            if (line.equalsIgnoreCase(title) ||
                    line.equalsIgnoreCase("[RTP]")) {
                e.line(0, LEGACY_SERIALIZER.deserialize(coloredTitle != null ? coloredTitle : "[RTP]"));
                MessagesCore.SIGN.send(e.getPlayer(), cmd(e.lines()));
            }
        }
    }

    private void action(Player p, String[] line) {
        BetterRTP.getInstance().getCmd().commandExecuted(p, "rtp", line);
    }

    private static String cmd(List<Component> signLines) {
        StringBuilder actions = new StringBuilder();
        for (int i = 1; i < signLines.size(); i++) {
            String line = serialize(signLines.get(i));
            if (!line.isEmpty()) {
                if (!actions.isEmpty())
                    actions.append(' ');
                actions.append(line);
            }
        }
        return actions.toString();
    }

    private static String serialize(Component component) {
        return LEGACY_SERIALIZER.serialize(component);
    }

    private static boolean isSign(Block block) {
        return block.getState() instanceof Sign;
    }
}
