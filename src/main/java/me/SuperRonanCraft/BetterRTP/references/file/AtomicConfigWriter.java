package me.SuperRonanCraft.BetterRTP.references.file;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

final class AtomicConfigWriter {

    private static final DateTimeFormatter BACKUP_SUFFIX = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private AtomicConfigWriter() {
    }

    static Path backup(File file, int sourceVersion) throws IOException {
        Path source = file.toPath();
        Path backup = source.resolveSibling(file.getName() + ".v" + sourceVersion + ".bak");
        if (Files.exists(backup)) {
            String timestamp = BACKUP_SUFFIX.format(LocalDateTime.now());
            int counter = 0;
            do {
                String suffix = counter == 0 ? "" : "-" + counter;
                counter++;
                backup = source.resolveSibling(file.getName() + ".v" + sourceVersion + "."
                        + timestamp + suffix + ".bak");
            } while (Files.exists(backup));
        }
        return Files.copy(source, backup, StandardCopyOption.COPY_ATTRIBUTES);
    }

    static void write(File file, String contents) throws IOException {
        Path target = file.toPath();
        Path parent = target.getParent();
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, file.getName(), ".tmp");
        try {
            Files.writeString(temporary, contents, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, target,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
