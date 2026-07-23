package me.SuperRonanCraft.BetterRTP.references.messages;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageTest {

    @Test
    void convertsAmpersandColorsToSectionColors() {
        assertEquals("§aHello", Message.color("&aHello"));
    }

    @Test
    void preservesLegacyHexColorFormat() {
        assertEquals("§x§1§2§a§b§3§4Hello", Message.color("#12ab34Hello"));
    }

    @Test
    void stripsFormattingThroughPlainTextSerializer() {
        assertEquals("Hello", Message.stripColor(Message.color("&a&lHello")));
    }
}
