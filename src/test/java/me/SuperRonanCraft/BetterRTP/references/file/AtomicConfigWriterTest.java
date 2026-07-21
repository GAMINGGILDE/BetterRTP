package me.SuperRonanCraft.BetterRTP.references.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtomicConfigWriterTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void backsUpAndAtomicallyReplacesConfiguration() throws Exception {
        File file = temporaryDirectory.resolve("config.yml").toFile();
        Files.writeString(file.toPath(), "Config-Version: 3\n");

        Path backup = AtomicConfigWriter.backup(file, 3);
        AtomicConfigWriter.write(file, "Config-Version: 4\n");

        assertTrue(Files.exists(backup));
        assertEquals("Config-Version: 3\n", Files.readString(backup));
        assertEquals("Config-Version: 4\n", Files.readString(file.toPath()));
    }
}
