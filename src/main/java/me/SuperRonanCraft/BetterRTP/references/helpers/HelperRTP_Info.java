package me.SuperRonanCraft.BetterRTP.references.helpers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import me.SuperRonanCraft.BetterRTP.references.PermissionNode;
import me.SuperRonanCraft.BetterRTP.references.messages.MessagesCore;

public class HelperRTP_Info {

    //Custom biomes
    public static List<String> getBiomes(String[] args, int start, CommandSender sendi) {
        List<String> biomes = new ArrayList<>();
        boolean error_sent = false;
        if (PermissionNode.BIOME.check(sendi))
            for (int i = start; i < args.length; i++) {
                String str = args[i];
                var biome = BiomeHelper.find(str);
                if (biome != null) {
                    biomes.add(BiomeHelper.name(biome));
                } else {
                    if (!error_sent) {
                        MessagesCore.OTHER_BIOME.send(sendi, str);
                        error_sent = true;
                    }
                }
            }
        return biomes;
    }

    public static void addBiomes(List<String> list, String[] args) {
        String prefix = args[args.length - 1].toUpperCase();
        BiomeHelper.stream()
                .map(BiomeHelper::name)
                .filter(name -> name.startsWith(prefix))
                .forEach(list::add);
    }

}
