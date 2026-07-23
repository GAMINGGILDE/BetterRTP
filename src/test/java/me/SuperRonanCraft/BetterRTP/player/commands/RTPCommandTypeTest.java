package me.SuperRonanCraft.BetterRTP.player.commands;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class RTPCommandTypeTest {

    @Test
    void createsFreshCommandStateForEveryLifecycle() {
        for (RTPCommandType type : RTPCommandType.values()) {
            assertNotSame(type.createCommand(), type.createCommand(), type.name());
        }
    }

    @Test
    void commandNamesAreUnique() {
        long uniqueNames = Arrays.stream(RTPCommandType.values())
                .map(type -> type.createCommand().getName().toLowerCase())
                .distinct()
                .count();

        assertEquals(RTPCommandType.values().length, uniqueNames);
    }
}
