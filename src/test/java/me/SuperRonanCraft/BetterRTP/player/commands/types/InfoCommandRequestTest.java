package me.SuperRonanCraft.BetterRTP.player.commands.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InfoCommandRequestTest {

    @Test
    void parsesOverviewWorldAndPlayerModes() {
        assertEquals(
                InfoCommandRequest.Subcommand.OVERVIEW,
                InfoCommandRequest.parse(new String[]{"info"}).subcommand());

        InfoCommandRequest world = InfoCommandRequest.parse(
                new String[]{"info", "world", "survival", "Alex"});
        assertEquals(InfoCommandRequest.Subcommand.WORLD, world.subcommand());
        assertEquals("survival", world.worldName());
        assertEquals("Alex", world.playerName());

        InfoCommandRequest player = InfoCommandRequest.parse(
                new String[]{"info", "player", "Steve"});
        assertEquals(InfoCommandRequest.Subcommand.PLAYER, player.subcommand());
        assertNull(player.worldName());
        assertEquals("Steve", player.playerName());
    }

    @Test
    void representsMissingAndUnknownArgumentsExplicitly() {
        InfoCommandRequest missingWorld =
                InfoCommandRequest.parse(new String[]{"info", "world"});
        assertEquals(InfoCommandRequest.Subcommand.WORLD, missingWorld.subcommand());
        assertNull(missingWorld.worldName());

        assertEquals(
                InfoCommandRequest.Subcommand.UNKNOWN,
                InfoCommandRequest.parse(
                        new String[]{"info", "does-not-exist"}).subcommand());
    }
}
