package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.references.helpers.BiomeHelper;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.RTPWorld;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WORLD_TYPE;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLocation {

    public static Location generateLocation(RTPWorld rtpWorld) {
        try {
            RtpArea area = new RtpArea(rtpWorld.getCenterX(), rtpWorld.getCenterZ(),
                    rtpWorld.getMinRadius(), rtpWorld.getMaxRadius(), rtpWorld.getShape());
            RtpArea.Point point = area.randomPoint(ThreadLocalRandom.current());
            return new Location(rtpWorld.getWorld(), point.x(), 69, point.z());
        } catch (IllegalArgumentException e) {
            BetterRTP.getInstance().getLogger().warning("Incorrect configuration! Check your config and confirm that MinRadius is smaller than MaxRadius and that they are both positive numbers!");
            BetterRTP.getInstance().getLogger().warning("Max: " + rtpWorld.getMaxRadius() + " Min: " + rtpWorld.getMinRadius());
            return null;
        }
    }

    public static Location getSafeLocation(WORLD_TYPE type, World world, Location loc, int minY, int maxY, List<String> biomes) {
        switch (type) { //Get a Y position and check for bad blocks
            case NETHER: return getLocAtNether(loc.getBlockX(), loc.getBlockZ(), minY, maxY, world, biomes);
            case NORMAL:
            default: return getLocAtNormal(loc.getBlockX(), loc.getBlockZ(), minY, maxY, world, biomes);
        }
    }
    private static Location getLocAtNormal(int x, int z, int minY, int maxY, World world, List<String> biomes) {
        Block b = getHighestBlock(x, z, world);
        if (!b.getType().isSolid()) { //Water, lava, shrubs...
            if (!badBlock(b.getType().name(), x, z, world, null)) { //Make sure it's not an invalid block (ex: water, lava...)
                //int y = world.getHighestBlockYAt(x, z);
                b = world.getBlockAt(x, b.getY() - 1, z);
            }
        }
        //Between max and min y
        if (    b.getY() >= minY
                && b.getY() <= maxY
                && !badBlock(b.getType().name(), x, z, world, biomes)) {
            return new Location(world, x, b.getY() + 1, z);
        }
        return null;
    }

    public static Block getHighestBlock(int x, int z, World world) {
        Block b = world.getHighestBlockAt(x, z);
        if (b.getType().toString().endsWith("AIR")) //1.15.1 or less
            b = world.getBlockAt(x, b.getY() - 1, z);
        return b;
    }

    private static Location getLocAtNether(int x, int z, int minY, int maxY, World world, List<String> biomes) {
        //Max and Min Y
        for (int y = minY + 1; y < maxY/*world.getMaxHeight()*/; y++) {
            Block block_current = world.getBlockAt(x, y, z);
            if (block_current.getType().name().endsWith("AIR") || !block_current.getType().isSolid()) {
                if (!block_current.getType().name().endsWith("AIR") &&
                        !block_current.getType().isSolid()) { //Block is not a solid (ex: lava, water...)
                    String block_in = block_current.getType().name();
                    if (badBlock(block_in, x, z, world, null))
                        continue;
                }
                String block = world.getBlockAt(x, y - 1, z).getType().name();
                if (block.endsWith("AIR")) //Block below is air, skip
                    continue;
                if (world.getBlockAt(x, y + 1, z).getType().name().endsWith("AIR") //Head space
                        && !badBlock(block, x, z, world, biomes)) //Valid block
                    return new Location(world, x, y, z);
            }
        }
        return null;
    }

    // Bad blocks, or bad biome
    public static boolean badBlock(String block, int x, int z, World world, List<String> biomes) {
        for (String currentBlock : BetterRTP.getInstance().getRTP().getBlockList()) //Check Block
            if (currentBlock.equalsIgnoreCase(block))
                return true;
        //Check Biomes
        if (biomes == null || biomes.isEmpty())
            return false;
        String biomeCurrent = BiomeHelper.name(world.getBiome(x, z));
        for (String biome : biomes)
            if (biomeCurrent.toUpperCase().contains(biome.toUpperCase()))
                return false;
        return true;
        //FALSE MEANS NO BAD BLOCKS/BIOME WHERE FOUND!
    }

    public static void runChunkTest() {
        BetterRTP.getInstance().getLogger().info("---------------- Starting chunk test!");
        World world = Bukkit.getWorld("world");
        cacheChunkAt(world, 32, -32, -32, -32);
    }

    private static void cacheTask(World world, int goal, int start, int xat, int zat) {
        zat += 1;
        if (zat > goal) {
            zat = start;
            xat += 1;
        }
        if (xat <= goal)
            cacheChunkAt(world, goal, start, xat, zat);
    }

    private static void cacheChunkAt(World world, int goal, int start, int xat, int zat) {
        Location location = new Location(world, xat * 16, 0, zat * 16);
        AsyncHandler.syncAtLocation(location, () -> {
            CompletableFuture<Chunk> task = world.getChunkAtAsync(location);
            task.thenAccept(chunk -> AsyncHandler.syncAtLocation(location, () -> {
                try {
                    ChunkSnapshot snapshot = chunk.getChunkSnapshot(true, true, false);
                    int maxy = snapshot.getHighestBlockYAt(8, 8);
                    Biome biome = snapshot.getBiome(8, 8);
                    BetterRTP.getInstance().getDatabaseHandler().getDatabaseChunks().addChunk(chunk, maxy, biome);
                    cacheTask(world, goal, start, xat, zat);
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw new RuntimeException();
                }
            }));
        });
    }

}
